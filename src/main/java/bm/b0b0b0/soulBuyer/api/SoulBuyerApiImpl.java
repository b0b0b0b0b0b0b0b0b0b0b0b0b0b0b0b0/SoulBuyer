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
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        return itemRegistry.resolve(itemId).map(value -> sellService.unitQuote(player, value));
    }

    @Override
    public Optional<ItemUnitQuote> quoteStack(Player player, ItemStack itemStack) {
        if (!isReady() || player == null || ItemStacks.isAbsent(itemStack)) {
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
        return sellService.displayMultiplier(player);
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
            return CompletableFuture.completedFuture(new PlayerProgress(playerId, 0.0D, Map.of()));
        }
        return playerProgressRepository.find(playerId).whenComplete((progress, throwable) -> {
            if (progress != null) {
                sellService.warmProgressCache(progress);
            }
        });
    }

    @Override
    public boolean isSellable(String itemId) {
        return itemRegistry.existsInPool(itemId);
    }

    @Override
    public boolean isInActiveCatalog(String itemId) {
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

    @Override
    public boolean isSaleInProgress(Player player) {
        return isReady() && player != null && sellService.isProcessing(player.getUniqueId());
    }

    @Override
    public boolean sellAll(Player player) {
        return sellAll(player, null);
    }

    @Override
    public boolean sellAll(Player player, Runnable onComplete) {
        return sellAll(player, SoulBuyerSellDelivery.CHAT, onComplete);
    }

    @Override
    public boolean sellAll(Player player, SoulBuyerSellDelivery delivery, Runnable onComplete) {
        if (!canSell(player)) {
            return false;
        }
        sellService.sellFromInventory(
                player,
                definition -> true,
                delivery.toInternal(),
                onComplete,
                true
        );
        return true;
    }

    @Override
    public boolean sellItem(Player player, String itemId) {
        return sellItem(player, itemId, null);
    }

    @Override
    public boolean sellItem(Player player, String itemId, Runnable onComplete) {
        if (!canSell(player) || !itemRegistry.existsInPool(itemId)) {
            return false;
        }
        sellService.sellItemFromInventory(player, itemId, onComplete);
        return true;
    }

    @Override
    public boolean sellItemAmount(Player player, String itemId, int amount, Runnable onComplete) {
        if (!canSell(player) || !itemRegistry.existsInPool(itemId) || amount <= 0) {
            return false;
        }
        sellService.sellItemAmountFromInventory(player, itemId, amount, onComplete);
        return true;
    }

    @Override
    public Optional<String> categoryId(String itemId) {
        return itemRegistry.resolve(itemId)
                .map(SellableItemDefinition::categoryId)
                .filter(category -> category != null && !category.isBlank());
    }

    @Override
    public Optional<String> categoryId(ItemStack itemStack) {
        if (!isReady() || ItemStacks.isAbsent(itemStack)) {
            return Optional.empty();
        }
        return itemRegistry.findInPool(itemStack)
                .map(SellableItemDefinition::categoryId)
                .filter(category -> category != null && !category.isBlank());
    }

    @Override
    public boolean sellStacks(Player player, List<ItemStack> stacks) {
        return sellStacks(player, stacks, null);
    }

    @Override
    public boolean sellStacks(Player player, List<ItemStack> stacks, Runnable onComplete) {
        return sellStacks(player, stacks, SoulBuyerSellReturnPolicy.RETURN_TO_PLAYER, SoulBuyerSellDelivery.CHAT, onComplete);
    }

    @Override
    public boolean sellStacks(
            Player player,
            List<ItemStack> stacks,
            SoulBuyerSellReturnPolicy returnPolicy,
            SoulBuyerSellDelivery delivery,
            Runnable onComplete
    ) {
        if (!canSell(player) || stacks == null || stacks.isEmpty()) {
            return false;
        }
        SoulBuyerSellReturnPolicy policy = returnPolicy == null
                ? SoulBuyerSellReturnPolicy.RETURN_TO_PLAYER
                : returnPolicy;
        SoulBuyerSellDelivery mode = delivery == null ? SoulBuyerSellDelivery.CHAT : delivery;
        boolean restoreFailed = policy != SoulBuyerSellReturnPolicy.CALLER_OWNS_ITEMS;
        return sellService.sellProvidedStacks(player, stacks, restoreFailed, mode.toInternal(), onComplete);
    }

    private boolean canSell(Player player) {
        return isReady() && player != null && !isSaleInProgress(player);
    }
}
