package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class GuiLayoutHelper {

    private GuiLayoutHelper() {
    }

    public static Map<Integer, String> actionBySlot(Map<String, GuiGeneralSettings.GuiElementSettings> elements) {
        Map<Integer, String> actions = new HashMap<>();
        for (GuiGeneralSettings.GuiElementSettings element : elements.values()) {
            if (element.slot >= 0) {
                actions.put(element.slot, element.action);
            }
        }
        return actions;
    }

    public static int[] frameSlots(int size) {
        int rows = size / 9;
        int[] slots = new int[rows * 9];
        for (int index = 0; index < slots.length; index++) {
            slots[index] = index;
        }
        return slots;
    }

    public static void fillBorderAndSeparators(
            Inventory inventory,
            Player player,
            GuiItemFactory factory,
            int size,
            GuiGeneralSettings.GuiElementSettings border,
            GuiGeneralSettings.GuiElementSettings separator,
            IntPredicate skipSlot,
            int[] separatorSlots
    ) {
        if (border == null) {
            return;
        }
        for (int slot : frameSlots(size)) {
            if (!skipSlot.test(slot)) {
                inventory.setItem(slot, factory.filler(player, border));
            }
        }
        if (separator == null || separatorSlots == null) {
            return;
        }
        for (int slot : separatorSlots) {
            if (!skipSlot.test(slot)) {
                inventory.setItem(slot, factory.filler(player, separator));
            }
        }
    }
}
