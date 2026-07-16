package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessagePairUtils;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.service.SellService;
import bm.b0b0b0.soulBuyer.util.PluginSchedulers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerAutosellCategoryMenu implements SoulBuyerGuiHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private final BuyerMenuItemRenderer itemRenderer;
    private final SellService sellService;
    private final AutosellService autosellService;
    private final BuyerMenuNavigation navigation;
    private final BuyerMenuSession parentSession;
    private final String categoryId;
    private final Inventory inventory;
    private final List<Integer> contentSlots;
    private final Map<Integer, String> actions;
    private final Map<Integer, String> slotToItemId = new HashMap<>();
    private final Set<Integer> reservedSlots = new HashSet<>();
    private int page;
    private boolean closingIntentionally;

    public BuyerAutosellCategoryMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemRegistry itemRegistry,
            BuyerMenuItemRenderer itemRenderer,
            SellService sellService,
            AutosellService autosellService,
            BuyerMenuNavigation navigation,
            BuyerMenuSession parentSession,
            String categoryId,
            int page
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.itemRegistry = itemRegistry;
        this.itemRenderer = itemRenderer;
        this.sellService = sellService;
        this.autosellService = autosellService;
        this.navigation = navigation;
        this.parentSession = parentSession == null ? BuyerMenuSession.empty() : parentSession;
        this.categoryId = categoryId == null ? "" : categoryId;
        this.page = Math.max(0, page);
        this.contentSlots = new ArrayList<>(config.buyerContentSlots());
        this.actions = GuiLayoutHelper.actionBySlot(config.buyerGui().elements);
        indexReservedSlots();
        Component title = messageService.guiText(
                player,
                "gui.autosell.category-items-title",
                "category",
                messageService.raw(player, "categories." + this.categoryId)
        );
        this.inventory = Bukkit.createInventory(this, config.buyerSize(), title);
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(int rawSlot) {
        if (!autosellService.canAccess(player)) {
            return;
        }
        GuiGeneralSettings.GuiElementSettings back = config.autosellGui().elements.get("back");
        if (back != null && rawSlot == back.slot) {
            goBack();
            return;
        }
        String action = actions.getOrDefault(rawSlot, "NONE");
        if ("PAGE_PREV".equals(action)) {
            if (page > 0) {
                page--;
                render();
            }
            return;
        }
        if ("PAGE_NEXT".equals(action)) {
            if (page < maxPage()) {
                page++;
                render();
            }
            return;
        }
        String itemId = slotToItemId.get(rawSlot);
        if (itemId != null) {
            autosellService.toggleItem(player, itemId, this::render);
        }
    }

    public void onClose() {
        if (closingIntentionally || !player.isOnline()) {
            return;
        }
        PluginSchedulers.runLater(plugin, player, () -> navigation.openAutosell(player, parentSession), 1L);
    }

    private void goBack() {
        closingIntentionally = true;
        navigation.openAutosell(player, parentSession);
    }

    private void render() {
        inventory.clear();
        slotToItemId.clear();
        fillFrame();
        fillItems();
        fillNavigation();
    }

    private void fillFrame() {
        List<Integer> separators = config.buyerSeparatorSlots();
        int[] separatorSlots = new int[separators.size()];
        for (int index = 0; index < separators.size(); index++) {
            separatorSlots[index] = separators.get(index);
        }
        GuiLayoutHelper.fillBorderAndSeparators(
                inventory,
                player,
                itemFactory,
                config.buyerSize(),
                config.buyerGui().elements.get("border"),
                config.buyerGui().elements.get("separator"),
                reservedSlots::contains,
                separatorSlots
        );
    }

    private void fillItems() {
        List<SellableItemDefinition> items = itemRegistry.activeByCategory(categoryId);
        int pageSize = contentSlots.size();
        int fromIndex = page * pageSize;
        if (fromIndex >= items.size() && page > 0) {
            page = 0;
            fromIndex = 0;
        }
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        PlayerAutosellSettings settings = autosellService.settings(player);
        for (int index = fromIndex; index < toIndex; index++) {
            int slot = contentSlots.get(index - fromIndex);
            SellableItemDefinition definition = items.get(index);
            slotToItemId.put(slot, definition.id());
            inventory.setItem(slot, renderItem(settings, definition));
        }
    }

    private ItemStack renderItem(PlayerAutosellSettings settings, SellableItemDefinition definition) {
        boolean enabled = autosellService.isItemEnabled(settings, definition);
        String[] pairs = MessagePairUtils.append(
                new String[]{"id", definition.id()},
                "item_state",
                messageService.raw(player, enabled ? "gui.autosell.item-on" : "gui.autosell.item-off")
        );
        List<Component> autosellLore = messageService.guiLore(player, "gui.autosell.category-item-lore", pairs);
        return itemRenderer.renderAutosellCategoryItem(
                player,
                definition,
                sellService.unitQuote(player, definition),
                parentSession.payoutMode(),
                autosellLore,
                enabled
        );
    }

    private void fillNavigation() {
        GuiGeneralSettings.GuiElementSettings back = config.autosellGui().elements.get("back");
        if (back != null && back.slot >= 0) {
            inventory.setItem(back.slot, itemFactory.build(player, back));
        }
        Map<String, GuiGeneralSettings.GuiElementSettings> buyerElements = config.buyerGui().elements;
        GuiGeneralSettings.GuiElementSettings pagePrev = buyerElements.get("page-prev");
        GuiGeneralSettings.GuiElementSettings pageNext = buyerElements.get("page-next");
        GuiGeneralSettings.GuiElementSettings separator = buyerElements.get("separator");
        GuiGeneralSettings.GuiElementSettings border = buyerElements.get("border");
        int pages = maxPage() + 1;
        if (pagePrev != null && pagePrev.slot >= 0) {
            if (pages > 1 && page > 0) {
                inventory.setItem(pagePrev.slot, itemFactory.build(player, pagePrev));
            } else {
                inventory.setItem(pagePrev.slot, itemFactory.navigationFiller(player, separator, border));
            }
        }
        if (pageNext != null && pageNext.slot >= 0) {
            if (pages > 1 && page < maxPage()) {
                inventory.setItem(pageNext.slot, itemFactory.build(player, pageNext));
            } else {
                inventory.setItem(pageNext.slot, itemFactory.navigationFiller(player, separator, border));
            }
        }
    }

    private int maxPage() {
        int pageSize = contentSlots.size();
        if (pageSize <= 0) {
            return 0;
        }
        int count = itemRegistry.activeByCategory(categoryId).size();
        return Math.max(0, (count - 1) / pageSize);
    }

    private void indexReservedSlots() {
        reservedSlots.clear();
        reservedSlots.addAll(contentSlots);
        Map<String, GuiGeneralSettings.GuiElementSettings> buyerElements = config.buyerGui().elements;
        for (String key : List.of("page-prev", "page-next", "border", "separator")) {
            GuiGeneralSettings.GuiElementSettings element = buyerElements.get(key);
            if (element != null && element.slot >= 0) {
                reservedSlots.add(element.slot);
            }
        }
        GuiGeneralSettings.GuiElementSettings back = config.autosellGui().elements.get("back");
        if (back != null && back.slot >= 0) {
            reservedSlots.add(back.slot);
        }
    }
}
