package bm.b0b0b0.soulBuyer.config.settings;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GuiBoostersSettings extends YamlSerializable {

    public GuiBoostersSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключ заголовка в lang/*.yml"))
    public String titleKey = "gui.boosters.title";

    @Comment(@CommentValue("54 = 6 рядов"))
    public int size = 54;

    @NewLine
    public Map<String, GuiGeneralSettings.GuiElementSettings> elements = defaultElements();

    private static Map<String, GuiGeneralSettings.GuiElementSettings> defaultElements() {
        Map<String, GuiGeneralSettings.GuiElementSettings> elements = new LinkedHashMap<>();

        GuiGeneralSettings.GuiElementSettings border = new GuiGeneralSettings.GuiElementSettings();
        border.slot = -1;
        border.material = "BLACK_STAINED_GLASS_PANE";
        border.nameKey = "gui.boosters.border";
        border.action = "DECORATION";
        elements.put("border", border);

        GuiGeneralSettings.GuiElementSettings separator = new GuiGeneralSettings.GuiElementSettings();
        separator.slot = -1;
        separator.material = "GRAY_STAINED_GLASS_PANE";
        separator.nameKey = "gui.boosters.separator";
        separator.action = "DECORATION";
        elements.put("separator", separator);

        elements.put("offer-multiplier", offerButton(20, "multiplier", "EXPERIENCE_BOTTLE", "gui.boosters.offer-multiplier"));
        elements.put("offer-money", offerButton(22, "money", "GOLD_INGOT", "gui.boosters.offer-money"));
        elements.put("offer-limit", offerButton(24, "limit", "CHEST", "gui.boosters.offer-limit"));

        GuiGeneralSettings.GuiElementSettings info = new GuiGeneralSettings.GuiElementSettings();
        info.slot = 49;
        info.material = "WRITABLE_BOOK";
        info.nameKey = "gui.boosters.info";
        info.loreKeys = List.of("gui.boosters.info-lore");
        info.action = "DECORATION";
        elements.put("info", info);

        elements.put("back", backButton(45));

        return elements;
    }

    private static GuiGeneralSettings.GuiElementSettings offerButton(
            int slot,
            String offerId,
            String material,
            String nameKey
    ) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = material;
        element.nameKey = nameKey;
        element.loreKeys = List.of("gui.boosters.offer-lore");
        element.action = "BOOSTER_BUY";
        element.offerId = offerId;
        return element;
    }

    private static GuiGeneralSettings.GuiElementSettings backButton(int slot) {
        GuiGeneralSettings.GuiElementSettings element = new GuiGeneralSettings.GuiElementSettings();
        element.slot = slot;
        element.material = "LIGHT_GRAY_DYE";
        element.nameKey = "gui.nav.back";
        element.action = "BOOSTER_BACK";
        return element;
    }
}
