package bm.b0b0b0.soulBuyer.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemStacks {

    private ItemStacks() {
    }

    public static boolean isPresent(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static boolean isAbsent(ItemStack stack) {
        return !isPresent(stack);
    }

    public static void applyCustomModelData(ItemStack itemStack, int customModelData) {
        if (isAbsent(itemStack) || customModelData < 0) {
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(customModelData);
        itemStack.setItemMeta(meta);
    }

    public static boolean matchesCustomModelData(ItemStack itemStack, int expected) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == expected;
    }
}
