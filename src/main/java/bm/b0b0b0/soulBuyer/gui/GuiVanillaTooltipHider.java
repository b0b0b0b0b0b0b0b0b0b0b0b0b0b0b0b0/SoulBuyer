package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.util.MaterialParser;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemArmorTrim;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

final class GuiVanillaTooltipHider {

    static final String BUILD_TAG = "paper-model-v2";

    private static final Material ARMOR_TRIM_ICON = Material.IRON_CHESTPLATE;
    private static final Material NETHERITE_UPGRADE_ICON = Material.NETHERITE_INGOT;

    private static final Set<DataComponentType> VISIBLE_TOOLTIP_COMPONENTS = Set.of(
            DataComponentTypes.LORE,
            DataComponentTypes.CUSTOM_NAME,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.CUSTOM_MODEL_DATA,
            DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE
    );

    private static final Set<DataComponentType> HIDDEN_TOOLTIP_COMPONENTS = buildHiddenTooltipComponents();

    private GuiVanillaTooltipHider() {
    }

    static ItemStack buildDisplayItem(
            Material sourceMaterial,
            String displayMaterialOverride,
            int amount,
            Component name,
            List<Component> lore,
            Integer customModelData,
            NamespacedKey itemIdKey,
            String itemId
    ) {
        int clampedAmount = Math.max(1, Math.min(64, amount));
        TrimPattern trimPattern = null;
        Material modelMaterial = resolveModelMaterial(sourceMaterial, displayMaterialOverride);
        if (isArmorTrimSmithingTemplate(sourceMaterial)) {
            trimPattern = resolveTrimPattern(sourceMaterial);
            modelMaterial = ARMOR_TRIM_ICON;
        } else if (sourceMaterial == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE && isBlank(displayMaterialOverride)) {
            modelMaterial = NETHERITE_UPGRADE_ICON;
        } else if (isSmithingTemplate(modelMaterial)) {
            trimPattern = resolveTrimPattern(modelMaterial);
            modelMaterial = trimPattern != null ? ARMOR_TRIM_ICON : Material.PAPER;
        }

        ItemStack itemStack = ItemStack.of(Material.PAPER, clampedAmount);
        clearDefaultComponents(itemStack);
        applyVisualModel(itemStack, modelMaterial);
        if (trimPattern != null) {
            applyTrimPattern(itemStack, trimPattern);
        }
        applyDisplayData(itemStack, name, lore, customModelData, itemIdKey, itemId);
        return itemStack;
    }

    private static Material resolveModelMaterial(Material sourceMaterial, String displayMaterialOverride) {
        if (!isBlank(displayMaterialOverride)) {
            Material display = MaterialParser.parse(displayMaterialOverride);
            if (display.isItem() && !isSmithingTemplate(display)) {
                return display;
            }
        }
        if (sourceMaterial != null && sourceMaterial.isItem() && !isSmithingTemplate(sourceMaterial)) {
            return sourceMaterial;
        }
        return Material.STONE;
    }

    private static void applyTrimPattern(ItemStack itemStack, TrimPattern trimPattern) {
        TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft("iron"));
        if (trimMaterial == null) {
            return;
        }
        itemStack.setData(
                DataComponentTypes.TRIM,
                ItemArmorTrim.itemArmorTrim(new ArmorTrim(trimMaterial, trimPattern))
        );
    }

    private static void applyDisplayData(
            ItemStack itemStack,
            Component name,
            List<Component> lore,
            Integer customModelData,
            NamespacedKey itemIdKey,
            String itemId
    ) {
        itemStack.unsetData(DataComponentTypes.ITEM_NAME);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, name);
        if (!lore.isEmpty()) {
            itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        } else {
            itemStack.unsetData(DataComponentTypes.LORE);
        }
        if (customModelData != null && customModelData >= 0) {
            itemStack.setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA,
                    CustomModelData.customModelData().addFloat(customModelData.floatValue())
            );
        }
        if (itemIdKey != null && itemId != null) {
            itemStack.editPersistentDataContainer(container -> {
                container.set(itemIdKey, PersistentDataType.STRING, itemId);
                container.set(new NamespacedKey(itemIdKey.getNamespace(), "tooltip-build"), PersistentDataType.STRING, BUILD_TAG);
            });
        }
        itemStack.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .hideTooltip(false)
                        .hiddenComponents(HIDDEN_TOOLTIP_COMPONENTS)
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isSmithingTemplate(Material material) {
        return material != null && material.name().endsWith("_SMITHING_TEMPLATE");
    }

    private static boolean isArmorTrimSmithingTemplate(Material material) {
        return material != null && material.name().endsWith("_ARMOR_TRIM_SMITHING_TEMPLATE");
    }

    private static TrimPattern resolveTrimPattern(Material material) {
        if (material == null) {
            return null;
        }
        String materialKey = material.getKey().getKey();
        String suffix = "_armor_trim_smithing_template";
        if (!materialKey.endsWith(suffix)) {
            return null;
        }
        String patternKey = materialKey.substring(0, materialKey.length() - suffix.length());
        return Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(patternKey));
    }

    private static void clearDefaultComponents(ItemStack itemStack) {
        Set<DataComponentType> present = new HashSet<>(itemStack.getDataTypes());
        for (DataComponentType type : present) {
            if (type != DataComponentTypes.MAX_STACK_SIZE) {
                itemStack.unsetData(type);
            }
        }
    }

    private static void applyVisualModel(ItemStack itemStack, Material visualMaterial) {
        if (visualMaterial == null || !visualMaterial.isItem()) {
            return;
        }
        ItemType itemType = visualMaterial.asItemType();
        if (itemType != null && itemType.hasDefaultData(DataComponentTypes.ITEM_MODEL)) {
            itemStack.setData(DataComponentTypes.ITEM_MODEL, itemType.getDefaultData(DataComponentTypes.ITEM_MODEL));
            return;
        }
        itemStack.setData(
                DataComponentTypes.ITEM_MODEL,
                Key.key(visualMaterial.getKey().getNamespace(), visualMaterial.getKey().getKey())
        );
    }

    private static Set<DataComponentType> buildHiddenTooltipComponents() {
        Set<DataComponentType> hidden = new HashSet<>();
        for (Field field : DataComponentTypes.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                Object value = field.get(null);
                if (value instanceof DataComponentType type
                        && !VISIBLE_TOOLTIP_COMPONENTS.contains(type)
                        && type != DataComponentTypes.TOOLTIP_DISPLAY) {
                    hidden.add(type);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        hidden.add(DataComponentTypes.ITEM_NAME);
        hidden.add(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        hidden.add(DataComponentTypes.TRIM);
        hidden.add(DataComponentTypes.PROVIDES_TRIM_MATERIAL);
        hidden.add(DataComponentTypes.ITEM_MODEL);
        hidden.add(DataComponentTypes.RARITY);
        hidden.removeAll(VISIBLE_TOOLTIP_COMPONENTS);
        return Set.copyOf(hidden);
    }

    static String readPersistentString(ItemStack itemStack, NamespacedKey key) {
        if (itemStack == null || key == null) {
            return null;
        }
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING)) {
            return null;
        }
        return container.get(key, PersistentDataType.STRING);
    }
}
