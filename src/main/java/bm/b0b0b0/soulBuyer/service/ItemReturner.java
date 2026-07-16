package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.util.ItemStacks;
import bm.b0b0b0.soulBuyer.util.PluginSchedulers;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class ItemReturner {

    private ItemReturner() {
    }

    public static void returnItems(Player player, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            give(player, stack);
        }
    }

    public static void give(Player player, ItemStack stack) {
        if (ItemStacks.isAbsent(stack)) {
            return;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
    }

    public static void returnSecured(Player player, List<ItemStack> stacks, boolean restoreToPlayer) {
        if (!restoreToPlayer || stacks == null || stacks.isEmpty()) {
            return;
        }
        if (player != null && player.isOnline()) {
            returnItems(player, stacks);
            return;
        }
        if (player != null) {
            Location dropAt = player.getLocation();
            for (ItemStack stack : stacks) {
                if (ItemStacks.isAbsent(stack)) {
                    continue;
                }
                dropAt.getWorld().dropItemNaturally(dropAt, stack);
            }
        }
    }

    public static void returnToContainer(Plugin plugin, Inventory container, List<ItemStack> stacks, Player fallback) {
        if (stacks == null || stacks.isEmpty() || container == null) {
            return;
        }
        Location location = locationOf(container);
        Runnable putBack = () -> {
            for (ItemStack stack : stacks) {
                if (ItemStacks.isAbsent(stack)) {
                    continue;
                }
                Map<Integer, ItemStack> leftover = container.addItem(stack.clone());
                if (leftover.isEmpty()) {
                    continue;
                }
                if (fallback != null && fallback.isOnline()) {
                    PluginSchedulers.run(plugin, fallback, () -> leftover.values().forEach(item -> give(fallback, item)));
                } else if (location != null && location.getWorld() != null) {
                    leftover.values().forEach(item -> location.getWorld().dropItemNaturally(location, item));
                }
            }
        };
        if (location != null) {
            PluginSchedulers.runAt(plugin, location, putBack);
            return;
        }
        putBack.run();
    }

    public static Location locationOf(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryHolder holder = inventory.getHolder(false);
        if (holder instanceof DoubleChest doubleChest) {
            Location location = doubleChest.getLocation();
            return location.getWorld() == null ? null : location;
        }
        if (holder instanceof BlockState blockState) {
            Location location = blockState.getLocation();
            return location.getWorld() == null ? null : location;
        }
        if (holder instanceof Entity entity) {
            return entity.getLocation();
        }
        return null;
    }

    public static void returnItemsOffline(UUID playerId, List<ItemStack> sellStacks, List<ItemStack> returnStacks) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }
        returnItems(player, sellStacks);
        returnItems(player, returnStacks);
    }
}
