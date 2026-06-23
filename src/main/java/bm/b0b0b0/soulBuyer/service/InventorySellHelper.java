package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventorySellHelper {

    public enum CatalogScope {
        ACTIVE,
        POOL
    }

    public record GroupedStacks(
            Map<String, Integer> amounts,
            Map<String, List<ItemStack>> stacksByItemId,
            Map<String, SellableItemDefinition> definitions
    ) {
    }

    private static final Predicate<SellableItemDefinition> ANY = definition -> true;

    private InventorySellHelper() {
    }

    public static int countMatching(Player player, ItemRegistry itemRegistry, String itemId) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (matches(stack, itemRegistry, definition -> definition.id().equals(itemId))) {
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
            if (!matches(stack, itemRegistry, definition -> definition.id().equals(itemId))) {
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
        ItemStack[] contents = player.getInventory().getStorageContents();
        List<ItemStack> collected = collectAndClearSlots(contents, itemRegistry, filter);
        player.getInventory().setStorageContents(contents);
        return collected;
    }

    public static List<ItemStack> collectAndRemove(
            Inventory inventory,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        if (inventory == null) {
            return List.of();
        }
        List<ItemStack> collected = new ArrayList<>();
        for (int index = 0; index < inventory.getSize(); index++) {
            ItemStack stack = inventory.getItem(index);
            if (collectMatch(collected, stack, itemRegistry, filter)) {
                inventory.setItem(index, null);
            }
        }
        return collected;
    }

    public static List<ItemStack> filterSellableStacks(ItemRegistry itemRegistry, List<ItemStack> stacks) {
        List<ItemStack> collected = new ArrayList<>();
        if (stacks == null) {
            return collected;
        }
        for (ItemStack stack : stacks) {
            if (matches(stack, itemRegistry, ANY)) {
                collected.add(stack.clone());
            }
        }
        return collected;
    }

    public static GroupedStacks groupStacks(
            ItemRegistry itemRegistry,
            List<ItemStack> stacks,
            CatalogScope scope
    ) {
        Map<String, Integer> amounts = new HashMap<>();
        Map<String, List<ItemStack>> stacksByItemId = new HashMap<>();
        Map<String, SellableItemDefinition> definitions = new HashMap<>();
        if (stacks == null) {
            return new GroupedStacks(amounts, stacksByItemId, definitions);
        }
        for (ItemStack stack : stacks) {
            Optional<SellableItemDefinition> definition = definition(stack, itemRegistry, scope, ANY);
            if (definition.isEmpty()) {
                continue;
            }
            String itemId = definition.get().id();
            amounts.merge(itemId, stack.getAmount(), Integer::sum);
            stacksByItemId.computeIfAbsent(itemId, ignored -> new ArrayList<>()).add(stack.clone());
            definitions.put(itemId, definition.get());
        }
        return new GroupedStacks(amounts, stacksByItemId, definitions);
    }

    private static List<ItemStack> collectAndClearSlots(
            ItemStack[] contents,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        List<ItemStack> collected = new ArrayList<>();
        for (int index = 0; index < contents.length; index++) {
            if (collectMatch(collected, contents[index], itemRegistry, filter)) {
                contents[index] = null;
            }
        }
        return collected;
    }

    private static boolean collectMatch(
            List<ItemStack> collected,
            ItemStack stack,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        if (!matches(stack, itemRegistry, filter)) {
            return false;
        }
        collected.add(stack.clone());
        return true;
    }

    private static boolean matches(
            ItemStack stack,
            ItemRegistry itemRegistry,
            Predicate<SellableItemDefinition> filter
    ) {
        return definition(stack, itemRegistry, CatalogScope.POOL, filter).isPresent();
    }

    private static Optional<SellableItemDefinition> definition(
            ItemStack stack,
            ItemRegistry itemRegistry,
            CatalogScope scope,
            Predicate<SellableItemDefinition> filter
    ) {
        if (ItemStacks.isAbsent(stack)) {
            return Optional.empty();
        }
        Optional<SellableItemDefinition> definition = scope == CatalogScope.ACTIVE
                ? itemRegistry.find(stack)
                : itemRegistry.findInPool(stack);
        if (definition.isEmpty() || !filter.test(definition.get())) {
            return Optional.empty();
        }
        return definition;
    }
}
