package bm.b0b0b0.soulBuyer.config.settings;

import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class SoulBuyerItemsSettings extends YamlSerializable {

    public SoulBuyerItemsSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment({
            @CommentValue("=== SELLABLE ITEMS ==="),
            @CommentValue("id — internal key (not Material). Used in stats and market."),
            @CommentValue("material — Bukkit Material (DIAMOND, IRON_INGOT, …)."),
            @CommentValue("category — id from categories in config.yml."),
            @CommentValue("base-price — price per unit at market coefficient 1.0."),
            @CommentValue("base-points — progression points per unit."),
            @CommentValue("custom-model-data: -1 = any CMD; number ≥ 0 = only that CMD.")
    })
    public Map<String, SoulBuyerSettings.SellableItemSettings> items = SoulBuyerItemDefaults.create();
}
