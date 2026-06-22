package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.autosell.AutosellPayout;
import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import bm.b0b0b0.soulBuyer.service.SellService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class BuyerMenu implements InventoryHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private final ItemNameResolver itemNameResolver;
    private final BuyerMenuItemRenderer itemRenderer;
    private final SellService sellService;
    private final BuyerStatsService buyerStatsService;
    private final AutosellService autosellService;
    private final BoosterService boosterService;
    private final BuyerMenuNavigation navigation;
    private final BuyerCategoryIconAnimator categoryIconAnimator;
    private final BuyerMenuLiveStatsUpdater liveStatsUpdater;
    private final Inventory inventory;
    private final List<Integer> contentSlots;
    private final List<Integer> separatorSlots;
    private final Map<Integer, String> actions;
    private final Map<Integer, String> categoryBySlot = new HashMap<>();
    private final Map<Integer, String> slotToItemId = new HashMap<>();
    private String categoryFilter = "";
    private String sortMode = BuyerSortMode.PRICE_DESC;
    private int page;
    private final BuyerPayoutMode payoutMode;
    private boolean processing;
    private boolean closingIntentionally;

    public BuyerMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemRegistry itemRegistry,
            ItemNameResolver itemNameResolver,
            BuyerMenuItemRenderer itemRenderer,
            SellService sellService,
            BuyerStatsService buyerStatsService,
            AutosellService autosellService,
            BoosterService boosterService,
            BuyerMenuNavigation navigation,
            BuyerMenuSession session
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
        this.buyerStatsService = buyerStatsService;
        this.autosellService = autosellService;
        this.boosterService = boosterService;
        this.navigation = navigation;
        if (session != null) {
            this.categoryFilter = session.categoryFilter() == null ? "" : session.categoryFilter();
            this.sortMode = BuyerSortMode.normalize(
                    session.sortMode() == null || session.sortMode().isBlank()
                            ? BuyerSortMode.PRICE_DESC
                            : session.sortMode()
            );
            this.page = Math.max(0, session.page());
            this.payoutMode = session.payoutMode() == null ? BuyerPayoutMode.VAULT : session.payoutMode();
        } else {
            this.payoutMode = BuyerPayoutMode.VAULT;
        }
        this.contentSlots = new ArrayList<>(config.buyerContentSlots());
        this.separatorSlots = new ArrayList<>(config.buyerSeparatorSlots());
        this.actions = GuiLayoutHelper.actionBySlot(config.buyerGui().elements);
        indexCategoryButtons();
        Component title = messageService.guiText(player, config.buyerTitleKey(payoutMode));
        this.inventory = Bukkit.createInventory(this, config.buyerSize(), title);
        GuiGeneralSettings.GuiElementSettings marketStatsElement = config.buyerGui().elements.get("market-stats");
        this.liveStatsUpdater = new BuyerMenuLiveStatsUpdater(
                plugin,
                player,
                buyerStatsService,
                itemFactory,
                inventory,
                marketStatsElement,
                marketStatsElement == null ? 49 : marketStatsElement.slot
        );
        this.categoryIconAnimator = new BuyerCategoryIconAnimator(
                plugin,
                player,
                config,
                itemRegistry,
                itemFactory,
                inventory,
                config.buyerGui().elements
        );
        this.categoryIconAnimator.bind(() -> categoryFilter, this::isProcessing);
        render();
        liveStatsUpdater.start();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player player() {
        return player;
    }

    public boolean isProcessing() {
        return processing || sellService.isProcessing(player.getUniqueId());
    }

    public String actionAt(int slot) {
        return actions.getOrDefault(slot, "NONE");
    }

    public BuyerMenuSession session() {
        return new BuyerMenuSession(categoryFilter, sortMode, page, payoutMode);
    }

    public void handleClick(int rawSlot, boolean rightClick) {
        if (isProcessing()) {
            return;
        }
        String itemId = slotToItemId.get(rawSlot);
        if (itemId != null) {
            if (rightClick) {
                openQuantity(itemId);
            } else {
                sellItem(itemId);
            }
            return;
        }
        handleControlClick(rawSlot, rightClick);
    }

    private void openQuantity(String itemId) {
        if (InventorySellHelper.countMatching(player, itemRegistry, itemId) <= 0) {
            return;
        }
        categoryIconAnimator.stop();
        liveStatsUpdater.stop();
        closingIntentionally = true;
        navigation.openQuantity(player, itemId, session());
    }

    private void handleAutosellClick(boolean rightClick) {
        if (!autosellService.featureEnabled()) {
            return;
        }
        if (!autosellService.canAccess(player)) {
            messageService.send(player, "autosell.no-permission");
            return;
        }
        if (rightClick) {
            autosellService.toggleEnabled(player, this::refreshIfOpen);
            return;
        }
        categoryIconAnimator.stop();
        liveStatsUpdater.stop();
        closingIntentionally = true;
        navigation.openAutosell(player, session());
    }

    private void handleBoostersClick() {
        if (!boosterService.featureEnabled()) {
            return;
        }
        if (!boosterService.canAccess(player)) {
            messageService.send(player, "boosters.no-permission");
            return;
        }
        categoryIconAnimator.stop();
        liveStatsUpdater.stop();
        closingIntentionally = true;
        navigation.openBoosters(player, session());
    }

    private void handleControlClick(int rawSlot, boolean rightClick) {
        String action = actionAt(rawSlot);
        if ("AUTOSELL".equals(action)) {
            handleAutosellClick(rightClick);
            return;
        }
        if ("BOOSTERS".equals(action)) {
            handleBoostersClick();
            return;
        }
        switch (action) {
            case "SELL_ALL" -> sellAll();
            case "PAGE_PREV" -> changePage(-1);
            case "PAGE_NEXT" -> changePage(1);
            case "CATEGORY_FILTER" -> selectCategory(categoryBySlot.getOrDefault(rawSlot, ""));
            case "SORT_CYCLE" -> cycleSort();
            default -> {
            }
        }
    }

    public void refresh() {
        if (!isProcessing()) {
            render();
        }
    }

    public void onClose() {
        categoryIconAnimator.stop();
        liveStatsUpdater.stop();
        if (closingIntentionally) {
            return;
        }
        if (isProcessing()) {
            sellService.abortAndReturn(player);
            processing = false;
        }
    }

    private void sellItem(String itemId) {
        processing = true;
        sellService.sellItemFromInventory(player, itemId, payoutMode, () -> Bukkit.getScheduler().runTask(plugin, () -> {
            processing = false;
            refreshIfOpen();
        }));
    }

    private void sellAll() {
        processing = true;
        sellService.sellAllFromInventory(player, payoutMode, () -> Bukkit.getScheduler().runTask(plugin, () -> {
            processing = false;
            refreshIfOpen();
        }));
    }

    private void changePage(int delta) {
        int pages = totalPages();
        if (pages <= 1) {
            return;
        }
        int nextPage = page + delta;
        if (nextPage < 0 || nextPage >= pages) {
            return;
        }
        page = nextPage;
        render();
    }

    private void cycleSort() {
        sortMode = BuyerSortMode.nextInCycle(sortMode);
        page = 0;
        render();
    }

    private void selectCategory(String categoryId) {
        categoryFilter = categoryId == null ? "" : categoryId;
        page = 0;
        render();
    }

    private void refreshIfOpen() {
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerMenu) {
            render();
        }
    }

    private void render() {
        slotToItemId.clear();
        inventory.clear();
        fillFrame();
        fillSeparator();
        fillControls();
        fillItems();
        categoryIconAnimator.bind(() -> categoryFilter, this::isProcessing);
        categoryIconAnimator.onMenuRendered();
    }

    private void fillFrame() {
        GuiGeneralSettings.GuiElementSettings border = config.buyerGui().elements.get("border");
        if (border == null) {
            return;
        }
        Set<Integer> reserved = reservedSlots();
        for (int slot : GuiLayoutHelper.frameSlots(config.buyerSize())) {
            if (reserved.contains(slot)) {
                continue;
            }
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
    }

    private void fillSeparator() {
        GuiGeneralSettings.GuiElementSettings separator = config.buyerGui().elements.get("separator");
        if (separator == null) {
            return;
        }
        for (int slot : separatorSlots) {
            inventory.setItem(slot, itemFactory.filler(player, separator));
        }
    }

    private Set<Integer> reservedSlots() {
        Set<Integer> reserved = new java.util.HashSet<>(contentSlots);
        reserved.addAll(separatorSlots);
        reserved.addAll(actions.keySet());
        return reserved;
    }

    private void fillControls() {
        PlayerProgress progress = sellService.cachedProgress(player);
        String[] statsPairs = new String[]{
                "points", itemNameResolver.formatMoney(progress.points()),
                "multiplier", itemNameResolver.formatMoney(playerMultiplierDisplay())
        };
        int pages = totalPages();
        String[] marketStatsPairs = buyerStatsService.guiPairs(player);

        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : config.buyerGui().elements.entrySet()) {
            String key = entry.getKey();
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot < 0 || "border".equals(key) || "separator".equals(key)) {
                continue;
            }
            if (contentSlots.contains(element.slot) || separatorSlots.contains(element.slot)) {
                continue;
            }
            if ("page-prev".equals(key)) {
                if (pages > 1 && page > 0) {
                    inventory.setItem(element.slot, itemFactory.build(player, element));
                } else {
                    inventory.setItem(element.slot, pagePlaceholder());
                }
                continue;
            }
            if ("page-next".equals(key)) {
                if (pages > 1 && page < pages - 1) {
                    inventory.setItem(element.slot, itemFactory.build(player, element));
                } else {
                    inventory.setItem(element.slot, pagePlaceholder());
                }
                continue;
            }
            if ("market-stats".equals(key)) {
                if (element == null) {
                    continue;
                }
                inventory.setItem(element.slot, itemFactory.build(player, element, marketStatsPairs));
                continue;
            }
            if ("page-info".equals(key)) {
                continue;
            }
            if ("stats".equals(key)) {
                inventory.setItem(element.slot, itemFactory.build(player, element, statsPairs));
                continue;
            }
            if ("autosell".equals(key) || "autosell-disabled".equals(key)) {
                continue;
            }
            if ("boosters".equals(key) || "boosters-disabled".equals(key)) {
                continue;
            }
            if ("sort-cycle".equals(key)) {
                continue;
            }
            if ("CATEGORY_FILTER".equals(element.action)) {
                String categoryId = element.categoryFilter == null ? "" : element.categoryFilter;
                if (config.categoryIconAnimation().enabled && !categoryId.isEmpty()) {
                    continue;
                }
                boolean active = categoryFilter.equals(categoryId);
                inventory.setItem(
                        element.slot,
                        active ? itemFactory.buildSelected(player, element) : itemFactory.build(player, element)
                );
                continue;
            }
            inventory.setItem(element.slot, itemFactory.build(player, element));
        }
        fillSortButton();
        fillAutosellButton(statsPairs);
        fillBoostersButton();
    }

    private void fillSortButton() {
        GuiGeneralSettings.GuiElementSettings sortElement = config.buyerGui().elements.get("sort-cycle");
        if (sortElement == null || sortElement.slot < 0) {
            return;
        }
        String sortLabel = messageService.raw(player, BuyerSortMode.labelKey(sortMode));
        String[] pairs = new String[]{"sort", sortLabel};
        inventory.setItem(sortElement.slot, itemFactory.buildSelected(player, sortElement, pairs));
    }

    private void fillBoostersButton() {
        GuiGeneralSettings.GuiElementSettings boostersElement = config.buyerGui().elements.get("boosters");
        if (boostersElement == null || boostersElement.slot < 0) {
            return;
        }
        if (!boosterService.featureEnabled()) {
            GuiGeneralSettings.GuiElementSettings disabled = config.buyerGui().elements.get("boosters-disabled");
            if (disabled != null) {
                inventory.setItem(boostersElement.slot, itemFactory.build(player, disabled));
            }
            return;
        }
        if (!boosterService.canAccess(player)) {
            GuiGeneralSettings.GuiElementSettings locked = new GuiGeneralSettings.GuiElementSettings();
            locked.slot = boostersElement.slot;
            locked.material = boostersElement.material;
            locked.nameKey = boostersElement.nameKey;
            locked.loreKeys = List.of("gui.buyer.boosters-no-access-lore");
            locked.action = boostersElement.action;
            inventory.setItem(boostersElement.slot, itemFactory.build(player, locked));
            return;
        }
        inventory.setItem(boostersElement.slot, itemFactory.build(player, boostersElement));
    }

    private void fillAutosellButton(String[] statsPairs) {
        GuiGeneralSettings.GuiElementSettings autosellElement = config.buyerGui().elements.get("autosell");
        if (autosellElement == null || autosellElement.slot < 0) {
            return;
        }
        if (!autosellService.featureEnabled()) {
            GuiGeneralSettings.GuiElementSettings disabled = config.buyerGui().elements.get("autosell-disabled");
            if (disabled != null) {
                inventory.setItem(autosellElement.slot, itemFactory.build(player, disabled));
            }
            return;
        }
        if (!autosellService.canAccess(player)) {
            GuiGeneralSettings.GuiElementSettings lockedView = lockedAutosellView(autosellElement);
            inventory.setItem(autosellElement.slot, itemFactory.build(player, lockedView));
            return;
        }
        PlayerAutosellSettings autosellSettings = autosellService.settings(player);
        String payoutLabelKey = autosellService.payoutChoiceAvailable()
                ? autosellPayoutKey(autosellSettings.payoutTarget())
                : autosellPayoutKey(
                config.defaultOpenPayoutMode() == BuyerPayoutMode.PLAYER_POINTS
                ? AutosellPayout.PLAYER_POINTS
                : AutosellPayout.VAULT
        );
        String[] autosellPairs = mergePairs(
                new String[]{
                        "autosell_state",
                        messageService.raw(player, autosellSettings.enabled()
                                ? "gui.buyer.autosell-on"
                                : "gui.buyer.autosell-off")
                },
                new String[]{
                        "autosell_payout",
                        messageService.raw(player, payoutLabelKey)
                }
        );
        String[] pairs = mergePairs(statsPairs, autosellPairs);
        if (autosellSettings.enabled()) {
            inventory.setItem(autosellElement.slot, itemFactory.buildSelected(player, autosellElement, pairs));
        } else {
            inventory.setItem(autosellElement.slot, itemFactory.build(player, autosellElement, pairs));
        }
    }

    private GuiGeneralSettings.GuiElementSettings lockedAutosellView(
            GuiGeneralSettings.GuiElementSettings autosellElement
    ) {
        GuiGeneralSettings.GuiElementSettings lockedView = new GuiGeneralSettings.GuiElementSettings();
        lockedView.slot = autosellElement.slot;
        lockedView.material = autosellElement.material;
        lockedView.nameKey = autosellElement.nameKey;
        lockedView.loreKeys = List.of("gui.buyer.autosell-no-access-lore");
        lockedView.action = autosellElement.action;
        return lockedView;
    }

    private String autosellPayoutKey(String payoutTarget) {
        if (AutosellPayout.isPlayerPoints(payoutTarget)) {
            return "gui.buyer.autosell-payout-playerpoints";
        }
        return "gui.buyer.autosell-payout-vault";
    }

    private String[] mergePairs(String[] first, String[] second) {
        String[] merged = new String[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }

    private double playerMultiplierDisplay() {
        return itemRegistry.all().stream()
                .findFirst()
                .map(definition -> sellService.unitQuote(player, definition).playerMultiplier())
                .orElse(1.0D);
    }

    private void fillItems() {
        List<SellableItemDefinition> items = sortedItems();
        int pageSize = contentSlots.size();
        int fromIndex = page * pageSize;
        if (fromIndex >= items.size() && page > 0) {
            page = 0;
            fromIndex = 0;
        }
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        for (int index = fromIndex; index < toIndex; index++) {
            int slot = contentSlots.get(index - fromIndex);
            SellableItemDefinition definition = items.get(index);
            slotToItemId.put(slot, definition.id());
            inventory.setItem(slot, itemRenderer.render(player, definition, sellService.unitQuote(player, definition), payoutMode));
        }
    }

    private List<SellableItemDefinition> sortedItems() {
        List<SellableItemDefinition> items = new ArrayList<>(itemRegistry.all());
        if (!categoryFilter.isEmpty()) {
            items.removeIf(definition -> !categoryFilter.equals(definition.categoryId()));
        }
        items.sort(currentComparator());
        return items;
    }

    private Comparator<SellableItemDefinition> currentComparator() {
        if (BuyerSortMode.PRICE_DESC.equals(sortMode)) {
            return Comparator.comparingDouble(this::unitPrice).reversed()
                    .thenComparing(SellableItemDefinition::id);
        }
        if (BuyerSortMode.PRICE_ASC.equals(sortMode)) {
            return Comparator.comparingDouble(this::unitPrice)
                    .thenComparing(SellableItemDefinition::id);
        }
        if (BuyerSortMode.POINTS_DESC.equals(sortMode)) {
            return Comparator.comparingDouble(this::unitPoints).reversed()
                    .thenComparing(SellableItemDefinition::id);
        }
        if (BuyerSortMode.POINTS_ASC.equals(sortMode)) {
            return Comparator.comparingDouble(this::unitPoints)
                    .thenComparing(SellableItemDefinition::id);
        }
        if (BuyerSortMode.INVENTORY_DESC.equals(sortMode)) {
            return Comparator.comparingInt(this::inventoryAmount).reversed()
                    .thenComparing(SellableItemDefinition::id);
        }
        return Comparator
                .comparing((SellableItemDefinition definition) -> categoryOrder(definition.categoryId()))
                .thenComparing(SellableItemDefinition::id);
    }

    private double unitPrice(SellableItemDefinition definition) {
        return sellService.unitQuote(player, definition).unitPrice();
    }

    private double unitPoints(SellableItemDefinition definition) {
        return sellService.unitQuote(player, definition).unitPoints();
    }

    private int inventoryAmount(SellableItemDefinition definition) {
        return sellService.unitQuote(player, definition).inventoryAmount();
    }

    private int categoryOrder(String categoryId) {
        SoulBuyerSettings.CategorySettings settings = config.categories().get(categoryId);
        return settings == null ? Integer.MAX_VALUE : settings.order;
    }

    private int totalPages() {
        int pageSize = Math.max(1, contentSlots.size());
        int count = sortedItems().size();
        return Math.max(1, (count + pageSize - 1) / pageSize);
    }

    private void indexCategoryButtons() {
        for (GuiGeneralSettings.GuiElementSettings element : config.buyerGui().elements.values()) {
            if ("CATEGORY_FILTER".equals(element.action) && element.slot >= 0) {
                categoryBySlot.put(element.slot, element.categoryFilter == null ? "" : element.categoryFilter);
            }
        }
    }

    private org.bukkit.inventory.ItemStack pagePlaceholder() {
        GuiGeneralSettings.GuiElementSettings separator = config.buyerGui().elements.get("separator");
        if (separator != null) {
            return itemFactory.filler(player, separator);
        }
        GuiGeneralSettings.GuiElementSettings border = config.buyerGui().elements.get("border");
        return itemFactory.filler(player, border);
    }
}
