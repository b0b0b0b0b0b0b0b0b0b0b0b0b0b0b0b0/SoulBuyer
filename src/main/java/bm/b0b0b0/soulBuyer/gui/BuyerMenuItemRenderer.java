package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BuyerMenuItemRenderer {

    private final JavaPlugin plugin;
    private final MessageService messageService;
    private final ItemNameResolver itemNameResolver;
    private final NamespacedKey itemIdKey;

    public BuyerMenuItemRenderer(
            JavaPlugin plugin,
            MessageService messageService,
            ItemNameResolver itemNameResolver
    ) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.itemNameResolver = itemNameResolver;
        this.itemIdKey = new NamespacedKey(plugin, "item-id");
    }

    public ItemStack render(Player player, SellableItemDefinition definition, ItemUnitQuote quote) {
        return render(player, definition, quote, BuyerPayoutMode.VAULT);
    }

    public ItemStack render(
            Player player,
            SellableItemDefinition definition,
            ItemUnitQuote quote,
            BuyerPayoutMode payoutMode
    ) {
        Material material = parseMaterial(definition.material());
        ItemStack itemStack = ItemStack.of(material, Math.clamp(quote.inventoryAmount(), 1, 64));
        ItemMeta meta = itemStack.getItemMeta();
        Component itemName = itemNameResolver.displayComponent(player, itemStack);
        meta.displayName(messageService.guiItemName(player, "gui.buyer.item-name", itemName));
        meta.lore(buildLore(player, definition, quote, payoutMode));
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, definition.id());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public String readItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        return itemStack.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
    }

    private List<Component> buildLore(
            Player player,
            SellableItemDefinition definition,
            ItemUnitQuote quote,
            BuyerPayoutMode payoutMode
    ) {
        String categoryName = messageService.raw(player, "categories." + definition.categoryId());
        String[] pairs = new String[]{
                "price", itemNameResolver.formatMoney(quote.unitPrice()),
                "points", itemNameResolver.formatMoney(quote.unitPoints()),
                "market", itemNameResolver.formatMoney(quote.marketCoefficient()),
                "multiplier", itemNameResolver.formatMoney(quote.playerMultiplier()),
                "amount", String.valueOf(quote.inventoryAmount()),
                "category", categoryName
        };
        String loreKey = payoutMode == BuyerPayoutMode.PLAYER_POINTS
                ? "gui.buyer.item-lore-playerpoints"
                : "gui.buyer.item-lore";
        List<Component> lore = new ArrayList<>(messageService.guiLore(player, loreKey, pairs));
        if (quote.inventoryAmount() > 0) {
            lore.addAll(messageService.guiLore(player, "gui.buyer.item-sell-hint", pairs));
        } else {
            lore.addAll(messageService.guiLore(player, "gui.buyer.item-empty-hint", pairs));
        }
        return lore;
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Material.STONE;
        }
    }
}
