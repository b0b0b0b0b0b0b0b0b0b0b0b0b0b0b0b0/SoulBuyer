package bm.b0b0b0.soulBuyer;

import bm.b0b0b0.soulBuyer.api.SoulBuyerApi;
import bm.b0b0b0.soulBuyer.api.SoulBuyerApiImpl;
import bm.b0b0b0.soulBuyer.api.UnavailableSoulBuyerApi;
import bm.b0b0b0.soulBuyer.autosell.AutosellContainerListener;
import bm.b0b0b0.soulBuyer.autosell.AutosellPickupListener;
import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.bootstrap.EconomyWaitListener;
import bm.b0b0b0.soulBuyer.bootstrap.SoulBuyerStartupLog;
import bm.b0b0b0.soulBuyer.catalog.CatalogRotationService;
import bm.b0b0b0.soulBuyer.command.SoulBuyerCommandRegistrar;
import bm.b0b0b0.soulBuyer.config.ConfigLegacyGuard;
import bm.b0b0b0.soulBuyer.config.ConfigurationLoader;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.database.StorageBootstrap;
import bm.b0b0b0.soulBuyer.database.StorageSession;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.gui.BuyerGuiService;
import bm.b0b0b0.soulBuyer.gui.BuyerMenuItemRenderer;
import bm.b0b0b0.soulBuyer.gui.GuiItemFactory;
import bm.b0b0b0.soulBuyer.integration.*;
import bm.b0b0b0.soulBuyer.io.SoulBuyerIoExecutor;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.limit.SellLimitService;
import bm.b0b0b0.soulBuyer.listener.BuyerInventoryListener;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.market.PriceQuoteService;
import bm.b0b0b0.soulBuyer.message.MessageLoader;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import bm.b0b0b0.soulBuyer.repository.CatalogRotationRepository;
import bm.b0b0b0.soulBuyer.repository.SaleLogRepository;
import bm.b0b0b0.soulBuyer.repository.YamlCatalogRotationRepository;
import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import bm.b0b0b0.soulBuyer.service.SellSecureStorage;
import bm.b0b0b0.soulBuyer.service.SellService;
import bm.b0b0b0.soulBuyer.sync.RedisBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SoulBuyer extends JavaPlugin {

    private static final int ECONOMY_RETRY_MAX = 400;
    private static final long ECONOMY_RETRY_TICKS = 5L;

    private final AtomicBoolean economyWaitActive = new AtomicBoolean(false);
    private final AtomicBoolean bootstrapFinished = new AtomicBoolean(false);

    private SoulBuyerDebugLog debugLog;
    private SoulBuyerStartupLog startupLog;
    private ConfigurationLoader configurationLoader;
    private SoulBuyerCommandRegistrar commandRegistrar;
    private MessageService messageService;
    private MessageLoader messageLoader;
    private StorageBootstrap storageBootstrap;
    private RedisBootstrap redisBootstrap;
    private MarketService marketService;
    private CatalogRotationService catalogRotationService;
    private PlaceholderApiBridge placeholderApiBridge;
    private BuyerStatsService buyerStatsService;
    private SoulBuyerRuntime runtime;
    private SoulBuyerApi soulBuyerApi;
    private SaleLogRepository saleLogRepository;

    private PluginConfig pendingConfig;
    private StorageSession pendingSession;
    private RedisBootstrap pendingRedis;
    private int economyRetryAttempt;
    private boolean economyWaitLogged;

    public SoulBuyerDebugLog debug() {
        return debugLog;
    }

    @Override
    public void onEnable() {
        debugLog = new SoulBuyerDebugLog(this);
        startupLog = new SoulBuyerStartupLog();
        startupLog.bannerStart(getPluginMeta().getVersion());
        debugLog.boot("onEnable start, plugin=" + getPluginMeta().getVersion());

        runtime = new SoulBuyerRuntime();
        commandRegistrar = new SoulBuyerCommandRegistrar(this, runtime);
        commandRegistrar.registerLifecycle();
        getServer().getPluginManager().registerEvents(new EconomyWaitListener(this), this);

        try {
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
                debugLog.warn("failed to create data folder");
                startupLog.stepFail("Папка данных — не удалось создать");
            }

            ConfigLegacyGuard.prepare(this, debugLog);
            startupLog.info("Загрузка конфигурации...");
            debugLog.boot("loading Elytrium configs on main thread...");
            configurationLoader = new ConfigurationLoader();
            PluginConfig pluginConfig = configurationLoader.load(this, debugLog);
            debugLog.setEnabled(pluginConfig.debug());
            startupLog.stepOk("Конфиг — storage=" + pluginConfig.storageType()
                    + ", предметов=" + pluginConfig.items().size()
                    + ", debug=" + pluginConfig.debug());
            debugLog.boot("config OK | storage=" + pluginConfig.storageType()
                    + " | items=" + pluginConfig.items().size()
                    + " | debug=" + pluginConfig.debug());

            messageLoader = new MessageLoader(
                    this,
                    pluginConfig.defaultLocale(),
                    pluginConfig.fallbackLocale()
            );
            startupLog.info("Загрузка lang (" + pluginConfig.defaultLocale() + ")...");
            debugLog.boot("loading lang (" + pluginConfig.defaultLocale() + ")...");
            messageLoader.load();
            startupLog.stepOk("Lang — " + pluginConfig.defaultLocale());
            debugLog.boot("lang OK");

            startupLog.info("Подключение хранилища...");
            debugLog.boot("queue storage/redis on IO thread...");
            SoulBuyerIoExecutor.executor().execute(() -> runStorageBootstrap(pluginConfig));
        } catch (Throwable throwable) {
            debugLog.error("onEnable failed on main thread", throwable);
            startupLog.stepFail("Ошибка при старте — см. stack trace выше");
            startupLog.bannerFailure(throwable.getMessage() == null ? "unknown error" : throwable.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void runStorageBootstrap(PluginConfig pluginConfig) {
        long startedAt = System.currentTimeMillis();
        debugLog.boot("runStorageBootstrap entered");
        try {
            StorageBootstrap storage = new StorageBootstrap(this, pluginConfig, debugLog, startupLog);
            debugLog.boot("storage startBlocking...");
            StorageSession session = storage.startBlocking();
            storageBootstrap = storage;
            startupLog.stepOk("Хранилище — " + pluginConfig.storageType());
            debugLog.boot("storage OK");

            RedisBootstrap redis = new RedisBootstrap(this, pluginConfig, debugLog);
            redis.connect();
            logRedisStatus(pluginConfig, redis);
            debugLog.boot("redis step OK in " + (System.currentTimeMillis() - startedAt) + "ms");

            Bukkit.getScheduler().runTask(this, () -> beginActivate(pluginConfig, session, redis));
        } catch (Throwable throwable) {
            debugLog.error("storage bootstrap FAILED after " + (System.currentTimeMillis() - startedAt) + "ms", throwable);
            startupLog.stepFail("Хранилище — ошибка подключения");
            startupLog.bannerFailure("storage bootstrap failed");
            Bukkit.getScheduler().runTask(this, () -> {
                shutdownStartedResources();
                if (isEnabled()) {
                    getServer().getPluginManager().disablePlugin(this);
                }
            });
        }
    }

    public void retryEconomyActivation() {
        if (!economyWaitActive.get() || bootstrapFinished.get()) {
            return;
        }
        debugLog.log("economy plugin event -> retry activation");
        Bukkit.getScheduler().runTask(this, this::attemptEconomyActivation);
    }

    private void beginActivate(PluginConfig pluginConfig, StorageSession session, RedisBootstrap redis) {
        debugLog.boot("beginActivate on main thread, enabled=" + isEnabled());
        if (!isEnabled()) {
            session.shutdown().run();
            redis.shutdown();
            return;
        }

        messageService = new MessageService(messageLoader);
        commandRegistrar.bind(pluginConfig, messageService, configurationLoader);
        debugLog.boot("command registrar bound, runtime.ready=" + runtime.isReady());

        pendingConfig = pluginConfig;
        pendingSession = session;
        pendingRedis = redis;
        economyRetryAttempt = 0;
        economyWaitLogged = false;
        economyWaitActive.set(true);
        startupLog.info("Проверка зависимостей...");
        attemptEconomyActivation();
    }

    private void attemptEconomyActivation() {
        if (!isEnabled() || bootstrapFinished.get()) {
            return;
        }

        PluginConfig config = pendingConfig;
        boolean needVault = config.requiresVaultEconomy();
        boolean needPlayerPoints = config.requiresPlayerPoints();

        VaultEconomyHook vaultEconomyHook = new VaultEconomyHook(this, debugLog);
        PlayerPointsEconomyHook playerPointsEconomyHook = new PlayerPointsEconomyHook(debugLog);

        if (needVault) {
            VaultEconomyHook.ProbeState vaultState = vaultEconomyHook.probe();
            if (vaultState != VaultEconomyHook.ProbeState.READY) {
                handleVaultEconomyWait(vaultState);
                return;
            }
        } else {
            logVaultDependencySkipped();
        }

        if (needPlayerPoints) {
            PlayerPointsEconomyHook.ProbeState playerPointsState = playerPointsEconomyHook.probe();
            if (playerPointsState != PlayerPointsEconomyHook.ProbeState.READY) {
                handlePlayerPointsWait(playerPointsState);
                return;
            }
        } else {
            logPlayerPointsDependencySkipped();
        }

        economyWaitActive.set(false);
        logEconomyStatus(config, vaultEconomyHook, playerPointsEconomyHook);
        debugLog.boot("economy hooked on attempt " + economyRetryAttempt
                + " vault=" + vaultEconomyHook.available()
                + " playerpoints=" + playerPointsEconomyHook.available());
        EconomyPayoutRouter payoutRouter = new EconomyPayoutRouter(vaultEconomyHook, playerPointsEconomyHook);
        finishActivate(pendingConfig, pendingSession, pendingRedis, vaultEconomyHook, playerPointsEconomyHook, payoutRouter);
    }

    private void handleVaultEconomyWait(VaultEconomyHook.ProbeState state) {
        if (!economyWaitLogged) {
            economyWaitLogged = true;
            if (state == VaultEconomyHook.ProbeState.VAULT_ABSENT) {
                startupLog.stepFail("Vault — не найден");
            } else {
                startupLog.stepOk("Vault — найден");
                startupLog.stepFail("Economy — провайдер не зарегистрирован");
            }
        }
        if (state == VaultEconomyHook.ProbeState.VAULT_ABSENT) {
            abortEconomyBootstrap(
                    "Vault не найден. Установите Vault и economy-плагин (EssentialsX, CMI или аналог)."
            );
            return;
        }
        if (state == VaultEconomyHook.ProbeState.ECONOMY_ABSENT
                && !EconomyPluginPresence.anyKnownEconomyPluginInstalled()) {
            abortEconomyBootstrap(
                    "Vault установлен, но economy-плагин не найден. Установите EssentialsX, CMI или аналог."
            );
            return;
        }
        scheduleEconomyRetry("Economy-провайдер Vault");
    }

    private void handlePlayerPointsWait(PlayerPointsEconomyHook.ProbeState state) {
        if (!economyWaitLogged) {
            economyWaitLogged = true;
            if (state == PlayerPointsEconomyHook.ProbeState.PLUGIN_ABSENT) {
                startupLog.stepFail("PlayerPoints — не найден");
            } else {
                startupLog.stepOk("PlayerPoints — найден");
                startupLog.stepFail("PlayerPoints — API ещё не готов");
            }
        }
        if (state == PlayerPointsEconomyHook.ProbeState.PLUGIN_ABSENT) {
            abortEconomyBootstrap(
                    "PlayerPoints не найден. Установите PlayerPoints или отключите player-points-enabled в config.yml."
            );
            return;
        }
        scheduleEconomyRetry("PlayerPoints");
    }

    private void logVaultDependencySkipped() {
        if (EconomyPluginPresence.vaultInstalled()) {
            startupLog.stepSkipped("Vault — не требуется в конфиге (найден на сервере)");
        } else {
            startupLog.stepSkipped("Vault — не требуется в конфиге");
        }
    }

    private void logPlayerPointsDependencySkipped() {
        if (EconomyPluginPresence.playerPointsInstalled()) {
            startupLog.stepSkipped("PlayerPoints — отключён в конфиге (найден на сервере)");
        } else {
            startupLog.stepSkipped("PlayerPoints — отключён в конфиге");
        }
    }

    private void abortEconomyBootstrap(String message) {
        economyWaitActive.set(false);
        logPlayerPointsDependencySkipped();
        startupLog.abort(message);
        pendingSession.shutdown().run();
        pendingRedis.shutdown();
        getServer().getPluginManager().disablePlugin(this);
    }

    private void scheduleEconomyRetry(String label) {
        if (economyRetryAttempt == 0) {
            startupLog.stepWaiting(label + " — ожидание...");
            debugLog.boot(label + " missing, waiting...");
        } else if (economyRetryAttempt % 20 == 0) {
            debugLog.log("still waiting for " + label + ", attempt=" + economyRetryAttempt + "/" + ECONOMY_RETRY_MAX);
        }
        if (economyRetryAttempt >= ECONOMY_RETRY_MAX) {
            economyWaitActive.set(false);
            startupLog.stepFail(label + " — не подключился");
            startupLog.abort(label + " не подключился — SoulBuyer отключён.");
            debugLog.boot(label + " not available — disabling SoulBuyer");
            pendingSession.shutdown().run();
            pendingRedis.shutdown();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economyRetryAttempt++;
        getServer().getScheduler().runTaskLater(this, this::attemptEconomyActivation, ECONOMY_RETRY_TICKS);
    }

    private void finishActivate(
            PluginConfig pluginConfig,
            StorageSession session,
            RedisBootstrap redis,
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook,
            EconomyPayoutRouter economyPayoutRouter
    ) {
        if (!bootstrapFinished.compareAndSet(false, true)) {
            debugLog.warn("finishActivate called twice, ignored");
            return;
        }
        try {
            finishActivateBody(
                    pluginConfig,
                    session,
                    redis,
                    vaultEconomyHook,
                    playerPointsEconomyHook,
                    economyPayoutRouter
            );
        } catch (Throwable throwable) {
            bootstrapFinished.set(false);
            debugLog.error("finishActivate failed", throwable);
            startupLog.abort("Ошибка инициализации — см. stack trace выше");
            shutdownStartedResources();
            if (isEnabled()) {
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    private void finishActivateBody(
            PluginConfig pluginConfig,
            StorageSession session,
            RedisBootstrap redis,
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook,
            EconomyPayoutRouter economyPayoutRouter
    ) {
        debugLog.boot("finishActivate: wiring services...");

        redisBootstrap = redis;
        saleLogRepository = session.saleLog();

        ItemRegistry itemRegistry = new ItemRegistry(pluginConfig);
        debugLog.log("item registry pool=" + itemRegistry.poolSize());

        ItemNameResolver itemNameResolver = new ItemNameResolver();
        ProgressionService progressionService = new ProgressionService(pluginConfig);

        marketService = new MarketService(this, pluginConfig, session.market(), redisBootstrap);
        redisBootstrap.startSubscriber(marketService::applyRemoteUpdate);

        CatalogRotationRepository catalogRotationRepository = new YamlCatalogRotationRepository(
                this,
                pluginConfig,
                SoulBuyerIoExecutor.executor()
        );
        catalogRotationService = new CatalogRotationService(
                this,
                pluginConfig,
                itemRegistry,
                marketService,
                messageService,
                catalogRotationRepository,
                debugLog
        );

        session.market().loadAllCoefficients()
                .exceptionally(error -> {
                    debugLog.error("market load failed", error);
                    return Map.of();
                })
                .thenAccept(coefficients ->
                        Bukkit.getScheduler().runTask(this, () -> {
                            if (!isEnabled() || !bootstrapFinished.get()) {
                                return;
                            }
                            marketService.loadFromDatabase(coefficients);
                            debugLog.log("market coefficients loaded, count=" + coefficients.size()
                                    + " memory=" + coefficients.size());
                            runtime.markReady();
                            catalogRotationService.start();
                            startupLog.stepOk("Рынок — " + coefficients.size() + " позиций");
                            startupLog.bannerSuccess();
                            debugLog.boot("runtime ready after market load");
                        })
                );

        BoosterService boosterService = new BoosterService(
                this,
                pluginConfig,
                messageService,
                session.playerBoosters(),
                session.playerProgress(),
                vaultEconomyHook,
                playerPointsEconomyHook,
                playerId -> session.playerProgress().find(playerId).thenAccept(runtime::cacheProgress)
        );
        SellLimitService sellLimitService = new SellLimitService(
                pluginConfig,
                itemRegistry,
                boosterService,
                session.playerSellLimits()
        );
        PriceQuoteService priceQuoteService = new PriceQuoteService(
                itemRegistry,
                marketService,
                progressionService,
                boosterService
        );
        SellSecureStorage secureStorage = new SellSecureStorage();

        placeholderApiBridge = new PlaceholderApiBridge(this);
        messageService.bindPlaceholderApi(placeholderApiBridge);
        buyerStatsService = new BuyerStatsService(
                pluginConfig,
                messageService,
                itemRegistry,
                itemNameResolver,
                catalogRotationService,
                saleLogRepository,
                placeholderApiBridge
        );
        placeholderApiBridge.registerExpansion(buyerStatsService);
        logPlaceholderApiStatus(placeholderApiBridge);

        SellService sellService = new SellService(this, runtime, debugLog);

        AutosellService autosellService = new AutosellService(
                this,
                pluginConfig,
                itemRegistry,
                session.playerAutosell(),
                sellService
        );

        GuiItemFactory guiItemFactory = new GuiItemFactory(messageService);
        BuyerMenuItemRenderer buyerMenuItemRenderer = new BuyerMenuItemRenderer(
                this,
                messageService,
                itemNameResolver
        );
        BuyerGuiService buyerGuiService = new BuyerGuiService(
                this,
                pluginConfig,
                messageService,
                guiItemFactory,
                itemRegistry,
                itemNameResolver,
                buyerMenuItemRenderer,
                sellService,
                buyerStatsService,
                autosellService,
                boosterService,
                debugLog
        );

        runtime.bind(
                pluginConfig,
                messageService,
                itemRegistry,
                itemNameResolver,
                priceQuoteService,
                marketService,
                progressionService,
                session.playerProgress(),
                session.playerSellLimits(),
                saleLogRepository,
                vaultEconomyHook,
                playerPointsEconomyHook,
                economyPayoutRouter,
                secureStorage,
                buyerGuiService,
                buyerStatsService,
                boosterService,
                sellLimitService,
                catalogRotationService
        );
        debugLog.boot("runtime.bind complete, ready=" + runtime.isReady());

        soulBuyerApi = new SoulBuyerApiImpl(
                buyerGuiService,
                itemRegistry,
                marketService,
                sellService,
                session.playerProgress(),
                autosellService,
                runtime::isReady
        );
        getServer().getServicesManager().register(SoulBuyerApi.class, soulBuyerApi, this, ServicePriority.Normal);

        getServer().getPluginManager().registerEvents(new BuyerInventoryListener(sellService), this);
        getServer().getPluginManager().registerEvents(
                new AutosellPickupListener(this, autosellService, pluginConfig.autosell().pickupDelayTicks),
                this
        );
        getServer().getPluginManager().registerEvents(new AutosellContainerListener(autosellService), this);
        for (Player online : getServer().getOnlinePlayers()) {
            autosellService.preload(online);
            boosterService.preload(online);
        }

        long flushInterval = Math.max(1000L, pluginConfig.market().saleFlushIntervalMs);
        getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                saleLogRepository::flushPending,
                flushInterval / 50L,
                flushInterval / 50L
        );

        startupLog.info("Загрузка коэффициентов рынка...");
        debugLog.boot("SoulBuyer wiring complete, waiting for market | storage=" + pluginConfig.storageType()
                + " | server-id=" + pluginConfig.serverId());
    }

    private void logRedisStatus(PluginConfig pluginConfig, RedisBootstrap redis) {
        if (pluginConfig.singleServer() || !pluginConfig.redis().enabled) {
            startupLog.stepSkipped("Redis — не используется (single-server или disabled)");
            return;
        }
        if (redis.connected()) {
            startupLog.stepOk("Redis — " + pluginConfig.redis().host + ":" + pluginConfig.redis().port);
            return;
        }
        startupLog.stepFail("Redis — не подключён");
    }

    private void logEconomyStatus(
            PluginConfig config,
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook
    ) {
        if (config.requiresVaultEconomy()) {
            if (vaultEconomyHook.available()) {
                String name = vaultEconomyHook.providerName();
                startupLog.stepOk("Vault — economy" + (name.isEmpty() ? "" : " (" + name + ")"));
            } else {
                startupLog.stepFail("Vault — economy не подключена");
            }
        }
        if (config.requiresPlayerPoints()) {
            if (playerPointsEconomyHook.available()) {
                startupLog.stepOk("PlayerPoints — API подключён");
            } else {
                startupLog.stepFail("PlayerPoints — API не подключён");
            }
        }
    }

    private void logPlaceholderApiStatus(PlaceholderApiBridge placeholderApiBridge) {
        if (!placeholderApiBridge.available()) {
            startupLog.stepSkipped("PlaceholderAPI — не найден (плейсхолдеры отключены)");
            return;
        }
        startupLog.stepOk("PlaceholderAPI — найден");
    }

    private void shutdownStartedResources() {
        debugLog.boot("shutdownStartedResources");
        if (pendingSession != null) {
            pendingSession.shutdown().run();
        }
        if (storageBootstrap != null) {
            storageBootstrap.shutdown();
        }
        if (pendingRedis != null) {
            pendingRedis.shutdown();
        }
        if (redisBootstrap != null) {
            redisBootstrap.shutdown();
        }
    }

    public MessageService messageService() {
        return messageService;
    }

    public SoulBuyerRuntime runtime() {
        return runtime;
    }

    public SoulBuyerApi api() {
        return soulBuyerApi == null ? UnavailableSoulBuyerApi.INSTANCE : soulBuyerApi;
    }

    @Override
    public void onDisable() {
        if (soulBuyerApi != null) {
            getServer().getServicesManager().unregisterAll(this);
            soulBuyerApi = null;
        }
        if (debugLog != null) {
            debugLog.boot("onDisable start, ready=" + (runtime != null && runtime.isReady()));
        }
        economyWaitActive.set(false);
        if (saleLogRepository != null) {
            try {
                saleLogRepository.drainPending().get(5L, TimeUnit.SECONDS);
            } catch (Exception exception) {
                if (debugLog != null) {
                    debugLog.warn("drain pending sales on disable failed: " + exception.getMessage());
                }
            }
        }
        if (catalogRotationService != null) {
            catalogRotationService.shutdown();
        }
        if (marketService != null) {
            marketService.shutdown();
        }
        if (redisBootstrap != null) {
            redisBootstrap.shutdown();
        }
        if (pendingRedis != null) {
            pendingRedis.shutdown();
        }
        if (storageBootstrap != null) {
            storageBootstrap.shutdown();
        }
        if (pendingSession != null) {
            pendingSession.shutdown().run();
        }
        if (debugLog != null) {
            debugLog.boot("onDisable complete");
        }
        if (startupLog != null) {
            startupLog.unload();
        }
    }
}
