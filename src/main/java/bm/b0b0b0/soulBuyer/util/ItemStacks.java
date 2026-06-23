package bm.b0b0b0.soulBuyer.util;

import org.bukkit.inventory.ItemStack;

public final class ItemStacks {

    private ItemStacks() {
    }

    public static boolean isPresent(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static boolean isAbsent(ItemStack stack) {
        return !isPresent(stack);
    }
}
