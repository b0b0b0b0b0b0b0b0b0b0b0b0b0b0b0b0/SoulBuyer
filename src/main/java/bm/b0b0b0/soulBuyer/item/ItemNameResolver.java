package bm.b0b0b0.soulBuyer.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public final class ItemNameResolver {

    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    public Component displayComponent(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
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
        return plain.serialize(Component.translatable(material.translationKey()));
    }

    public String formatAmount(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
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
