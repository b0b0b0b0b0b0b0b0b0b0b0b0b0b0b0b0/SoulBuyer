package bm.b0b0b0.soulBuyer.config.settings;

import java.util.List;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class GuiGeneralSettings extends YamlSerializable {

    public GuiGeneralSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @NewLine
    @Comment(@CommentValue("true — убрать ванильный курсив у названий и lore предметов в GUI"))
    public boolean disableItemItalic = true;

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
}
