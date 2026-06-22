package bm.b0b0b0.soulBuyer.config.settings;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GuiQuantitySettings extends YamlSerializable {

    public GuiQuantitySettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключ заголовка в lang/*.yml"))
    public final String titleKey = "gui.quantity.title";

    @Comment(@CommentValue("54 = 6 рядов"))
    public final int size = 54;

    @Comment(@CommentValue("Слот превью продаваемого предмета"))
    public final int previewSlot = 31;

    @Comment(@CommentValue("Слот счётчика выбранного количества"))
    public final int amountInfoSlot = 22;

    @NewLine
    public final Map<String, GuiGeneralSettings.GuiElementSettings> elements = defaultElements();

    private static Map<String, GuiGeneralSettings.GuiElementSettings> defaultElements() {
        Map<String, GuiGeneralSettings.GuiElementSettings> elements = new LinkedHashMap<>();

        GuiGeneralSettings.GuiElementSettings border = new GuiGeneralSettings.GuiElementSettings();
        border.slot = -1;
        border.material = "BLACK_STAINED_GLASS_PANE";
        border.nameKey = "gui.quantity.border";
        border.action = "DECORATION";
        elements.put("border", border);

        GuiGeneralSettings.GuiElementSettings separator = new GuiGeneralSettings.GuiElementSettings();
        separator.slot = -1;
        separator.material = "GRAY_STAINED_GLASS_PANE";
        separator.nameKey = "gui.quantity.separator";
        separator.action = "DECORATION";
        elements.put("separator", separator);

        elements.put("minus-10", qtyButton(10, "RED_STAINED_GLASS_PANE", "gui.quantity.minus-10", "QTY_MINUS_10"));
        elements.put("minus-5", qtyButton(19, "PINK_STAINED_GLASS_PANE", "gui.quantity.minus-5", "QTY_MINUS_5"));
        elements.put("minus-1", qtyButton(20, "ORANGE_STAINED_GLASS_PANE", "gui.quantity.minus-1", "QTY_MINUS_1"));
        elements.put("plus-1", qtyButton(24, "LIME_STAINED_GLASS_PANE", "gui.quantity.plus-1", "QTY_PLUS_1"));
        elements.put("plus-5", qtyButton(25, "GREEN_STAINED_GLASS_PANE", "gui.quantity.plus-5", "QTY_PLUS_5"));
        elements.put("plus-10", qtyButton(16, "EMERALD_BLOCK", "gui.quantity.plus-10", "QTY_PLUS_10"));

        elements.put("set-1", qtyButton(28, "IRON_NUGGET", "gui.quantity.set-1", "QTY_SET_1"));
        elements.put("set-5", qtyButton(30, "IRON_INGOT", "gui.quantity.set-5", "QTY_SET_5"));
        elements.put("set-10", qtyButton(32, "GOLD_INGOT", "gui.quantity.set-10", "QTY_SET_10"));

        GuiGeneralSettings.GuiElementSettings amountInfo = new GuiGeneralSettings.GuiElementSettings();
        amountInfo.slot = 22;
        amountInfo.material = "PAPER";
        amountInfo.nameKey = "gui.quantity.amount";
        amountInfo.loreKeys = List.of("gui.quantity.amount-lore");
        amountInfo.action = "DECORATION";
        elements.put("amount-info", amountInfo);

        elements.put("back", qtyButton(45, "LIGHT_GRAY_DYE", "gui.nav.back", "QTY_BACK"));
        elements.put("confirm", qtyButton(49, "HOPPER", "gui.quantity.confirm", "QTY_CONFIRM"));
        elements.put("sell-all", qtyButton(53, "CHEST", "gui.quantity.sell-all", "QTY_SELL_ALL"));

        return elements;
    }

    private static GuiGeneralSettings.GuiElementSettings qtyButton(
            int slot,
            String material,
            String nameKey,
            String action
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of("gui.quantity.button-lore");
        element.action = action;
        return element;
    }
}
