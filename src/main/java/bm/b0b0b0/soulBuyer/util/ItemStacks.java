package bm.b0b0b0.soulBuyer.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import java.util.List;
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

    public static void applyCustomModelData(ItemStack itemStack, int customModelData) {
        if (isAbsent(itemStack) || customModelData < 0) {
            return;
        }
        itemStack.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addFloat(customModelData)
        );
    }

    public static boolean matchesCustomModelData(ItemStack itemStack, int expected) {
        if (!itemStack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            return false;
        }
        List<Float> floats = itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA).floats();
        return !floats.isEmpty() && floats.getFirst().intValue() == expected;
    }
}
