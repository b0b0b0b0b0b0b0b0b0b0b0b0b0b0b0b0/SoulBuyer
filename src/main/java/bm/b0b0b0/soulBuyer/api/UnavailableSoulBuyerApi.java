package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class UnavailableSoulBuyerApi implements SoulBuyerApi {

    public static final SoulBuyerApi INSTANCE = new UnavailableSoulBuyerApi();

    private UnavailableSoulBuyerApi() {
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void openBuyerMenu(Player player) {
    }

    @Override
    public Optional<ItemUnitQuote> quoteItem(Player player, String itemId) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemUnitQuote> quoteStack(Player player, ItemStack itemStack) {
        return Optional.empty();
    }

    @Override
    public double marketCoefficient(String itemId) {
        return 1.0D;
    }

    @Override
    public double cachedPoints(Player player) {
        return 0.0D;
    }

    @Override
    public double cachedMultiplier(Player player) {
        return 1.0D;
    }

    @Override
    public double cachedCategoryXp(Player player, String categoryId) {
        return 0.0D;
    }

    @Override
    public CompletableFuture<PlayerProgress> fetchProgress(UUID playerId) {
        return CompletableFuture.completedFuture(new PlayerProgress(playerId, 0.0D, java.util.Map.of()));
    }

    @Override
    public boolean isSellable(String itemId) {
        return false;
    }

    @Override
    public boolean isInActiveCatalog(String itemId) {
        return false;
    }

    @Override
    public int activeCatalogSize() {
        return 0;
    }

    @Override
    public boolean isAutosellFeatureEnabled() {
        return false;
    }

    @Override
    public boolean canUseAutosell(Player player) {
        return false;
    }

    @Override
    public boolean isAutosellEnabled(Player player) {
        return false;
    }

    @Override
    public boolean isSaleInProgress(Player player) {
        return false;
    }

    @Override
    public boolean sellAll(Player player) {
        return false;
    }

    @Override
    public boolean sellAll(Player player, Runnable onComplete) {
        return false;
    }

    @Override
    public boolean sellAll(Player player, SoulBuyerSellDelivery delivery, Runnable onComplete) {
        return false;
    }

    @Override
    public boolean sellItem(Player player, String itemId) {
        return false;
    }

    @Override
    public boolean sellItem(Player player, String itemId, Runnable onComplete) {
        return false;
    }

    @Override
    public boolean sellItemAmount(Player player, String itemId, int amount, Runnable onComplete) {
        return false;
    }

    @Override
    public Optional<String> categoryId(String itemId) {
        return Optional.empty();
    }

    @Override
    public Optional<String> categoryId(ItemStack itemStack) {
        return Optional.empty();
    }

    @Override
    public boolean sellStacks(Player player, List<ItemStack> stacks) {
        return false;
    }

    @Override
    public boolean sellStacks(Player player, List<ItemStack> stacks, Runnable onComplete) {
        return false;
    }

    @Override
    public boolean sellStacks(
            Player player,
            List<ItemStack> stacks,
            SoulBuyerSellReturnPolicy returnPolicy,
            SoulBuyerSellDelivery delivery,
            Runnable onComplete
    ) {
        return false;
    }
}
