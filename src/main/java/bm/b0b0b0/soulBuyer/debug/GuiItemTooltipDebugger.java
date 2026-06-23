package bm.b0b0b0.soulBuyer.debug;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.inventory.tooltip.TooltipContext;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class GuiItemTooltipDebugger {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private GuiItemTooltipDebugger() {
    }

    public static List<String> analyze(ItemStack itemStack, Player player, String label) {
        List<String> lines = new ArrayList<>();
        lines.add("=== " + label + " ===");
        if (itemStack == null || itemStack.getType().isAir()) {
            lines.add("stack=AIR");
            return lines;
        }
        lines.add("bukkitType=" + itemStack.getType());
        if (itemStack.getType() == Material.PAPER) {
            lines.add("tooltipBuildTag=" + readTooltipBuildTag(itemStack));
        }
        lines.add("amount=" + itemStack.getAmount());
        lines.add("dataTypesCount=" + itemStack.getDataTypes().size());
        lines.add("dataTypes=" + formatDataTypes(itemStack));
        lines.add("hasItemMeta=" + itemStack.hasItemMeta());
        if (itemStack.hasData(DataComponentTypes.ITEM_MODEL)) {
            lines.add("itemModel=" + itemStack.getData(DataComponentTypes.ITEM_MODEL));
        }
        if (itemStack.hasData(DataComponentTypes.TRIM)) {
            lines.add("trim=" + itemStack.getData(DataComponentTypes.TRIM).armorTrim());
        }
        if (itemStack.hasData(DataComponentTypes.CUSTOM_NAME)) {
            lines.add("customName=" + PLAIN.serialize(itemStack.getData(DataComponentTypes.CUSTOM_NAME)));
        }
        if (itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY)) {
            TooltipDisplay display = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY);
            lines.add("tooltipDisplay.hideTooltip=" + display.hideTooltip());
            lines.add("tooltipDisplay.hiddenCount=" + display.hiddenComponents().size());
        }
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            lines.add("meta.hideTooltip=" + meta.isHideTooltip());
            lines.add("meta.hasDisplayName=" + meta.hasDisplayName());
            lines.add("meta.hasItemName=" + meta.hasItemName());
            lines.add("meta.hasLore=" + meta.hasLore());
            lines.add("meta.hasItemModel=" + meta.hasItemModel());
            if (meta.hasItemModel()) {
                lines.add("meta.itemModel=" + meta.getItemModel());
            }
            try {
                lines.add("meta.componentString=" + meta.getAsComponentString());
            } catch (Exception exception) {
                lines.add("meta.componentString=ERROR " + exception.getMessage());
            }
        }
        appendTooltipLines(lines, "tooltip", itemStack, TooltipContext.create(), player);
        appendTooltipLines(lines, "tooltipAdvanced", itemStack, TooltipContext.create().asAdvanced(), player);
        lines.add("serializeBytes=" + itemStack.serializeAsBytes().length);
        return lines;
    }

    public static void dump(SoulBuyerDebugLog debug, Player player, String label, ItemStack itemStack) {
        for (String line : analyze(itemStack, player, label)) {
            debug.tooltipDebug(line);
        }
    }

    public static void dumpComparison(
            SoulBuyerDebugLog debug,
            Player player,
            Material sourceMaterial,
            ItemStack hiddenStack,
            ItemStack rawStack
    ) {
        debug.tooltipDebug("----- TOOLTIP DEBUG COMPARISON sourceMaterial=" + sourceMaterial
                + " hideVanillaPath=" + (hiddenStack != null)
                + " player=" + player.getName() + " -----");
        if (rawStack != null) {
            dump(debug, player, "RAW(" + sourceMaterial + ")", rawStack);
        }
        if (hiddenStack != null) {
            dump(debug, player, "HIDDEN-PATH", hiddenStack);
        }
        debug.tooltipDebug("----- END TOOLTIP DEBUG -----");
    }

    private static void appendTooltipLines(
            List<String> lines,
            String prefix,
            ItemStack itemStack,
            TooltipContext context,
            Player player
    ) {
        List<Component> tooltipLines = itemStack.computeTooltipLines(context, player);
        lines.add(prefix + ".lineCount=" + tooltipLines.size());
        for (int index = 0; index < tooltipLines.size(); index++) {
            lines.add(prefix + "[" + index + "]=" + PLAIN.serialize(tooltipLines.get(index)));
        }
    }

    private static String formatDataTypes(ItemStack itemStack) {
        return itemStack.getDataTypes().stream()
                .sorted(Comparator.comparing(type -> type.toString()))
                .map(GuiItemTooltipDebugger::formatDataType)
                .collect(Collectors.joining(", "));
    }

    private static String formatDataType(DataComponentType type) {
        return type.toString();
    }

    private static String readTooltipBuildTag(ItemStack itemStack) {
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();
        for (NamespacedKey key : container.getKeys()) {
            if ("tooltip-build".equals(key.getKey()) && container.has(key, PersistentDataType.STRING)) {
                return container.get(key, PersistentDataType.STRING);
            }
        }
        return "missing";
    }
}
