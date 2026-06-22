package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
}
