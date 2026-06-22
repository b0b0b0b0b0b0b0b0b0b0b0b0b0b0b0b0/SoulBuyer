package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;

import java.util.HashMap;
import java.util.Map;

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
}
