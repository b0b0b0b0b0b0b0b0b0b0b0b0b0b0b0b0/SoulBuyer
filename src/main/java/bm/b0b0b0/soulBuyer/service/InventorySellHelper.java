package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class InventorySellHelper {

    private InventorySellHelper() {
    }

    public static int countMatching(Player player, ItemRegistry itemRegistry, String itemId) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definition = itemRegistry.findInPool(stack);
            if (definition.isPresent() && definition.get().id().equals(itemId)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public static List<ItemStack> collectAmount(
            Player player,
            ItemRegistry itemRegistry,
            String itemId,
            int amount
    ) {
        if (amount <= 0) {
            return List.of();
        }
        int remaining = amount;
        List<ItemStack> collected = new ArrayList<>();
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int index = 0; index < contents.length && remaining > 0; index++) {
            ItemStack stack = contents[index];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definition = itemRegistry.findInPool(stack);
            if (definition.isEmpty() || !definition.get().id().equals(itemId)) {
                continue;
            }
            int take = Math.min(remaining, stack.getAmount());
            ItemStack taken = stack.clone();
            taken.setAmount(take);
            collected.add(taken);
            if (take >= stack.getAmount()) {
                contents[index] = null;
            } else {
                stack.setAmount(stack.getAmount() - take);
            }
            remaining -= take;
        }
        player.getInventory().setStorageContents(contents);
        return collected;
    }

    public static List<ItemStack> collectAndRemove(
            Player player,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        List<ItemStack> collected = new ArrayList<>();
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int index = 0; index < contents.length; index++) {
            ItemStack stack = contents[index];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definition = itemRegistry.findInPool(stack);
            if (definition.isEmpty() || !filter.test(definition.get())) {
                continue;
            }
            collected.add(stack.clone());
            contents[index] = null;
        }
        player.getInventory().setStorageContents(contents);
        return collected;
    }

    public static List<ItemStack> collectAndRemove(
            Inventory inventory,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        List<ItemStack> collected = new ArrayList<>();
        if (inventory == null) {
            return collected;
        }
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; index < contents.length; index++) {
            ItemStack stack = contents[index];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definition = itemRegistry.findInPool(stack);
            if (definition.isEmpty() || !filter.test(definition.get())) {
                continue;
            }
            collected.add(stack.clone());
            inventory.setItem(index, null);
        }
        return collected;
    }
}
