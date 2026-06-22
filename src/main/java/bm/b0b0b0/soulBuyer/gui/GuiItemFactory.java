package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.message.MessageService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GuiItemFactory {

    private final MessageService messageService;

    public GuiItemFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    public ItemStack build(Player player, GuiGeneralSettings.GuiElementSettings element, String... pairs) {
        ItemStack itemStack = buildMaterial(player, element, parseMaterial(element.material), pairs);
        applyInactiveSelection(itemStack);
        return itemStack;
    }

    public ItemStack buildSelected(Player player, GuiGeneralSettings.GuiElementSettings element, String... pairs) {
        ItemStack itemStack = buildMaterial(player, element, parseMaterial(element.material), pairs);
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
        if (itemStack == null || customModelData < 0) {
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(customModelData);
        itemStack.setItemMeta(meta);
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
        if (!element.loreKeys.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String loreKey : element.loreKeys) {
                lore.addAll(messageService.guiLore(player, loreKey, pairs));
            }
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return itemStack;
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

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Material.STONE;
        }
    }
}
