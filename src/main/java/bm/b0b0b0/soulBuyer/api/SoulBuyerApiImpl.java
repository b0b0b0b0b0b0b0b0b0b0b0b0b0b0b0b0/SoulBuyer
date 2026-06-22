package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.gui.BuyerGuiService;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.repository.PlayerProgressRepository;
import bm.b0b0b0.soulBuyer.service.SellService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public final class SoulBuyerApiImpl implements SoulBuyerApi {

    private final BuyerGuiService buyerGuiService;
    private final ItemRegistry itemRegistry;
    private final MarketService marketService;
    private final SellService sellService;
    private final PlayerProgressRepository playerProgressRepository;
    private final AutosellService autosellService;
    private final BooleanSupplier readyCheck;

    public SoulBuyerApiImpl(
            BuyerGuiService buyerGuiService,
            ItemRegistry itemRegistry,
            MarketService marketService,
            SellService sellService,
            PlayerProgressRepository playerProgressRepository,
            AutosellService autosellService,
            BooleanSupplier readyCheck
    ) {
        this.buyerGuiService = buyerGuiService;
        this.itemRegistry = itemRegistry;
        this.marketService = marketService;
        this.sellService = sellService;
        this.playerProgressRepository = playerProgressRepository;
        this.autosellService = autosellService;
        this.readyCheck = readyCheck;
    }

    @Override
    public boolean isReady() {
        return readyCheck.getAsBoolean();
    }

    @Override
    public void openBuyerMenu(Player player) {
        if (!isReady() || player == null) {
            return;
        }
        buyerGuiService.open(player);
    }

    @Override
    public Optional<ItemUnitQuote> quoteItem(Player player, String itemId) {
        if (!isReady() || player == null || itemId == null || itemId.isBlank()) {
            return Optional.empty();
        }
        Optional<SellableItemDefinition> definition = itemRegistry.byId(itemId);
        if (definition.isEmpty()) {
            definition = itemRegistry.pool().stream()
                    .filter(entry -> entry.id().equals(itemId))
                    .findFirst();
        }
        return definition.map(value -> sellService.unitQuote(player, value));
    }

    @Override
    public Optional<ItemUnitQuote> quoteStack(Player player, ItemStack itemStack) {
        if (!isReady() || player == null || itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }
        return itemRegistry.findInPool(itemStack)
                .map(definition -> sellService.unitQuote(player, definition));
    }

    @Override
    public double marketCoefficient(String itemId) {
        if (!isReady() || itemId == null || itemId.isBlank()) {
            return 1.0D;
        }
        return marketService.coefficient(itemId);
    }

    @Override
    public double cachedPoints(Player player) {
        if (!isReady() || player == null) {
            return 0.0D;
        }
        return sellService.cachedProgress(player).points();
    }

    @Override
    public double cachedMultiplier(Player player) {
        if (!isReady() || player == null) {
            return 1.0D;
        }
        return itemRegistry.all().stream()
                .findFirst()
                .map(definition -> sellService.unitQuote(player, definition).playerMultiplier())
                .orElse(1.0D);
    }

    @Override
    public double cachedCategoryXp(Player player, String categoryId) {
        if (!isReady() || player == null || categoryId == null || categoryId.isBlank()) {
            return 0.0D;
        }
        return sellService.cachedProgress(player).categoryXp().getOrDefault(categoryId, 0.0D);
    }

    @Override
    public CompletableFuture<PlayerProgress> fetchProgress(UUID playerId) {
        if (!isReady() || playerId == null) {
            return CompletableFuture.completedFuture(new PlayerProgress(playerId, 0.0D, java.util.Map.of()));
        }
        return playerProgressRepository.find(playerId);
    }

    @Override
    public boolean isSellable(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        return itemRegistry.pool().stream().anyMatch(definition -> definition.id().equals(itemId));
    }

    @Override
    public boolean isInActiveCatalog(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        return itemRegistry.isActive(itemId);
    }

    @Override
    public int activeCatalogSize() {
        return itemRegistry.activeSize();
    }

    @Override
    public boolean isAutosellFeatureEnabled() {
        return autosellService.featureEnabled();
    }

    @Override
    public boolean canUseAutosell(Player player) {
        return autosellService.canAccess(player);
    }

    @Override
    public boolean isAutosellEnabled(Player player) {
        if (!autosellService.canAccess(player)) {
            return false;
        }
        return autosellService.settings(player).enabled();
    }
}
