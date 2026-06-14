package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.integration.EconomyPayoutRouter;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.market.PriceQuoteService;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellLine;
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
        if (isProcessing(player.getUniqueId())) {
            context.messageService().send(player, "sell.in-progress");
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAmount(player, context.itemRegistry(), itemId, amount);
        if (collected.isEmpty()) {
            context.messageService().send(player, "sell.empty");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        beginSecuredSale(player, collected, List.of(), delivery, onComplete, payoutMode);
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
        if (isProcessing(player.getUniqueId())) {
            context.messageService().send(player, "sell.in-progress");
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAndRemove(player, context.itemRegistry(), filter);
        if (collected.isEmpty()) {
            if (notifyWhenEmpty) {
                context.messageService().send(player, "sell.empty");
            }
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        beginSecuredSale(player, collected, List.of(), delivery, onComplete, payoutMode);
    }

    public void sellFromContainer(
            Player player,
            Inventory container,
            Predicate<SellableItemDefinition> filter,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
    ) {
        if (isProcessing(player.getUniqueId())) {
            context.messageService().send(player, "sell.in-progress");
            return;
        }
        List<ItemStack> collected = InventorySellHelper.collectAndRemove(container, context.itemRegistry(), filter);
        if (collected.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        beginSecuredSale(player, collected, List.of(), delivery, onComplete, payoutMode);
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
        BuyerPayoutMode mode = payoutMode == null ? context.config().defaultOpenPayoutMode() : payoutMode;
        UUID playerId = player.getUniqueId();
        if (context.secureStorage().isProcessing(playerId)) {
            context.messageService().send(player, "sell.in-progress");
            return;
        }
        if (!context.economyPayoutRouter().available(mode)) {
            context.messageService().send(player, noEconomyKey(mode));
            ItemReturner.returnItems(player, sellStacks);
            ItemReturner.returnItems(player, returnStacks);
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        context.secureStorage().markProcessing(playerId, concat(cloneStacks(sellStacks), cloneStacks(returnStacks)));
        debug.log("sell begin " + player.getName() + " stacks=" + sellStacks.size() + " return=" + returnStacks.size());

        context.playerProgressRepository().find(playerId).thenAccept(progress -> {
            context.cacheProgress(progress);
            SellQuote quote = context.priceQuoteService().quote(player, sellStacks, progress);
            if (quote.lines().isEmpty()) {
                runMain(() -> {
                    context.secureStorage().tryAbort(playerId).forEach(stack -> ItemReturner.give(player, stack));
                    if (player.isOnline()) {
                        context.messageService().send(player, "sell.empty");
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
                return;
            }
            runMain(() -> finalizeSale(player, quote, returnStacks, delivery, onComplete, mode));
        });
    }

    private void finalizeSale(
            Player player,
            SellQuote quote,
            List<ItemStack> returnStacks,
            SaleDelivery delivery,
            Runnable onComplete,
            BuyerPayoutMode payoutMode
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
            context.secureStorage().cancelFinalize(playerId).forEach(stack -> ItemReturner.give(player, stack));
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        boolean paid = context.economyPayoutRouter().deposit(player, payoutMode, quote.totalMoney());
        if (!paid) {
            context.secureStorage().cancelFinalize(playerId).forEach(stack -> ItemReturner.give(player, stack));
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
        UUID playerId = player.getUniqueId();
        List<ItemStack> secured = context.secureStorage().tryAbort(playerId);
        for (ItemStack stack : secured) {
            ItemReturner.give(player, stack);
        }
    }

    public void cleanupOnQuit(Player player) {
        abortAndReturn(player);
    }

    public PlayerProgress cachedProgress(Player player) {
        return context.cachedProgress(player.getUniqueId());
    }

    private void runMain(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private List<ItemStack> cloneStacks(List<ItemStack> stacks) {
        List<ItemStack> clones = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.getType().isAir()) {
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

        BuyerStatsService buyerStatsService();
    }
}
