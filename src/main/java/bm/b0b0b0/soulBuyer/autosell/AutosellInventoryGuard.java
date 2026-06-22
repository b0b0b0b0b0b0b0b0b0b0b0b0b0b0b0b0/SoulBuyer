package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.gui.BuyerAutosellMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerBoostersMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerQuantityMenu;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AutosellInventoryGuard {

    private AutosellInventoryGuard() {
    }

    public static boolean isPluginGui(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        return isSoulBuyerGui(inventory.getHolder(false));
    }

    public static boolean isStorageContainer(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        if (isPluginGui(inventory)) {
            return false;
        }
        if (!isSupportedStorageType(inventory.getType())) {
            return false;
        }
        return isWorldStorageHolder(inventory.getHolder(false));
    }

    private static boolean isSoulBuyerGui(InventoryHolder holder) {
        return holder instanceof BuyerMenu
                || holder instanceof BuyerQuantityMenu
                || holder instanceof BuyerAutosellMenu
                || holder instanceof BuyerBoostersMenu;
    }

    private static boolean isWorldStorageHolder(InventoryHolder holder) {
        if (holder == null) {
            return false;
        }
        if (holder instanceof DoubleChest) {
            return true;
        }
        if (holder instanceof StorageMinecart || holder instanceof HopperMinecart) {
            return true;
        }
        if (holder instanceof Container container) {
            if (container instanceof BlockState blockState) {
                return blockState.getWorld() != null;
            }
            return true;
        }
        return false;
    }

    private static boolean isSupportedStorageType(InventoryType type) {
        return type == InventoryType.CHEST
                || type == InventoryType.ENDER_CHEST
                || type == InventoryType.BARREL
                || type == InventoryType.SHULKER_BOX
                || type == InventoryType.HOPPER
                || type == InventoryType.DROPPER
                || type == InventoryType.DISPENSER;
    }
}
