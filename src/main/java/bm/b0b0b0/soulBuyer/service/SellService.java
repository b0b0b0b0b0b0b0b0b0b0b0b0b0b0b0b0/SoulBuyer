package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.autosell.AutosellInventoryGuard;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.integration.EconomyPayoutRouter;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.market.PriceQuoteService;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import bm.b0b0b0.soulBuyer.model.SellLine;
import bm.b0b0b0.soulBuyer.model.SellLimitSplit;
import bm.b0b0b0.soulBuyer.model.SellQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import bm.b0b0b0.soulBuyer.repository.PlayerProgressRepository;
import bm.b0b0b0.soulBuyer.repository.SaleLogRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.UUID;
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class SellService {

    private final JavaPlugin plugin;
    private final PluginContext context;
    private final SoulBuyerDebugLog debug;

    public SellService(JavaPlugin plugin, PluginContext context, SoulBuyerDebugLog debug) {
        this.plugin = plugin;
        this.context = context;
        this.debug = debug;
    }

    public SellQuote preview(Player player, List<ItemStack> stacks) {
        return context.priceQuoteService().quote(
                player,
                stacks,
                context.cachedProgress(player.getUniqueId())
        );
    }

    public ItemUnitQuote unitQuote(Player player, SellableItemDefinition definition) {
        return context.priceQuoteService().unitQuote(
                player,
                definition,
                context.cachedProgress(player.getUniqueId())
        );
    }

    public void sellAllFromInventory(Player player, Runnable onComplete) {
        sellFromInventory(player, definition -> true, onComplete);
    }

    public void sellAllFromInventory(Player player, BuyerPayoutMode payoutMode, Runnable onComplete) {
        sellFromInventory(player, definition -> true, SaleDelivery.CHAT, onComplete, true, payoutMode);
    }

    public void sellItemFromInventory(Player player, String itemId, Runnable onComplete) {
        sellItemFromInventory(player, itemId, SaleDelivery.CHAT, onComplete, context.config().defaultOpenPayoutMode());
    }

    public void sellItemFromInventory(
            Player player,
            String itemId,
            BuyerPayoutMode payoutMode,
            Runnable onComplete
    ) {
        sellItemFromInventory(player, itemId, SaleDelivery.CHAT, onComplete, payoutMode);
    }

    public void sellItemAmountFromInventory(Player player, String itemId, int amount, Runnable onComplete) {
        sellItemAmountFromInventory(player, itemId, amount, SaleDelivery.CHAT, onComplete);
    }

    public void sellItemAmountFromInventory(
            Player player,
            String itemId,
            int amount,
            SaleDelivery delivery,
            Runnable onComplete
    ) {
        sellItemAmountFromInventory(player, itemId, amount, delivery, onComplete, context.config().defaultOpenPayoutMode());
    }

    public void sellItemAmountFromInventory(
            Player player,
            String itemId,
            int amount,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        UUID playerId = player.getUniqueId();
        if (!beginSaleSession(player, onComplete)) {
            return;
        }
        if (isLimitExhaustedForItem(player, itemId)) {
            abortLockedSale(playerId, onComplete, true, "sell.limit-reached");
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAmount(player, context.itemRegistry(), itemId, amount);
        startCollectedSale(player, collected, delivery, onComplete, payoutMode, true);
    }

    public void sellFromContainer(
            Player player,
            Inventory container,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete
    ) {
        sellFromContainer(player, container, filter, delivery, onComplete, context.config().autosellPayoutMode());
    }

    private void sellItemFromInventory(
            Player player,
            String itemId,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        sellFromInventory(
                player,
                definition -> definition.id().equals(itemId),
                delivery,
                onComplete,
                true,
                payoutMode
        );
    }

    public boolean isProcessing(UUID playerId) {
        return context.secureStorage().isProcessing(playerId);
    }

    public void sellFromInventory(Player player, Predicate<SellableItemDefinition> filter, Runnable onComplete) {
        sellFromInventory(player, filter, SaleDelivery.CHAT, onComplete);
    }

    public void sellFromInventory(
            Player player,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete
    ) {
        sellFromInventory(player, filter, delivery, onComplete, true);
    }

    public void sellFromInventory(
            Player player,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete,
            boolean notifyWhenEmpty
    ) {
        sellFromInventory(player, filter, delivery, onComplete, notifyWhenEmpty, context.config().defaultOpenPayoutMode());
    }

    public void sellFromInventory(
            Player player,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete,
            boolean notifyWhenEmpty,
            BuyerPayoutMode payoutMode
    ) {
        if (!beginSaleSession(player, onComplete)) {
            return;
        }
        if (isLimitExhaustedForFilter(player, filter)) {
            abortLockedSale(player.getUniqueId(), onComplete, true, "sell.limit-reached");
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAndRemove(player, context.itemRegistry(), filter);
        startCollectedSale(player, collected, delivery, onComplete, payoutMode, notifyWhenEmpty);
    }

    public void sellFromContainer(
            Player player,
            Inventory container,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        if (!beginSaleSession(player, onComplete)) {
            return;
        }
        if (!AutosellInventoryGuard.isStorageContainer(container)) {
            abortLockedSale(player.getUniqueId(), onComplete, false, null);
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAndRemove(container, context.itemRegistry(), filter);
        startCollectedSale(player, collected, delivery, onComplete, payoutMode, false);
    }

    public void beginSecuredSale(Player player, List<ItemStack> sellStacks, List<ItemStack> returnStacks, Runnable onComplete) {
        beginSecuredSale(player, sellStacks, returnStacks, SaleDelivery.CHAT, onComplete);
    }

    public void beginSecuredSale(
            Player player,
            List<ItemStack> sellStacks,
            List<ItemStack> returnStacks,
            SaleDelivery delivery,
            Runnable onComplete
    ) {
        beginSecuredSale(
                player,
                sellStacks,
                returnStacks,
                delivery,
                onComplete,
                context.config().defaultOpenPayoutMode()
        );
    }

    public void beginSecuredSale(
            Player player,
            List<ItemStack> sellStacks,
            List<ItemStack> returnStacks,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        beginSecuredSale(player, sellStacks, returnStacks, true, delivery, onComplete, payoutMode);
    }

    public void beginSecuredSale(
            Player player,
            List<ItemStack> sellStacks,
            List<ItemStack> returnStacks,
            boolean restoreFailedItemsToPlayer,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        UUID playerId = player.getUniqueId();
        if (!context.secureStorage().tryBegin(playerId)) {
            context.messageService().send(player, "sell.in-progress");
            if (restoreFailedItemsToPlayer) {
                ItemReturner.returnItems(player, sellStacks);
                ItemReturner.returnItems(player, returnStacks);
            }
            complete(onComplete);
            return;
        }
        context.secureStorage().replaceSecuredItems(
                playerId,
                concat(cloneStacks(sellStacks), cloneStacks(returnStacks))
        );
        continueSecuredSale(
                player,
                sellStacks,
                returnStacks,
                restoreFailedItemsToPlayer,
                delivery,
                onComplete,
                payoutMode
        );
    }

    private void continueSecuredSale(
            Player player,
            List<ItemStack> sellStacks,
            List<ItemStack> returnStacks,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        continueSecuredSale(player, sellStacks, returnStacks, true, delivery, onComplete, payoutMode);
    }

    private void continueSecuredSale(
            Player player,
            List<ItemStack> sellStacks,
            List<ItemStack> returnStacks,
            boolean restoreFailedItemsToPlayer,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        BuyerPayoutMode mode = payoutMode == null ? context.config().defaultOpenPayoutMode() : payoutMode;
        UUID playerId = player.getUniqueId();
        if (!context.economyPayoutRouter().available(mode)) {
            context.messageService().send(player, noEconomyKey(mode));
            restoreSecuredItems(player, playerId, restoreFailedItemsToPlayer);
            complete(onComplete);
            return;
        }
        debug.log("sell begin " + player.getName() + " stacks=" + sellStacks.size() + " return=" + returnStacks.size());

        String periodKey = context.sellLimitService().periodKey();
        context.playerProgressRepository().find(playerId).thenCompose(progress -> {
            context.cacheProgress(progress);
            return context.sellLimitRepository().find(playerId, periodKey).thenApply(usage -> {
                context.cacheSellLimitUsage(usage);
                SellLimitSplit split = context.sellLimitService().split(player, sellStacks, usage);
                SellQuote quote = context.priceQuoteService().quote(player, split.sellStacks(), progress);
                return new PreparedSale(split, quote);
            });
        }).thenAccept(prepared -> runMain(() -> {
            if (prepared.quote().lines().isEmpty()) {
                restoreSecuredItems(player, playerId, restoreFailedItemsToPlayer);
                if (player.isOnline()) {
                    String key = prepared.split().sellStacks().isEmpty() && !sellStacks.isEmpty()
                            ? "sell.limit-reached"
                            : "sell.empty";
                    context.messageService().send(player, key);
                }
                complete(onComplete);
                return;
            }
            List<ItemStack> mergedReturn = concat(returnStacks, prepared.split().returnStacks());
            finalizeSale(
                    player,
                    prepared.quote(),
                    mergedReturn,
                    restoreFailedItemsToPlayer,
                    delivery,
                    onComplete,
                    mode,
                    periodKey
            );
        })).exceptionally(throwable -> {
            debug.warn("sell async failed for " + player.getName() + ": " + throwable.getMessage());
            runMain(() -> {
                restoreSecuredItems(player, playerId, restoreFailedItemsToPlayer);
                complete(onComplete);
            });
            return null;
        });
    }

    public void preloadSellLimitUsage(Player player) {
        if (!context.sellLimitService().enabled()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        String periodKey = context.sellLimitService().periodKey();
        context.sellLimitRepository().find(playerId, periodKey).thenAccept(usage ->
                runMain(() -> context.cacheSellLimitUsage(usage))
        );
    }

    private boolean isLimitExhaustedForFilter(Player player, Predicate<SellableItemDefinition> filter) {
        PlayerSellLimitUsage usage = currentSellLimitUsage(player);
        if (usage == null) {
            return false;
        }
        return !context.sellLimitService().hasSellCapacity(player, usage, filter);
    }

    private boolean isLimitExhaustedForItem(Player player, String itemId) {
        PlayerSellLimitUsage usage = currentSellLimitUsage(player);
        if (usage == null) {
            return false;
        }
        return !context.sellLimitService().hasSellCapacity(player, usage, itemId);
    }

    private void complete(Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void restoreSecuredItems(Player player, UUID playerId, boolean restoreToPlayer) {
        ItemReturner.returnSecured(player, context.secureStorage().tryAbort(playerId), restoreToPlayer);
    }

    private void cancelFinalize(Player player, UUID playerId, boolean restoreToPlayer) {
        ItemReturner.returnSecured(player, context.secureStorage().cancelFinalize(playerId), restoreToPlayer);
    }

    private boolean beginSaleSession(Player player, Runnable onComplete) {
        if (context.secureStorage().tryBegin(player.getUniqueId())) {
            return true;
        }
        context.messageService().send(player, "sell.in-progress");
        complete(onComplete);
        return false;
    }

    private void abortLockedSale(UUID playerId, Runnable onComplete, boolean notify, String messageKey) {
        context.secureStorage().cancelLock(playerId);
        if (notify && messageKey != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                context.messageService().send(player, messageKey);
            }
        }
        complete(onComplete);
    }

    private void startCollectedSale(
            Player player,
            List<ItemStack> collected,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode,
            boolean notifyWhenEmpty
    ) {
        UUID playerId = player.getUniqueId();
        if (collected.isEmpty()) {
            abortLockedSale(playerId, onComplete, notifyWhenEmpty, "sell.empty");
            return;
        }
        context.secureStorage().replaceSecuredItems(playerId, collected);
        continueSecuredSale(player, collected, List.of(), delivery, onComplete, payoutMode);
    }

    private PlayerSellLimitUsage currentSellLimitUsage(Player player) {
        if (!context.sellLimitService().enabled()) {
            return null;
        }
        PlayerSellLimitUsage usage = context.cachedSellLimitUsage(player.getUniqueId());
        if (usage == null || !context.sellLimitService().periodKey().equals(usage.periodKey())) {
            return null;
        }
        return usage;
    }

    private void applySellLimitCache(UUID playerId, String periodKey, List<SellLine> lines) {
        PlayerSellLimitUsage cached = context.cachedSellLimitUsage(playerId);
        Map<String, Integer> sold = cached != null && periodKey.equals(cached.periodKey())
                ? new HashMap<>(cached.soldByItemId())
                : new HashMap<>();
        for (SellLine line : lines) {
            sold.merge(line.itemId(), line.amount(), Integer::sum);
        }
        context.cacheSellLimitUsage(new PlayerSellLimitUsage(playerId, periodKey, sold));
    }

    private record PreparedSale(SellLimitSplit split, SellQuote quote) {
    }

    private void finalizeSale(
            Player player,
            SellQuote quote,
            List<ItemStack> returnStacks,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode,
            String periodKey
    ) {
        finalizeSale(player, quote, returnStacks, true, delivery, onComplete, payoutMode, periodKey);
    }

    private void finalizeSale(
            Player player,
            SellQuote quote,
            List<ItemStack> returnStacks,
            boolean restoreFailedItemsToPlayer,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode,
            String periodKey
    ) {
        UUID playerId = player.getUniqueId();
        if (!context.secureStorage().tryEnterFinalize(playerId)) {
            debug.log("sell finalize aborted (session gone) " + player.getName());
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (!player.isOnline()) {
            cancelFinalize(player, playerId, restoreFailedItemsToPlayer);
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (!context.progressionService().isValidPayout(quote.totalMoney())) {
            debug.warn("sell payout rejected for " + player.getName() + " money=" + quote.totalMoney());
            cancelFinalize(player, playerId, restoreFailedItemsToPlayer);
            context.messageService().send(player, "sell.payout-limit");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        boolean paid = context.economyPayoutRouter().deposit(player, payoutMode, quote.totalMoney());
        if (!paid) {
            debug.warn("sell deposit failed for " + player.getName() + " money=" + quote.totalMoney());
            cancelFinalize(player, playerId, restoreFailedItemsToPlayer);
            context.messageService().send(player, "sell.failed");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Map<String, Double> categoryXpDelta = new HashMap<>();
        for (SellLine line : quote.lines()) {
            context.marketService().recordSale(line.itemId(), line.amount());
            Optional<SellableItemDefinition> definition = context.itemRegistry().byId(line.itemId());
            definition.ifPresent(def ->
                    context.progressionService().categoryXpDelta(def, line.totalPoints())
                            .forEach((key, value) -> categoryXpDelta.merge(key, value, Double::sum))
            );
        }

        context.playerProgressRepository().addPointsAndCategoryXp(playerId, quote.totalPoints(), categoryXpDelta)
                .thenRun(() -> context.playerProgressRepository().find(playerId).thenAccept(context::cacheProgress));

        context.saleLogRepository().enqueue(playerId, context.config().serverId(), quote.lines(), quote.totalMoney(), quote.totalPoints());
        context.buyerStatsService().recordSale(playerId, quote.totalMoney(), quote.totalPoints(), quote.lines().size());
        context.sellLimitRepository().recordSale(playerId, periodKey, quote.lines());
        applySellLimitCache(playerId, periodKey, quote.lines());

        context.secureStorage().commitFinalize(playerId);
        ItemReturner.returnItems(player, returnStacks);
        debug.log("sell OK " + player.getName() + " money=" + quote.totalMoney()
                + " points=" + quote.totalPoints() + " lines=" + quote.lines().size());

        deliverSaleMessage(player, quote, delivery, payoutMode);
        if (!returnStacks.isEmpty() && delivery != SaleDelivery.SILENT) {
            context.messageService().send(player, "sell.partial");
        }
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void deliverSaleMessage(Player player, SellQuote quote, SaleDelivery delivery, BuyerPayoutMode payoutMode) {
        if (delivery == SaleDelivery.SILENT) {
            return;
        }
        String key = switch (delivery) {
            case ACTION_BAR -> payoutMode == BuyerPayoutMode.PLAYER_POINTS
                    ? "autosell.success-actionbar-playerpoints"
                    : "autosell.success-actionbar";
            case AUTOSell_CHAT -> payoutMode == BuyerPayoutMode.PLAYER_POINTS
                    ? "autosell.success-playerpoints"
                    : "autosell.success";
            default -> payoutMode == BuyerPayoutMode.PLAYER_POINTS
                    ? "sell.success-playerpoints"
                    : "sell.success";
        };
        String[] pairs = new String[]{
                "count", String.valueOf(quote.lines().size()),
                "money", context.itemNameResolver().formatMoney(quote.totalMoney()),
                "points", context.itemNameResolver().formatMoney(quote.totalPoints())
        };
        if (delivery == SaleDelivery.ACTION_BAR) {
            context.messageService().sendActionBar(player, key, pairs);
            return;
        }
        context.messageService().send(player, key, pairs);
    }

    public void abortAndReturn(Player player) {
        ItemReturner.returnSecured(
                player,
                context.secureStorage().tryAbort(player.getUniqueId()),
                true
        );
    }

    public void cleanupOnQuit(Player player) {
        abortAndReturn(player);
    }

    public PlayerProgress cachedProgress(Player player) {
        return context.cachedProgress(player.getUniqueId());
    }

    public double displayMultiplier(Player player) {
        PlayerProgress progress = cachedProgress(player);
        double permission = context.progressionService().permissionMultiplier(player);
        double category = context.progressionService().categoryBonus(progress);
        double additive = context.boosterService().additiveMultiplier(player);
        return permission * category + additive;
    }

    public void preloadProgress(Player player) {
        preloadProgress(player, null);
    }

    public void preloadProgress(Player player, Runnable onLoaded) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        context.playerProgressRepository().find(playerId).thenAccept(progress -> runMain(() -> {
            context.cacheProgress(progress);
            if (onLoaded != null) {
                onLoaded.run();
            }
        }));
    }

    public void ensureProgressLoaded(Player player, Runnable onReady) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (context.isProgressHydrated(playerId)) {
            runMain(onReady);
            return;
        }
        preloadProgress(player, onReady);
    }

    public boolean sellProvidedStacks(
            Player player,
            List<ItemStack> stacks,
            boolean restoreFailedItemsToPlayer,
            SaleDelivery delivery,
            Runnable onComplete
    ) {
        return sellProvidedStacks(
                player,
                stacks,
                restoreFailedItemsToPlayer,
                delivery,
                onComplete,
                context.config().defaultOpenPayoutMode()
        );
    }

    public boolean sellProvidedStacks(
            Player player,
            List<ItemStack> stacks,
            boolean restoreFailedItemsToPlayer,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        if (player == null || stacks == null || stacks.isEmpty()) {
            return false;
        }
        List<ItemStack> sellable = InventorySellHelper.filterSellableStacks(context.itemRegistry(), stacks);
        if (sellable.isEmpty()) {
            return false;
        }
        beginSecuredSale(
                player,
                sellable,
                List.of(),
                restoreFailedItemsToPlayer,
                delivery,
                onComplete,
                payoutMode
        );
        return true;
    }

    public void warmProgressCache(PlayerProgress progress) {
        if (progress != null) {
            context.cacheProgress(progress);
        }
    }

    private void runMain(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private List<ItemStack> cloneStacks(List<ItemStack> stacks) {
        List<ItemStack> clones = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (ItemStacks.isPresent(stack)) {
                clones.add(stack.clone());
            }
        }
        return clones;
    }

    private List<ItemStack> concat(List<ItemStack> first, List<ItemStack> second) {
        List<ItemStack> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    private String noEconomyKey(BuyerPayoutMode mode) {
        return mode == BuyerPayoutMode.PLAYER_POINTS ? "sell.no-economy-playerpoints" : "sell.no-economy";
    }

    public interface PluginContext {
        bm.b0b0b0.soulBuyer.config.PluginConfig config();

        MessageService messageService();

        ItemRegistry itemRegistry();

        PriceQuoteService priceQuoteService();

        MarketService marketService();

        ProgressionService progressionService();

        PlayerProgressRepository playerProgressRepository();

        SaleLogRepository saleLogRepository();

        EconomyPayoutRouter economyPayoutRouter();

        SellSecureStorage secureStorage();

        bm.b0b0b0.soulBuyer.item.ItemNameResolver itemNameResolver();

        bm.b0b0b0.soulBuyer.model.PlayerProgress cachedProgress(UUID playerId);

        void cacheProgress(bm.b0b0b0.soulBuyer.model.PlayerProgress progress);

        boolean isProgressHydrated(UUID playerId);

        bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage cachedSellLimitUsage(UUID playerId);

        void cacheSellLimitUsage(bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage usage);

        BuyerStatsService buyerStatsService();

        bm.b0b0b0.soulBuyer.limit.SellLimitService sellLimitService();

        bm.b0b0b0.soulBuyer.repository.PlayerSellLimitRepository sellLimitRepository();

        bm.b0b0b0.soulBuyer.booster.BoosterService boosterService();
    }
}
