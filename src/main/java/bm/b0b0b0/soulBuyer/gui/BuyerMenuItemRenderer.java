package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerMenuItemRenderer {

    private final PluginConfig config;
    private final MessageService messageService;
    private final ItemNameResolver itemNameResolver;
    private final NamespacedKey itemIdKey;

    public BuyerMenuItemRenderer(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService,
            ItemNameResolver itemNameResolver
    ) {
        this.config = config;
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
        Material sourceMaterial = MaterialParser.parse(definition.material());
        Material visualMaterial = resolveVisualMaterial(definition, sourceMaterial);
        Component nameComponent = buildNameComponent(player, definition, sourceMaterial);
        String plainName = plainItemName(player, definition, sourceMaterial);
        return buildItemStack(
                player,
                definition,
                quote,
                payoutMode,
                sourceMaterial,
                visualMaterial,
                nameComponent,
                plainName,
                true,
                List.of(),
                false
        );
    }

    public ItemStack renderAutosellCategoryItem(
            Player player,
            SellableItemDefinition definition,
            ItemUnitQuote quote,
            BuyerPayoutMode payoutMode,
            List<Component> autosellLore,
            boolean enabled
    ) {
        Material sourceMaterial = MaterialParser.parse(definition.material());
        Material visualMaterial = resolveVisualMaterial(definition, sourceMaterial);
        Component nameComponent = buildNameComponent(player, definition, sourceMaterial);
        String plainName = plainItemName(player, definition, sourceMaterial);
        return buildItemStack(
                player,
                definition,
                quote,
                payoutMode,
                sourceMaterial,
                visualMaterial,
                nameComponent,
                plainName,
                false,
                autosellLore,
                enabled
        );
    }

    private ItemStack buildItemStack(
            Player player,
            SellableItemDefinition definition,
            ItemUnitQuote quote,
            BuyerPayoutMode payoutMode,
            Material sourceMaterial,
            Material visualMaterial,
            Component nameComponent,
            String plainName,
            boolean includeInteractionHints,
            List<Component> extraLore,
            boolean selected
    ) {
        List<Component> lore = buildLore(
                player,
                definition,
                quote,
                payoutMode,
                sourceMaterial,
                plainName,
                includeInteractionHints
        );
        lore.addAll(extraLore);
        int amount = Math.max(1, Math.min(64, quote.inventoryAmount()));
        Integer customModelData = definition.usesCustomModelData() ? definition.customModelData() : null;

        ItemStack itemStack;
        if (config.buyerGui().hideVanillaItemTooltip) {
            itemStack = GuiVanillaTooltipHider.buildDisplayItem(
                    sourceMaterial,
                    definition.displayMaterial(),
                    amount,
                    nameComponent,
                    lore,
                    customModelData,
                    itemIdKey,
                    definition.id()
            );
        } else {
            itemStack = ItemStack.of(visualMaterial, amount);
            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(nameComponent);
            meta.lore(lore);
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, definition.id());
            if (customModelData != null) {
                ItemStacks.applyCustomModelData(itemStack, customModelData);
            }
            itemStack.setItemMeta(meta);
        }
        applySelectionGlint(itemStack, selected);
        return itemStack;
    }

    public String readItemId(ItemStack itemStack) {
        String fromComponents = GuiVanillaTooltipHider.readPersistentString(itemStack, itemIdKey);
        if (fromComponents != null) {
            return fromComponents;
        }
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        return itemStack.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
    }

    private Component buildNameComponent(Player player, SellableItemDefinition definition, Material sourceMaterial) {
        String customNameKey = "items." + definition.id() + ".name";
        if (messageService.hasKey(player, customNameKey)) {
            return messageService.guiText(player, customNameKey);
        }
        return messageService.guiTextWithComponent(
                player,
                config.buyerGui().itemNameKey,
                "name",
                itemNameResolver.materialNameComponent(sourceMaterial)
        );
    }

    private void applySelectionGlint(ItemStack itemStack, boolean enabled) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(enabled);
            itemStack.setItemMeta(meta);
        }
    }

    private List<Component> buildLore(
            Player player,
            SellableItemDefinition definition,
            ItemUnitQuote quote,
            BuyerPayoutMode payoutMode,
            Material sourceMaterial,
            String plainName,
            boolean includeInteractionHints
    ) {
        String categoryName = messageService.raw(player, "categories." + definition.categoryId());
        String[] pairs = new String[]{
                "name", plainName,
                "item", plainName,
                "id", definition.id(),
                "item_id", definition.id(),
                "material", definition.material(),
                "price", itemNameResolver.formatMoney(quote.unitPrice()),
                "points", itemNameResolver.formatMoney(quote.unitPoints()),
                "market", itemNameResolver.formatMoney(quote.marketCoefficient()),
                "multiplier", itemNameResolver.formatMoney(quote.playerMultiplier()),
                "amount", String.valueOf(quote.inventoryAmount()),
                "category", categoryName
        };
        var buyerGui = config.buyerGui();
        String loreKey = payoutMode == BuyerPayoutMode.PLAYER_POINTS
                ? buyerGui.itemLorePlayerPointsKey
                : buyerGui.itemLoreKey;
        List<Component> lore = new ArrayList<>(messageService.guiLore(player, loreKey, pairs));
        String customLoreKey = "items." + definition.id() + ".lore";
        if (messageService.hasKey(player, customLoreKey)) {
            lore.addAll(messageService.guiLore(player, customLoreKey, pairs));
        }
        if (includeInteractionHints) {
            if (quote.inventoryAmount() > 0) {
                lore.addAll(messageService.guiLore(player, buyerGui.itemSellHintKey, pairs));
            } else {
                lore.addAll(messageService.guiLore(player, buyerGui.itemEmptyHintKey, pairs));
            }
        }
        return lore;
    }

    private String plainItemName(Player player, SellableItemDefinition definition, Material sourceMaterial) {
        String customKey = "items." + definition.id() + ".name";
        if (messageService.hasKey(player, customKey)) {
            return messageService.guiRaw(player, customKey);
        }
        return itemNameResolver.plainMaterialName(player, sourceMaterial);
    }

    private Material resolveVisualMaterial(SellableItemDefinition definition, Material sourceMaterial) {
        if (definition.displayMaterial() != null && !definition.displayMaterial().isBlank()) {
            return MaterialParser.parse(definition.displayMaterial());
        }
        return sourceMaterial;
    }
}
