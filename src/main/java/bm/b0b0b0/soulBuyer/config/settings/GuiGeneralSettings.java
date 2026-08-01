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
    @Comment(@CommentValue("true — remove vanilla italics from item names and lore in GUI"))
    public boolean disableItemItalic = true;

    public static final class GuiElementSettings {
        @Comment(@CommentValue("Slot index in the menu (0–53). -1 = background template (border/separator), does not occupy a slot"))
        public int slot = 0;

        @Comment(@CommentValue("Item icon (Bukkit Material): BLACK_STAINED_GLASS_PANE, GOLD_INGOT…"))
        public String material = "STONE";

        @Comment(@CommentValue("Display name: key from lang/*.yml (name-key → gui.… in translations)"))
        public String nameKey = "";

        @Comment(@CommentValue("Lore lines: keys from lang/*.yml, top to bottom"))
        public List<String> loreKeys = List.of();

        @Comment(@CommentValue("Click: DECORATION = decor with no action. Other values — see elements header in this gui/*.yml"))
        public String action = "NONE";

        @Comment(@CommentValue("Category filter only (buyer/autosell): ores | mobs | plants | blocks | misc"))
        public String categoryFilter = "";

        @Comment(@CommentValue("Boosters only (BOOSTER_BUY): id from config.yml → boosters.offers"))
        public String offerId = "";
    }
}
