package bm.b0b0b0.soulBuyer.config.settings;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GuiAutosellSettings extends YamlSerializable {

    public GuiAutosellSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключ заголовка в lang/*.yml"))
    public String titleKey = "gui.autosell.title";

    @Comment(@CommentValue("54 = 6 рядов"))
    public int size = 54;

    @NewLine
    public Map<String, GuiGeneralSettings.GuiElementSettings> elements = defaultElements();

    private static Map<String, GuiGeneralSettings.GuiElementSettings> defaultElements() {
        Map<String, GuiGeneralSettings.GuiElementSettings> elements = new LinkedHashMap<>();

        GuiGeneralSettings.GuiElementSettings border = new GuiGeneralSettings.GuiElementSettings();
        border.slot = -1;
        border.material = "BLACK_STAINED_GLASS_PANE";
        border.nameKey = "gui.autosell.border";
        border.action = "DECORATION";
        elements.put("border", border);

        GuiGeneralSettings.GuiElementSettings separator = new GuiGeneralSettings.GuiElementSettings();
        separator.slot = -1;
        separator.material = "GRAY_STAINED_GLASS_PANE";
        separator.nameKey = "gui.autosell.separator";
        separator.action = "DECORATION";
        elements.put("separator", separator);

        elements.put("toggle", button(13, "LIME_DYE", "gui.autosell.toggle", "AUTO_TOGGLE"));
        elements.put("trigger", button(22, "HOPPER", "gui.autosell.trigger", "AUTO_TRIGGER"));
        elements.put("notify", button(21, "BELL", "gui.autosell.notify", "AUTO_NOTIFY"));
        elements.put("min-price", button(23, "GOLD_NUGGET", "gui.autosell.min-price", "AUTO_MIN_PRICE"));
        elements.put("payout", button(31, "GOLD_INGOT", "gui.autosell.payout", "AUTO_PAYOUT"));

        elements.put("category-ores", categoryButton(2, "DIAMOND_ORE", "gui.buyer.category-ores", "ores"));
        elements.put("category-mobs", categoryButton(3, "ROTTEN_FLESH", "gui.buyer.category-mobs", "mobs"));
        elements.put("category-plants", categoryButton(5, "WHEAT", "gui.buyer.category-plants", "plants"));
        elements.put("category-blocks", categoryButton(6, "COBBLESTONE", "gui.buyer.category-blocks", "blocks"));
        elements.put("category-misc", categoryButton(7, "EMERALD", "gui.buyer.category-misc", "misc"));

        GuiGeneralSettings.GuiElementSettings info = new GuiGeneralSettings.GuiElementSettings();
        info.slot = 49;
        info.material = "WRITABLE_BOOK";
        info.nameKey = "gui.autosell.info";
        info.loreKeys = List.of("gui.autosell.info-lore");
        info.action = "DECORATION";
        elements.put("info", info);

        elements.put("back", button(45, "LIGHT_GRAY_DYE", "gui.nav.back", "AUTO_BACK"));

        return elements;
    }

    private static GuiGeneralSettings.GuiElementSettings button(
            int slot,
            String material,
            String nameKey,
            String action
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of("gui.autosell.button-lore");
        element.action = action;
        return element;
    }

    private static GuiGeneralSettings.GuiElementSettings categoryButton(
            int slot,
            String material,
            String nameKey,
            String categoryFilter
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of("gui.autosell.category-lore");
        element.action = "AUTO_CATEGORY";
        element.categoryFilter = categoryFilter;
        return element;
    }
}
