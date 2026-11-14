package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
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

        ItemStack itemStack = ItemStack.of(modelMaterial, clampedAmount);
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
        TrimMaterial trimMaterial = resolvePreviewTrimMaterial();
        if (trimMaterial == null || trimPattern == null) {
            return;
        }
        ArmorTrim trim = new ArmorTrim(trimMaterial, trimPattern);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(trim);
            itemStack.setItemMeta(armorMeta);
        }
    }

    private static TrimMaterial resolvePreviewTrimMaterial() {
        for (String materialKey : PREVIEW_TRIM_MATERIALS) {
            TrimMaterial material = trimMaterialByKey(materialKey);
            if (material != null) {
                return material;
            }
        }
        return null;
    }

    private static TrimMaterial trimMaterialByKey(String key) {
        return switch (key) {
            case "gold" -> TrimMaterial.GOLD;
            case "quartz" -> TrimMaterial.QUARTZ;
            case "copper" -> TrimMaterial.COPPER;
            case "amethyst" -> TrimMaterial.AMETHYST;
            case "diamond" -> TrimMaterial.DIAMOND;
            case "iron" -> TrimMaterial.IRON;
            case "emerald" -> TrimMaterial.EMERALD;
            case "lapis" -> TrimMaterial.LAPIS;
            default -> null;
        };
    }

    private static void applyDisplayData(
            ItemStack itemStack,
            Component name,
            List<Component> lore,
            Integer customModelData,
            NamespacedKey itemIdKey,
            String itemId
    ) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.itemName(Component.empty());
        meta.displayName(name);
        meta.lore(lore);
        if (customModelData != null && customModelData >= 0) {
            meta.setCustomModelData(customModelData);
        }
        if (itemIdKey != null && itemId != null) {
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, itemId);
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(itemIdKey.getNamespace(), "tooltip-build"),
                    PersistentDataType.STRING,
                    BUILD_TAG
            );
        }
        itemStack.setItemMeta(meta);
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
        return lookupTrimPattern(extractTrimPatternKey(material));
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
        return trimPatternByKey(patternKey);
    }

    private static TrimPattern trimPatternByKey(String key) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "sentry" -> TrimPattern.SENTRY;
            case "dune" -> TrimPattern.DUNE;
            case "coast" -> TrimPattern.COAST;
            case "wild" -> TrimPattern.WILD;
            case "ward" -> TrimPattern.WARD;
            case "eye" -> TrimPattern.EYE;
            case "vex" -> TrimPattern.VEX;
            case "tide" -> TrimPattern.TIDE;
            case "snout" -> TrimPattern.SNOUT;
            case "rib" -> TrimPattern.RIB;
            case "spire" -> TrimPattern.SPIRE;
            case "wayfinder" -> TrimPattern.WAYFINDER;
            case "shaper" -> TrimPattern.SHAPER;
            case "silence" -> TrimPattern.SILENCE;
            case "raiser" -> TrimPattern.RAISER;
            case "host" -> TrimPattern.HOST;
            case "flow" -> TrimPattern.FLOW;
            case "bolt" -> TrimPattern.BOLT;
            default -> null;
        };
    }

    static String readPersistentString(ItemStack itemStack, NamespacedKey key) {
        if (itemStack == null || key == null || !itemStack.hasItemMeta()) {
            return null;
        }
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return null;
        }
        return itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
