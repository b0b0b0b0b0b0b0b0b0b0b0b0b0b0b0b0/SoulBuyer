package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.bootstrap.SoulBuyerStartupLog;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.database.migration.StorageTypeMigrationService;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.repository.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public final class StorageBootstrap {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final SoulBuyerDebugLog debug;
    private final SoulBuyerStartupLog startup;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SoulBuyer-Storage");
        thread.setDaemon(true);
        return thread;
    });
    private StorageSession session;

    public StorageBootstrap(
            JavaPlugin plugin,
            PluginConfig config,
            SoulBuyerDebugLog debug,
            SoulBuyerStartupLog startup
    ) {
        this.plugin = plugin;
        this.config = config;
        this.debug = debug;
        this.startup = startup;
    }

    public CompletableFuture<StorageSession> start() {
        return CompletableFuture.supplyAsync(this::startBlocking, executor);
    }

    public StorageSession startBlocking() {
        debug.boot("storage startBlocking type=" + config.storageType());
        new StorageTypeMigrationService(plugin, config, debug, startup).migrateIfNeeded();
        if (config.isFlatStorage()) {
            debug.boot("storage mode flat (YAML)");
            session = createFlatSession();
            debug.boot("storage flat session ready");
            return session;
        }
        debug.boot("storage opening JDBC pool (" + config.storageType() + ")...");
        DataSourceProvider provider = new DataSourceProvider(plugin, config);
        try {
            debug.boot("storage running schema migration...");
            SchemaMigration.migrate(provider.dataSource(), config.isSqliteStorage());
            debug.boot("storage migration OK");
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Database migration failed", exception);
            debug.error("storage migration failed", exception);
            provider.close();
            throw new IllegalStateException(exception);
        }
        SqlPlayerProgressRepository playerProgressRepository = new SqlPlayerProgressRepository(
                provider.dataSource(),
                executor
        );
        SqlPlayerAutosellRepository playerAutosellRepository = new SqlPlayerAutosellRepository(
                provider.dataSource(),
                config,
                executor,
                config.isSqliteStorage()
        );
        SqlPlayerBoosterRepository playerBoosterRepository = new SqlPlayerBoosterRepository(
                provider.dataSource(),
                executor,
                config.isSqliteStorage()
        );
        SqlPlayerSellLimitRepository playerSellLimitRepository = new SqlPlayerSellLimitRepository(
                provider.dataSource(),
                executor,
                config.isSqliteStorage()
        );
        SqlMarketRepository marketRepository = new SqlMarketRepository(provider.dataSource(), executor);
        SqlSaleLogRepository saleLogRepository = new SqlSaleLogRepository(provider.dataSource(), executor);
        session = new StorageSession(
                playerProgressRepository,
                playerAutosellRepository,
                playerBoosterRepository,
                playerSellLimitRepository,
                marketRepository,
                saleLogRepository,
                executor,
                provider,
                () -> {
                    provider.close();
                    executor.shutdown();
                }
        );
        debug.boot("storage SQL session ready");
        return session;
    }

    public StorageSession session() {
        return session;
    }

    public void shutdown() {
        debug.log("storage shutdown");
        if (session != null) {
            session.shutdown().run();
            return;
        }
        executor.shutdown();
    }

    private StorageSession createFlatSession() {
        YamlPlayerProgressRepository playerProgressRepository = new YamlPlayerProgressRepository(
                plugin,
                config,
                executor
        );
        YamlPlayerAutosellRepository playerAutosellRepository = new YamlPlayerAutosellRepository(
                plugin,
                config,
                executor
        );
        YamlPlayerBoosterRepository playerBoosterRepository = new YamlPlayerBoosterRepository(
                plugin,
                config,
                executor
        );
        YamlPlayerSellLimitRepository playerSellLimitRepository = new YamlPlayerSellLimitRepository(
                plugin,
                config,
                executor
        );
        YamlMarketRepository marketRepository = new YamlMarketRepository(plugin, config, executor);
        YamlSaleLogRepository saleLogRepository = new YamlSaleLogRepository(plugin, config, executor);
        return new StorageSession(
                playerProgressRepository,
                playerAutosellRepository,
                playerBoosterRepository,
                playerSellLimitRepository,
                marketRepository,
                saleLogRepository,
                executor,
                null,
                executor::shutdown
        );
    }
}
