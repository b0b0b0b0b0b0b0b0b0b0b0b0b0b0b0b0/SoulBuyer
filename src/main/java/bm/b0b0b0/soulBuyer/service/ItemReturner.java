package bm.b0b0b0.soulBuyer.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ItemReturner {

    private ItemReturner() {
    }

    public static void returnItems(Player player, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            give(player, stack);
        }
    }

    public static void give(Player player, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
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
