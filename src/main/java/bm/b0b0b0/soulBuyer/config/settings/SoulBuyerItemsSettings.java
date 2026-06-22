package bm.b0b0b0.soulBuyer.config.settings;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.Map;

public final class SoulBuyerItemsSettings extends YamlSerializable {

    public SoulBuyerItemsSettings() {
        super(SoulBuyerSerializerConfig.INSTANCE);
    }

    @Comment({
            @CommentValue("=== СКУПАЕМЫЕ ПРЕДМЕТЫ ==="),
            @CommentValue("id — внутренний ключ (не Material). Используется в статистике и рынке."),
            @CommentValue("material — Bukkit Material (DIAMOND, IRON_INGOT, …)."),
            @CommentValue("category — id из categories в config.yml."),
            @CommentValue("base-price — цена за 1 шт. при коэффициенте рынка 1.0."),
            @CommentValue("base-points — очки прогрессии за 1 шт."),
            @CommentValue("custom-model-data: -1 = любой CMD; число ≥ 0 = только с этим CMD.")
    })
    public Map<String, SoulBuyerSettings.SellableItemSettings> items = SoulBuyerItemDefaults.create();
}
