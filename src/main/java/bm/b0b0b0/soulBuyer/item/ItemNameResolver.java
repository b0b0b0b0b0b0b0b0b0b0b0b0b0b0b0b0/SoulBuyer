package bm.b0b0b0.soulBuyer.item;

import bm.b0b0b0.soulBuyer.util.ItemStacks;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemNameResolver {

    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    public Component displayComponent(Player player, ItemStack itemStack) {
        if (ItemStacks.isAbsent(itemStack)) {
            return Component.empty();
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component display = meta.displayName();
            if (display != null) {
                return display;
            }
        }
        return Component.translatable(itemStack.getType().translationKey());
    }

    public String plainName(Player player, ItemStack itemStack) {
        return plain.serialize(displayComponent(player, itemStack));
    }

    public String plainMaterialName(Player player, org.bukkit.Material material) {
        return plain.serialize(translatedMaterialName(player, material));
    }

    public Component materialNameComponent(org.bukkit.Material material) {
        return Component.translatable(material.translationKey());
    }

    public Component translatedMaterialName(Player player, org.bukkit.Material material) {
        Locale locale = player != null ? player.locale() : Locale.getDefault();
        return GlobalTranslator.render(materialNameComponent(material), locale);
    }

    public String formatAmount(Player player, ItemStack itemStack) {
        if (ItemStacks.isAbsent(itemStack)) {
            return "";
        }
        String name = plainName(player, itemStack);
        if (itemStack.getAmount() <= 1) {
            return name;
        }
        return itemStack.getAmount() + " × " + name;
    }

    public String formatMoney(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001D) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
