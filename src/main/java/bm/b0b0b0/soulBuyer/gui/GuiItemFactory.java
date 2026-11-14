package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GuiItemFactory {

    private final MessageService messageService;

    public GuiItemFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    public ItemStack build(Player player, GuiGeneralSettings.GuiElementSettings element, String... pairs) {
        ItemStack itemStack = buildMaterial(player, element, MaterialParser.parse(element.material), pairs);
        applyInactiveSelection(itemStack);
        return itemStack;
    }

    public ItemStack buildSelected(Player player, GuiGeneralSettings.GuiElementSettings element, String... pairs) {
        ItemStack itemStack = buildMaterial(player, element, MaterialParser.parse(element.material), pairs);
        applyActiveSelection(itemStack);
        return itemStack;
    }

    public ItemStack buildSelectedMaterial(
            Player player,
            GuiGeneralSettings.GuiElementSettings element,
            Material material,
            String... pairs
    ) {
        ItemStack itemStack = buildMaterial(player, element, material, pairs);
        applyActiveSelection(itemStack);
        return itemStack;
    }

    public void applyCustomModelData(ItemStack itemStack, int customModelData) {
        ItemStacks.applyCustomModelData(itemStack, customModelData);
    }

    public ItemStack buildMaterial(
            Player player,
            GuiGeneralSettings.GuiElementSettings element,
            Material material,
            String... pairs
    ) {
        ItemStack itemStack = ItemStack.of(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (!element.nameKey.isEmpty()) {
            meta.displayName(messageService.guiText(player, element.nameKey, pairs));
        }
        applyLoreAndFlags(player, element, meta, pairs);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack buildMaterialWithDisplayName(
            Player player,
            GuiGeneralSettings.GuiElementSettings element,
            Material material,
            Component displayName,
            String... lorePairs
    ) {
        ItemStack itemStack = ItemStack.of(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(displayName);
        applyLoreAndFlags(player, element, meta, lorePairs);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack buildSelectedMaterialWithDisplayName(
            Player player,
            GuiGeneralSettings.GuiElementSettings element,
            Material material,
            Component displayName,
            String... lorePairs
    ) {
        ItemStack itemStack = buildMaterialWithDisplayName(player, element, material, displayName, lorePairs);
        applyActiveSelection(itemStack);
        return itemStack;
    }

    public ItemStack navigationFiller(
            Player player,
            GuiGeneralSettings.GuiElementSettings separator,
            GuiGeneralSettings.GuiElementSettings border
    ) {
        if (separator != null) {
            return filler(player, separator);
        }
        return filler(player, border);
    }

    private void applyLoreAndFlags(
            Player player,
            GuiGeneralSettings.GuiElementSettings element,
            ItemMeta meta,
            String... pairs
    ) {
        if (!element.loreKeys.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String loreKey : element.loreKeys) {
                lore.addAll(messageService.guiLore(player, loreKey, pairs));
            }
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
    }

    public ItemStack filler(Player player, GuiGeneralSettings.GuiElementSettings element) {
        return build(player, element);
    }

    private void applyActiveSelection(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(meta);
    }

    private void applyInactiveSelection(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setEnchantmentGlintOverride(false);
        itemStack.setItemMeta(meta);
    }
}
