package bm.b0b0b0.soulBuyer.gui;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class PaperTooltipDisplaySupport {

    private static final Set<DataComponentType> VISIBLE_TOOLTIP_COMPONENTS = Set.of(
            DataComponentTypes.LORE,
            DataComponentTypes.CUSTOM_NAME,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.CUSTOM_MODEL_DATA,
            DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE
    );

    private static final DataComponentType.Valued<TooltipDisplay> TOOLTIP_DISPLAY = resolveTooltipDisplayType();
    private static final Set<DataComponentType> HIDDEN_TOOLTIP_COMPONENTS = buildHiddenTooltipComponents(TOOLTIP_DISPLAY);

    private PaperTooltipDisplaySupport() {
    }

    static boolean supported() {
        return TOOLTIP_DISPLAY != null;
    }

    static void apply(ItemStack itemStack) {
        if (!supported()) {
            applyLegacyItemFlags(itemStack);
            return;
        }
        itemStack.setData(
                TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .hideTooltip(false)
                        .hiddenComponents(HIDDEN_TOOLTIP_COMPONENTS)
        );
    }

    static boolean hasTooltipDisplay(ItemStack itemStack) {
        return supported() && itemStack.hasData(TOOLTIP_DISPLAY);
    }

    static TooltipDisplay read(ItemStack itemStack) {
        if (!supported()) {
            return null;
        }
        return itemStack.getData(TOOLTIP_DISPLAY);
    }

    private static void applyLegacyItemFlags(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON
        );
        itemStack.setItemMeta(meta);
    }

    @SuppressWarnings("unchecked")
    private static DataComponentType.Valued<TooltipDisplay> resolveTooltipDisplayType() {
        try {
            Field field = DataComponentTypes.class.getField("TOOLTIP_DISPLAY");
            Object value = field.get(null);
            if (value instanceof DataComponentType.Valued<?> valued) {
                return (DataComponentType.Valued<TooltipDisplay>) valued;
            }
        } catch (ReflectiveOperationException | NoSuchFieldError ignored) {
        }
        return null;
    }

    private static Set<DataComponentType> buildHiddenTooltipComponents(DataComponentType tooltipDisplay) {
        Set<DataComponentType> hidden = new HashSet<>();
        for (Field field : DataComponentTypes.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                Object value = field.get(null);
                if (!(value instanceof DataComponentType type)) {
                    continue;
                }
                if (VISIBLE_TOOLTIP_COMPONENTS.contains(type)) {
                    continue;
                }
                if (tooltipDisplay != null && tooltipDisplay.equals(type)) {
                    continue;
                }
                hidden.add(type);
            } catch (IllegalAccessException | NoSuchFieldError ignored) {
            }
        }
        hidden.add(DataComponentTypes.ITEM_NAME);
        hidden.add(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        try {
            hidden.add(DataComponentTypes.PROVIDES_TRIM_MATERIAL);
        } catch (NoSuchFieldError ignored) {
        }
        try {
            hidden.add(DataComponentTypes.ITEM_MODEL);
        } catch (NoSuchFieldError ignored) {
        }
        try {
            hidden.add(DataComponentTypes.RARITY);
        } catch (NoSuchFieldError ignored) {
        }
        hidden.removeAll(VISIBLE_TOOLTIP_COMPONENTS);
        return Set.copyOf(hidden);
    }
}
