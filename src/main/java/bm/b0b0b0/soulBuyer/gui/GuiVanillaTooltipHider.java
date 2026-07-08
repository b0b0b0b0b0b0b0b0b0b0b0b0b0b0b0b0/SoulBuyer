package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.util.MaterialParser;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemArmorTrim;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

final class GuiVanillaTooltipHider {

    static final String BUILD_TAG = "armor-trim-v2";

    private static final Material ARMOR_TRIM_ICON = Material.IRON_CHESTPLATE;
    private static final Material NETHERITE_UPGRADE_ICON = Material.NETHERITE_INGOT;
    private static final String[] PREVIEW_TRIM_MATERIALS = {
            "gold", "quartz", "copper", "amethyst", "diamond", "iron", "emerald", "lapis"
    };

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
        if (isArmorTrimSmithingTemplate(sourceMaterial)) {
            TrimPattern trimPattern = resolveTrimPattern(sourceMaterial);
            if (trimPattern != null) {
                return buildArmorTrimPreview(clampedAmount, trimPattern, name, lore, customModelData, itemIdKey, itemId);
            }
        }
        if (sourceMaterial == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE && isBlank(displayMaterialOverride)) {
            return buildMaterialPreview(
                    NETHERITE_UPGRADE_ICON,
                    clampedAmount,
                    name,
                    lore,
                    customModelData,
                    itemIdKey,
                    itemId
            );
        }

        Material modelMaterial = resolveModelMaterial(sourceMaterial, displayMaterialOverride);
        if (isSmithingTemplate(modelMaterial)) {
            TrimPattern trimPattern = resolveTrimPattern(modelMaterial);
            if (trimPattern != null) {
                return buildArmorTrimPreview(clampedAmount, trimPattern, name, lore, customModelData, itemIdKey, itemId);
            }
            modelMaterial = Material.PAPER;
        }

        ItemStack itemStack = ItemStack.of(Material.PAPER, clampedAmount);
        clearDefaultComponents(itemStack);
        applyVisualModel(itemStack, modelMaterial);
        applyDisplayData(itemStack, name, lore, customModelData, itemIdKey, itemId);
        return itemStack;
    }

    private static ItemStack buildArmorTrimPreview(
            int amount,
            TrimPattern trimPattern,
            Component name,
            List<Component> lore,
            Integer customModelData,
            NamespacedKey itemIdKey,
            String itemId
    ) {
        ItemStack itemStack = ItemStack.of(ARMOR_TRIM_ICON, amount);
        stripVanillaTooltipNoise(itemStack);
        applyTrimPattern(itemStack, trimPattern);
        applyDisplayData(itemStack, name, lore, customModelData, itemIdKey, itemId);
        return itemStack;
    }

    private static ItemStack buildMaterialPreview(
            Material material,
            int amount,
            Component name,
            List<Component> lore,
            Integer customModelData,
            NamespacedKey itemIdKey,
            String itemId
    ) {
        ItemStack itemStack = ItemStack.of(material, amount);
        stripVanillaTooltipNoise(itemStack);
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

    private static void stripVanillaTooltipNoise(ItemStack itemStack) {
        itemStack.unsetData(DataComponentTypes.ITEM_NAME);
        itemStack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        itemStack.unsetData(DataComponentTypes.PROVIDES_TRIM_MATERIAL);
    }

    private static void applyTrimPattern(ItemStack itemStack, TrimPattern trimPattern) {
        TrimMaterial trimMaterial = resolvePreviewTrimMaterial();
        if (trimMaterial == null || trimPattern == null) {
            return;
        }
        ArmorTrim trim = new ArmorTrim(trimMaterial, trimPattern);
        itemStack.setData(DataComponentTypes.TRIM, ItemArmorTrim.itemArmorTrim(trim));
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(trim);
            itemStack.setItemMeta(armorMeta);
        }
    }

    private static TrimMaterial resolvePreviewTrimMaterial() {
        for (String materialKey : PREVIEW_TRIM_MATERIALS) {
            TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(materialKey));
            if (material != null) {
                return material;
            }
        }
        return null;
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
        PaperTooltipDisplaySupport.apply(itemStack);
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
        TrimPattern fromKey = lookupTrimPattern(extractTrimPatternKey(material));
        if (fromKey != null) {
            return fromKey;
        }
        return resolveTrimPatternFromRecipes(material);
    }

    private static String extractTrimPatternKey(Material material) {
        String materialKey = material.getKey().getKey().toLowerCase(Locale.ROOT);
        String suffix = "_armor_trim_smithing_template";
        if (!materialKey.endsWith(suffix)) {
            return "";
        }
        return materialKey.substring(0, materialKey.length() - suffix.length());
    }

    private static TrimPattern lookupTrimPattern(String patternKey) {
        if (patternKey == null || patternKey.isBlank()) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(patternKey);
        TrimPattern pattern = Registry.TRIM_PATTERN.get(key);
        if (pattern != null) {
            return pattern;
        }
        return Bukkit.getRegistry(TrimPattern.class).get(key);
    }

    private static TrimPattern resolveTrimPatternFromRecipes(Material material) {
        ItemStack templateStack = ItemStack.of(material, 1);
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof SmithingTrimRecipe trimRecipe)) {
                continue;
            }
            if (matchesTemplateChoice(trimRecipe.getTemplate(), templateStack)) {
                return trimRecipe.getTrimPattern();
            }
        }
        return null;
    }

    private static boolean matchesTemplateChoice(RecipeChoice choice, ItemStack templateStack) {
        if (choice == null || templateStack == null) {
            return false;
        }
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return materialChoice.getChoices().contains(templateStack.getType());
        }
        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            for (ItemStack candidate : exactChoice.getChoices()) {
                if (candidate != null && candidate.getType() == templateStack.getType()) {
                    return true;
                }
            }
        }
        return choice.test(templateStack);
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
