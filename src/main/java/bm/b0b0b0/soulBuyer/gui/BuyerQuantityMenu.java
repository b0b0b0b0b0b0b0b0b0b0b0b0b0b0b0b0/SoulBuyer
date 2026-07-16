package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.config.settings.GuiQuantitySettings;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import bm.b0b0b0.soulBuyer.service.SaleDelivery;
import bm.b0b0b0.soulBuyer.service.SellService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerQuantityMenu implements SoulBuyerGuiHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private final ItemNameResolver itemNameResolver;
    private final BuyerMenuItemRenderer itemRenderer;
    private final SellService sellService;
    private final BuyerMenuNavigation navigation;
    private final BuyerMenuSession parentSession;
    private final String itemId;
    private final SellableItemDefinition definition;
    private final Inventory inventory;
    private final GuiQuantitySettings quantityGui;
    private final Map<Integer, String> actions;
    private final Set<Integer> reservedSlots = new HashSet<>();
    private int selectedAmount;
    private final int maxAmount;
    private boolean processing;
    private boolean closingIntentionally;

    public BuyerQuantityMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemRegistry itemRegistry,
            ItemNameResolver itemNameResolver,
            BuyerMenuItemRenderer itemRenderer,
            SellService sellService,
            BuyerMenuNavigation navigation,
            String itemId,
            BuyerMenuSession parentSession
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.itemRegistry = itemRegistry;
        this.itemNameResolver = itemNameResolver;
        this.itemRenderer = itemRenderer;
        this.sellService = sellService;
        this.navigation = navigation;
        this.parentSession = parentSession == null ? BuyerMenuSession.empty() : parentSession;
        this.itemId = itemId;
        this.quantityGui = config.quantityGui();
        Optional<SellableItemDefinition> resolved = itemRegistry.byId(itemId);
        if (resolved.isEmpty()) {
            throw new IllegalStateException("Unknown sellable item: " + itemId);
        }
        this.definition = resolved.get();
        this.maxAmount = Math.max(0, InventorySellHelper.countMatching(player, itemRegistry, itemId));
        this.selectedAmount = maxAmount > 0 ? 1 : 0;
        this.actions = GuiLayoutHelper.actionBySlot(quantityGui.elements);
        indexReservedSlots();
        Component title = messageService.guiTextWithComponent(
                player,
                quantityGui.titleKey,
                "item",
                itemNameComponent(player)
        );
        this.inventory = Bukkit.createInventory(this, quantityGui.size, title);
        render();
        sellService.preloadSellLimitUsage(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean isProcessing() {
        return processing || sellService.isProcessing(player.getUniqueId());
    }

    public void handleClick(int rawSlot) {
        if (isProcessing() || maxAmount <= 0) {
            return;
        }
        String action = actions.getOrDefault(rawSlot, "NONE");
        switch (action) {
            case "QTY_MINUS_1" -> adjustAmount(-1);
            case "QTY_PLUS_1" -> adjustAmount(1);
            case "QTY_MINUS_5" -> adjustAmount(-5);
            case "QTY_PLUS_5" -> adjustAmount(5);
            case "QTY_MINUS_10" -> adjustAmount(-10);
            case "QTY_PLUS_10" -> adjustAmount(10);
            case "QTY_SET_1" -> setAmount(1);
            case "QTY_SET_5" -> setAmount(5);
            case "QTY_SET_10" -> setAmount(10);
            case "QTY_CONFIRM" -> confirmSell();
            case "QTY_SELL_ALL" -> sellAll();
            case "QTY_BACK" -> goBack();
            default -> {
            }
        }
    }

    public void onClose() {
        BuyerSubMenuNavigation.onUnexpectedClose(plugin, navigation, player, parentSession, closingIntentionally);
    }

    private void goBack() {
        BuyerSubMenuNavigation.goBack(navigation, player, parentSession, () -> closingIntentionally = true);
    }

    private void confirmSell() {
        if (selectedAmount <= 0) {
            return;
        }
        processing = true;
        sellService.sellItemAmountFromInventory(
                player,
                itemId,
                selectedAmount,
                SaleDelivery.CHAT,
                () -> {
                    processing = false;
                    closingIntentionally = true;
                    if (player.isOnline()) {
                        navigation.openBuyer(player, parentSession);
                    }
                },
                parentSession.payoutMode()
        );
    }

    private void sellAll() {
        if (maxAmount <= 0) {
            return;
        }
        processing = true;
        sellService.sellItemFromInventory(player, itemId, parentSession.payoutMode(), () -> {
            processing = false;
            closingIntentionally = true;
            if (player.isOnline()) {
                navigation.openBuyer(player, parentSession);
            }
        });
    }

    private void adjustAmount(int delta) {
        setAmount(selectedAmount + delta);
    }

    private void setAmount(int amount) {
        if (maxAmount <= 0) {
            selectedAmount = 0;
            render();
            return;
        }
        selectedAmount = Math.max(1, Math.min(maxAmount, amount));
        render();
    }

    private void render() {
        inventory.clear();
        fillFrame();
        fillControls();
        fillPreview();
        fillAmountInfo();
    }

    private void fillFrame() {
        int[] separatorSlots = quantityGui.separatorSlots.stream().mapToInt(Integer::intValue).toArray();
        GuiLayoutHelper.fillBorderAndSeparators(
                inventory,
                player,
                itemFactory,
                quantityGui.size,
                quantityGui.elements.get("border"),
                quantityGui.elements.get("separator"),
                reservedSlots::contains,
                separatorSlots
        );
    }

    private void fillControls() {
        String[] pairs = amountPairs();
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : quantityGui.elements.entrySet()) {
            String key = entry.getKey();
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot < 0 || "border".equals(key) || "separator".equals(key) || "amount-info".equals(key)) {
                continue;
            }
            if (element.slot == quantityGui.previewSlot) {
                continue;
            }
            inventory.setItem(element.slot, itemFactory.build(player, element, pairs));
        }
    }

    private void fillPreview() {
        if (maxAmount <= 0) {
            return;
        }
        ItemUnitQuote unitQuote = sellService.unitQuote(player, definition);
        ItemStack preview = itemRenderer.render(player, definition, unitQuote, parentSession.payoutMode());
        preview.setAmount(Math.max(1, Math.min(64, selectedAmount)));
        inventory.setItem(quantityGui.previewSlot, preview);
    }

    private void fillAmountInfo() {
        GuiGeneralSettings.GuiElementSettings element = quantityGui.elements.get("amount-info");
        if (element == null) {
            return;
        }
        inventory.setItem(quantityGui.amountInfoSlot, itemFactory.build(player, element, amountPairs()));
    }

    private String[] amountPairs() {
        ItemUnitQuote unitQuote = sellService.unitQuote(player, definition);
        double totalMoney = unitQuote.unitPrice() * selectedAmount;
        double totalPoints = unitQuote.unitPoints() * selectedAmount;
        return new String[]{
                "amount", String.valueOf(selectedAmount),
                "max", String.valueOf(maxAmount),
                "price", itemNameResolver.formatMoney(totalMoney),
                "points", itemNameResolver.formatMoney(totalPoints),
                "unit_price", itemNameResolver.formatMoney(unitQuote.unitPrice()),
                "unit_points", itemNameResolver.formatMoney(unitQuote.unitPoints())
        };
    }

    private Component itemNameComponent(Player player) {
        Material material = MaterialParser.parse(definition.material());
        return itemNameResolver.displayComponent(player, ItemStack.of(material));
    }

    private void indexReservedSlots() {
        reservedSlots.clear();
        reservedSlots.add(quantityGui.previewSlot);
        reservedSlots.add(quantityGui.amountInfoSlot);
        for (GuiGeneralSettings.GuiElementSettings element : quantityGui.elements.values()) {
            if (element.slot >= 0) {
                reservedSlots.add(element.slot);
            }
        }
    }
}
