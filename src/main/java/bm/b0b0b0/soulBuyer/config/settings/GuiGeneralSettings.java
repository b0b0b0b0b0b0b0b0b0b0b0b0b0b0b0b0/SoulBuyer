package bm.b0b0b0.soulBuyer.config.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class GuiGeneralSettings extends YamlSerializable {

    public GuiGeneralSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключи lang для общих кнопок навигации (другие экраны)"))
    public String closeNameKey = "gui.nav.close";
    public String backNameKey = "gui.nav.back";

    @NewLine
    @Comment(@CommentValue("Общие декоративные элементы (filler) для всех GUI SoulBuyer"))
    public Map<String, GuiElementSettings> navigation = defaultNavigation();

    public static final class GuiElementSettings {
        @Comment(@CommentValue("-1 = не привязан к слоту (шаблон)"))
        public int slot = 0;

        @Comment(@CommentValue("Bukkit Material, например LIME_CONCRETE"))
        public String material = "STONE";

        @Comment(@CommentValue("Ключ названия в lang/*.yml"))
        public String nameKey = "";

        @Comment(@CommentValue("Ключи строк lore в lang/*.yml"))
        public List<String> loreKeys = List.of();

        @Comment(@CommentValue("NONE | DECORATION | SELL_ALL | CLOSE | CATEGORY_FILTER | SORT_CYCLE | PAGE_PREV | PAGE_NEXT | …"))
        public String action = "NONE";

        @Comment(@CommentValue("Для CATEGORY_FILTER — id категории; пусто = все"))
        public String categoryFilter = "";

        @Comment(@CommentValue("Для SORT_CYCLE — не используется; режимы в BuyerSortMode.cycleModes()"))
        public String sortFilter = "";

        @Comment(@CommentValue("Для BOOSTER_BUY — id предложения из config.yml → boosters.offers"))
        public String offerId = "";
    }

    private static Map<String, GuiElementSettings> defaultNavigation() {
        Map<String, GuiElementSettings> elements = new LinkedHashMap<>();

        GuiElementSettings filler = new GuiElementSettings();
        filler.slot = -1;
        filler.material = "GRAY_STAINED_GLASS_PANE";
        filler.nameKey = "gui.nav.filler";
        filler.action = "DECORATION";
        elements.put("filler", filler);

        return elements;
    }
}
