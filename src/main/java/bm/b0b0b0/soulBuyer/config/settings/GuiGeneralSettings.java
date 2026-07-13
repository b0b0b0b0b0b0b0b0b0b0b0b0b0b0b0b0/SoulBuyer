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
        @Comment(@CommentValue("Номер слота в меню (0–53). -1 = шаблон фона (border/separator), слот не занимает"))
        public int slot = 0;

        @Comment(@CommentValue("Иконка предмета (Material Bukkit): BLACK_STAINED_GLASS_PANE, GOLD_INGOT…"))
        public String material = "STONE";

        @Comment(@CommentValue("Название: ключ из lang/*.yml (name-key → gui.… в переводе)"))
        public String nameKey = "";

        @Comment(@CommentValue("Строки lore: ключи из lang/*.yml, сверху вниз"))
        public List<String> loreKeys = List.of();

        @Comment(@CommentValue("Клик: DECORATION = декор без действия. Другие значения — в шапке elements этого gui/*.yml"))
        public String action = "NONE";

        @Comment(@CommentValue("Только фильтр категорий (buyer/autosell): ores | mobs | plants | blocks | misc"))
        public String categoryFilter = "";

        @Comment(@CommentValue("Только бустеры (BOOSTER_BUY): id из config.yml → boosters.offers"))
        public String offerId = "";
    }
}
