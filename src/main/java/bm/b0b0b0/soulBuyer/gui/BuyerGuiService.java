package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.service.SellService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerGuiService implements BuyerMenuNavigation {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private final ItemNameResolver itemNameResolver;
    private final BuyerMenuItemRenderer itemRenderer;
    private final SellService sellService;
    private final BuyerStatsService buyerStatsService;
    private final AutosellService autosellService;
    private final BoosterService boosterService;
    private final SoulBuyerDebugLog debug;

    public BuyerGuiService(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemRegistry itemRegistry,
            ItemNameResolver itemNameResolver,
            BuyerMenuItemRenderer itemRenderer,
            SellService sellService,
            BuyerStatsService buyerStatsService,
            AutosellService autosellService,
            BoosterService boosterService,
            SoulBuyerDebugLog debug
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.itemRegistry = itemRegistry;
        this.itemNameResolver = itemNameResolver;
        this.itemRenderer = itemRenderer;
        this.sellService = sellService;
        this.buyerStatsService = buyerStatsService;
        this.autosellService = autosellService;
        this.boosterService = boosterService;
        this.debug = debug;
    }

    public void open(Player player) {
        openBuyer(player, BuyerMenuSession.withPayout(config.defaultOpenPayoutMode()));
    }

    public void openDonateBuyer(Player player) {
        if (!config.donateBuyerActive()) {
            return;
        }
        openBuyer(player, BuyerMenuSession.withPayout(BuyerPayoutMode.PLAYER_POINTS));
    }

    @Override
    public void openBuyer(Player player, BuyerMenuSession session) {
        debug.log("GUI open buyer for " + player.getName() + " items=" + itemRegistry.all().size());
        autosellService.trySellOnBuyerOpen(player, () -> openBuyerMenu(player, session));
    }

    private void openBuyerMenu(Player player, BuyerMenuSession session) {
        BuyerMenu menu = new BuyerMenu(
                plugin,
                player,
                config,
                messageService,
                itemFactory,
                itemRegistry,
                itemNameResolver,
                itemRenderer,
                sellService,
                buyerStatsService,
                autosellService,
                boosterService,
                this,
                session
        );
        player.openInventory(menu.getInventory());
    }

    @Override
    public void openBoosters(Player player, BuyerMenuSession session) {
        debug.log("GUI open boosters for " + player.getName());
        BuyerBoostersMenu menu = new BuyerBoostersMenu(
                plugin,
                player,
                config,
                messageService,
                itemFactory,
                itemNameResolver,
                boosterService,
                this,
                session
        );
        player.openInventory(menu.getInventory());
    }

    @Override
    public void openQuantity(Player player, String itemId, BuyerMenuSession session) {
        debug.log("GUI open quantity for " + player.getName() + " item=" + itemId);
        BuyerQuantityMenu menu = new BuyerQuantityMenu(
                plugin,
                player,
                config,
                messageService,
                itemFactory,
                itemRegistry,
                itemNameResolver,
                itemRenderer,
                sellService,
                this,
                itemId,
                session
        );
        player.openInventory(menu.getInventory());
    }

    @Override
    public void openAutosell(Player player, BuyerMenuSession session) {
        debug.log("GUI open autosell for " + player.getName());
        BuyerAutosellMenu menu = new BuyerAutosellMenu(
                plugin,
                player,
                config,
                messageService,
                itemFactory,
                autosellService,
                this,
                session
        );
        player.openInventory(menu.getInventory());
    }
}
