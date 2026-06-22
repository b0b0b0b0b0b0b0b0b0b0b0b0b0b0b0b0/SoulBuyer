package bm.b0b0b0.soulBuyer.listener;

import bm.b0b0b0.soulBuyer.gui.BuyerAutosellMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerBoostersMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerMenu;
import bm.b0b0b0.soulBuyer.gui.BuyerQuantityMenu;
import bm.b0b0b0.soulBuyer.service.SellService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class BuyerInventoryListener implements Listener {

    private final SellService sellService;

    public BuyerInventoryListener(SellService sellService) {
        this.sellService = sellService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder(false);
        if (!(holder instanceof BuyerMenu)
                && !(holder instanceof BuyerQuantityMenu)
                && !(holder instanceof BuyerAutosellMenu)
                && !(holder instanceof BuyerBoostersMenu)) {
            return;
        }
        int topSize = top.getSize();
        int rawSlot = event.getRawSlot();
        if (rawSlot >= topSize) {
            if (shouldBlockPlayerInventoryClick(event)) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (holder instanceof BuyerMenu buyerMenu) {
            if (buyerMenu.isProcessing()) {
                return;
            }
            boolean rightClick = event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT;
            buyerMenu.handleClick(rawSlot, rightClick);
        } else if (holder instanceof BuyerQuantityMenu quantityMenu) {
            if (quantityMenu.isProcessing()) {
                return;
            }
            quantityMenu.handleClick(rawSlot);
        } else if (holder instanceof BuyerAutosellMenu autosellMenu) {
            autosellMenu.handleClick(rawSlot);
        } else if (holder instanceof BuyerBoostersMenu boostersMenu) {
            boostersMenu.handleClick(rawSlot);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder(false);
        if (!(holder instanceof BuyerMenu)
                && !(holder instanceof BuyerQuantityMenu)
                && !(holder instanceof BuyerAutosellMenu)
                && !(holder instanceof BuyerBoostersMenu)) {
            return;
        }
        int topSize = top.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (holder instanceof BuyerMenu menu) {
            menu.onClose();
        } else if (holder instanceof BuyerQuantityMenu quantityMenu) {
            quantityMenu.onClose();
        } else if (holder instanceof BuyerAutosellMenu autosellMenu) {
            autosellMenu.onClose();
        } else if (holder instanceof BuyerBoostersMenu boostersMenu) {
            boostersMenu.onClose();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sellService.cleanupOnQuit(event.getPlayer());
    }

    private boolean shouldBlockPlayerInventoryClick(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            return true;
        }
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return true;
        }
        if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND) {
            return event.getView().getTopInventory().equals(event.getClickedInventory());
        }
        return false;
    }
}
