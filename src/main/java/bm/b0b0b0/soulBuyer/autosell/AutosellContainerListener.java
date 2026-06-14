package bm.b0b0b0.soulBuyer.autosell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public final class AutosellContainerListener implements Listener {

    private final AutosellService autosellService;

    public AutosellContainerListener(AutosellService autosellService) {
        this.autosellService = autosellService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!AutosellInventoryGuard.isStorageContainer(event.getInventory())) {
            return;
        }
        autosellService.trySellChestOnOpen(player, event.getInventory());
    }
}
