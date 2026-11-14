package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface SoulBuyerApi {

    boolean isReady();

    void openBuyerMenu(Player player);

    Optional<ItemUnitQuote> quoteItem(Player player, String itemId);

    Optional<ItemUnitQuote> quoteStack(Player player, ItemStack itemStack);

    double marketCoefficient(String itemId);

    double cachedPoints(Player player);

    double cachedMultiplier(Player player);

    double cachedCategoryXp(Player player, String categoryId);

    CompletableFuture<PlayerProgress> fetchProgress(UUID playerId);

    boolean isSellable(String itemId);

    boolean isInActiveCatalog(String itemId);

    int activeCatalogSize();

    boolean isAutosellFeatureEnabled();

    boolean canUseAutosell(Player player);

    boolean isAutosellEnabled(Player player);

    boolean isSaleInProgress(Player player);

    boolean sellAll(Player player);

    boolean sellAll(Player player, Runnable onComplete);

    boolean sellAll(Player player, SoulBuyerSellDelivery delivery, Runnable onComplete);

    boolean sellItem(Player player, String itemId);

    boolean sellItem(Player player, String itemId, Runnable onComplete);

    boolean sellItemAmount(Player player, String itemId, int amount, Runnable onComplete);

    Optional<String> categoryId(String itemId);

    Optional<String> categoryId(ItemStack itemStack);

    boolean sellStacks(Player player, List<ItemStack> stacks);

    boolean sellStacks(Player player, List<ItemStack> stacks, Runnable onComplete);

    boolean sellStacks(
            Player player,
            List<ItemStack> stacks,
            SoulBuyerSellReturnPolicy returnPolicy,
            SoulBuyerSellDelivery delivery,
            Runnable onComplete
    );
}
