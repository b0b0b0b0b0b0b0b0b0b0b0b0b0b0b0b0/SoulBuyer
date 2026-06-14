package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.gui.BuyerAutosellMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerQuantityMenu;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AutosellInventoryGuard {

    private AutosellInventoryGuard() {
    }

    public static boolean isPluginGui(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder(false);
        return holder instanceof BuyerMenu
                || holder instanceof BuyerQuantityMenu
                || holder instanceof BuyerAutosellMenu;
    }

    public static boolean isStorageContainer(Inventory inventory) {
        if (isPluginGui(inventory)) {
            return false;
        }
        InventoryType type = inventory.getType();
        return type == InventoryType.CHEST
                || type == InventoryType.ENDER_CHEST
                || type == InventoryType.BARREL
                || type == InventoryType.SHULKER_BOX
                || type == InventoryType.HOPPER
                || type == InventoryType.DROPPER
                || type == InventoryType.DISPENSER;
    }
}
