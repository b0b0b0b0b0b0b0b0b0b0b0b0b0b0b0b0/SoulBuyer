package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BuyerMenuLiveStatsUpdater {

    private final JavaPlugin plugin;
    private final Player player;
    private final BuyerStatsService buyerStatsService;
    private final GuiItemFactory itemFactory;
    private final Inventory inventory;
    private final GuiGeneralSettings.GuiElementSettings statsElement;
    private final int statsSlot;

    private BukkitTask task;

    public BuyerMenuLiveStatsUpdater(
            JavaPlugin plugin,
            Player player,
            BuyerStatsService buyerStatsService,
            GuiItemFactory itemFactory,
            Inventory inventory,
            GuiGeneralSettings.GuiElementSettings statsElement,
            int statsSlot
    ) {
        this.plugin = plugin;
        this.player = player;
        this.buyerStatsService = buyerStatsService;
        this.itemFactory = itemFactory;
        this.inventory = inventory;
        this.statsElement = statsElement;
        this.statsSlot = statsSlot;
    }

    public void start() {
        if (statsElement == null) {
            return;
        }
        buyerStatsService.preloadDaily(player);
        apply();
        if (task != null) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::apply, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void apply() {
        if (!player.isOnline()) {
            stop();
            return;
        }
        if (!(player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerMenu)) {
            stop();
            return;
        }
        inventory.setItem(statsSlot, itemFactory.build(player, statsElement, buyerStatsService.guiPairs(player)));
    }
}
