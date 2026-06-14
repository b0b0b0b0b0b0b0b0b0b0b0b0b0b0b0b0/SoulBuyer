package bm.b0b0b0.soulBuyer.autosell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutosellPickupListener implements Listener {

    private final JavaPlugin plugin;
    private final AutosellService autosellService;
    private final int pickupDelayTicks;

    public AutosellPickupListener(JavaPlugin plugin, AutosellService autosellService, int pickupDelayTicks) {
        this.plugin = plugin;
        this.autosellService = autosellService;
        this.pickupDelayTicks = Math.max(0, pickupDelayTicks);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        autosellService.preload(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        autosellService.unload(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        schedulePickup(player, event.getItem().getItemStack().clone());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!(event.getCaught() instanceof org.bukkit.entity.Item itemEntity)) {
            return;
        }
        schedulePickup(event.getPlayer(), itemEntity.getItemStack().clone());
    }

    private void schedulePickup(Player player, ItemStack stack) {
        if (!autosellService.canAccess(player)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> autosellService.onItemAcquired(player, stack),
                pickupDelayTicks
        );
    }
}
