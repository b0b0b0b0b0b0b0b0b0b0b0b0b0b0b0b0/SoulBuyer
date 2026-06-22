package bm.b0b0b0.soulBuyer;

import bm.b0b0b0.soulBuyer.catalog.CatalogRotationService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.gui.BuyerGuiService;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.market.PriceQuoteService;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.limit.SellLimitService;
import bm.b0b0b0.soulBuyer.repository.PlayerProgressRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerSellLimitRepository;
import bm.b0b0b0.soulBuyer.repository.SaleLogRepository;
import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import bm.b0b0b0.soulBuyer.service.SellSecureStorage;
import bm.b0b0b0.soulBuyer.service.SellService;
import bm.b0b0b0.soulBuyer.integration.EconomyPayoutRouter;
import bm.b0b0b0.soulBuyer.integration.PlayerPointsEconomyHook;
import bm.b0b0b0.soulBuyer.integration.VaultEconomyHook;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SoulBuyerRuntime implements SellService.PluginContext {

    private PluginConfig config;
    private MessageService messageService;
    private ItemRegistry itemRegistry;
    private ItemNameResolver itemNameResolver;
    private PriceQuoteService priceQuoteService;
    private MarketService marketService;
    private ProgressionService progressionService;
    private PlayerProgressRepository playerProgressRepository;
    private PlayerSellLimitRepository sellLimitRepository;
    private SaleLogRepository saleLogRepository;
    private VaultEconomyHook vaultEconomyHook;
    private PlayerPointsEconomyHook playerPointsEconomyHook;
    private EconomyPayoutRouter economyPayoutRouter;
    private SellSecureStorage secureStorage;
    private BuyerGuiService buyerGuiService;
    private BuyerStatsService buyerStatsService;
    private BoosterService boosterService;
    private SellLimitService sellLimitService;
    private CatalogRotationService catalogRotationService;
    private final Map<UUID, PlayerProgress> progressCache = new ConcurrentHashMap<>();
    private volatile boolean ready;

    public boolean isReady() {
        return ready;
    }

    public void bind(
            PluginConfig config,
            MessageService messageService,
            ItemRegistry itemRegistry,
            ItemNameResolver itemNameResolver,
            PriceQuoteService priceQuoteService,
            MarketService marketService,
            ProgressionService progressionService,
            PlayerProgressRepository playerProgressRepository,
            PlayerSellLimitRepository sellLimitRepository,
            SaleLogRepository saleLogRepository,
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook,
            EconomyPayoutRouter economyPayoutRouter,
            SellSecureStorage secureStorage,
            BuyerGuiService buyerGuiService,
            BuyerStatsService buyerStatsService,
            BoosterService boosterService,
            SellLimitService sellLimitService,
            CatalogRotationService catalogRotationService
    ) {
        this.config = config;
        this.messageService = messageService;
        this.itemRegistry = itemRegistry;
        this.itemNameResolver = itemNameResolver;
        this.priceQuoteService = priceQuoteService;
        this.marketService = marketService;
        this.progressionService = progressionService;
        this.playerProgressRepository = playerProgressRepository;
        this.sellLimitRepository = sellLimitRepository;
        this.saleLogRepository = saleLogRepository;
        this.vaultEconomyHook = vaultEconomyHook;
        this.playerPointsEconomyHook = playerPointsEconomyHook;
        this.economyPayoutRouter = economyPayoutRouter;
        this.secureStorage = secureStorage;
        this.buyerGuiService = buyerGuiService;
        this.buyerStatsService = buyerStatsService;
        this.boosterService = boosterService;
        this.sellLimitService = sellLimitService;
        this.catalogRotationService = catalogRotationService;
    }

    public void markReady() {
        this.ready = true;
    }

    public BuyerStatsService buyerStatsService() {
        return buyerStatsService;
    }

    public CatalogRotationService catalogRotationService() {
        return catalogRotationService;
    }

    public BuyerGuiService buyerGuiService() {
        return buyerGuiService;
    }

    @Override
    public PluginConfig config() {
        return config;
    }

    @Override
    public MessageService messageService() {
        return messageService;
    }

    public void updatePluginConfig(PluginConfig config) {
        this.config = config;
    }

    public ItemRegistry itemRegistry() {
        return itemRegistry;
    }

    @Override
    public PriceQuoteService priceQuoteService() {
        return priceQuoteService;
    }

    @Override
    public MarketService marketService() {
        return marketService;
    }

    @Override
    public ProgressionService progressionService() {
        return progressionService;
    }

    @Override
    public PlayerProgressRepository playerProgressRepository() {
        return playerProgressRepository;
    }

    @Override
    public SaleLogRepository saleLogRepository() {
        return saleLogRepository;
    }

    @Override
    public EconomyPayoutRouter economyPayoutRouter() {
        return economyPayoutRouter;
    }

    @Override
    public SellSecureStorage secureStorage() {
        return secureStorage;
    }

    @Override
    public ItemNameResolver itemNameResolver() {
        return itemNameResolver;
    }

    @Override
    public PlayerProgress cachedProgress(UUID playerId) {
        return progressCache.getOrDefault(playerId, new PlayerProgress(playerId, 0.0D, Map.of()));
    }

    @Override
    public SellLimitService sellLimitService() {
        return sellLimitService;
    }

    @Override
    public PlayerSellLimitRepository sellLimitRepository() {
        return sellLimitRepository;
    }

    @Override
    public void cacheProgress(PlayerProgress progress) {
        progressCache.put(progress.playerId(), progress);
    }
}
