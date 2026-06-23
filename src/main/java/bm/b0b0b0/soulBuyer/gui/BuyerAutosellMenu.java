package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.autosell.AutosellPayout;
import bm.b0b0b0.soulBuyer.autosell.AutosellNotify;
import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.autosell.AutosellTrigger;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiAutosellSettings;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessagePairUtils;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerAutosellMenu implements SoulBuyerGuiHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private final AutosellService autosellService;
    private final BuyerMenuNavigation navigation;
    private final BuyerMenuSession parentSession;
    private final BuyerCategoryIconAnimator categoryIconAnimator;
    private final Inventory inventory;
    private final GuiAutosellSettings autosellGui;
    private final Map<Integer, String> actions;
    private final Map<Integer, String> categoryBySlot = new HashMap<>();
    private final Set<Integer> reservedSlots = new HashSet<>();
    private boolean closingIntentionally;

    public BuyerAutosellMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemRegistry itemRegistry,
            AutosellService autosellService,
            BuyerMenuNavigation navigation,
            BuyerMenuSession parentSession
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.itemRegistry = itemRegistry;
        this.autosellService = autosellService;
        this.navigation = navigation;
        this.parentSession = parentSession == null ? BuyerMenuSession.empty() : parentSession;
        this.autosellGui = config.autosellGui();
        this.actions = GuiLayoutHelper.actionBySlot(autosellGui.elements);
        indexCategories();
        indexReservedSlots();
        Component title = messageService.guiText(player, autosellGui.titleKey);
        this.inventory = Bukkit.createInventory(this, autosellGui.size, title);
        this.categoryIconAnimator = new BuyerCategoryIconAnimator(
                plugin,
                player,
                config,
                itemRegistry,
                itemFactory,
                inventory,
                autosellGui.elements,
                "AUTO_CATEGORY"
        );
        this.categoryIconAnimator.bind(
                this::isCategoryEnabled,
                this::categoryItemPairs,
                () -> false,
                () -> player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerAutosellMenu
        );
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(int rawSlot, boolean rightClick) {
        if (!autosellService.canAccess(player)) {
            return;
        }
        String action = actions.getOrDefault(rawSlot, "NONE");
        switch (action) {
            case "AUTO_TOGGLE" -> autosellService.toggleEnabled(player, this::render);
            case "AUTO_TRIGGER" -> autosellService.cycleTrigger(player, this::render);
            case "AUTO_NOTIFY" -> autosellService.cycleNotify(player, this::render);
            case "AUTO_MIN_PRICE" -> autosellService.cycleMinUnitPrice(player, this::render);
            case "AUTO_PAYOUT" -> autosellService.cyclePayout(player, this::render);
            case "AUTO_CATEGORY" -> {
                String categoryId = categoryBySlot.getOrDefault(rawSlot, "");
                if (categoryId.isBlank()) {
                    return;
                }
                if (rightClick) {
                    categoryIconAnimator.stop();
                    closingIntentionally = true;
                    navigation.openAutosellCategory(player, categoryId, parentSession);
                    return;
                }
                autosellService.toggleCategory(player, categoryId, this::render);
            }
            case "AUTO_BACK" -> goBack();
            default -> {
            }
        }
    }

    public void onClose() {
        categoryIconAnimator.stop();
        BuyerSubMenuNavigation.onUnexpectedClose(plugin, navigation, player, parentSession, closingIntentionally);
    }

    private void goBack() {
        BuyerSubMenuNavigation.goBack(navigation, player, parentSession, () -> closingIntentionally = true);
    }

    private void render() {
        inventory.clear();
        fillFrame();
        fillControls();
        categoryIconAnimator.onMenuRendered();
    }

    private void fillFrame() {
        GuiLayoutHelper.fillBorderAndSeparators(
                inventory,
                player,
                itemFactory,
                autosellGui.size,
                autosellGui.elements.get("border"),
                autosellGui.elements.get("separator"),
                reservedSlots::contains,
                new int[]{10, 11, 12, 13, 14, 15, 16}
        );
    }

    private void fillControls() {
        PlayerAutosellSettings settings = autosellService.settings(player);
        String[] pairs = settingsPairs(settings);
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : autosellGui.elements.entrySet()) {
            String key = entry.getKey();
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot < 0 || "border".equals(key) || "separator".equals(key)) {
                continue;
            }
            if ("toggle".equals(key)) {
                Material material = settings.enabled() ? Material.LIME_DYE : Material.GRAY_DYE;
                inventory.setItem(element.slot, itemFactory.buildSelectedMaterial(player, element, material, pairs));
                continue;
            }
            if ("trigger".equals(key)) {
                Material material = triggerMaterial(settings.trigger());
                inventory.setItem(element.slot, itemFactory.buildSelectedMaterial(player, element, material, pairs));
                continue;
            }
            if ("payout".equals(key)) {
                if (!autosellService.payoutChoiceAvailable()) {
                    continue;
                }
                Material material = payoutMaterial(settings.payoutTarget());
                inventory.setItem(element.slot, itemFactory.buildSelectedMaterial(player, element, material, pairs));
                continue;
            }
            if ("AUTO_CATEGORY".equals(element.action)) {
                String categoryId = element.categoryFilter == null ? "" : element.categoryFilter;
                if (categoryId.isBlank()) {
                    continue;
                }
                if (config.categoryIconAnimation().enabled) {
                    continue;
                }
                boolean selected = isCategoryEnabled(categoryId);
                String[] categoryPairs = categoryItemPairs(categoryId);
                inventory.setItem(
                        element.slot,
                        selected
                                ? itemFactory.buildSelected(player, element, categoryPairs)
                                : itemFactory.build(player, element, categoryPairs)
                );
                continue;
            }
            inventory.setItem(element.slot, itemFactory.build(player, element, pairs));
        }
    }

    private boolean isCategoryEnabled(String categoryId) {
        return autosellService.settings(player).categories().contains(categoryId);
    }

    private String[] categoryItemPairs(String categoryId) {
        PlayerAutosellSettings settings = autosellService.settings(player);
        boolean selected = settings.categories().contains(categoryId);
        return MessagePairUtils.append(
                settingsPairs(settings),
                "category_state",
                messageService.raw(
                        player,
                        selected ? "gui.autosell.category-on" : "gui.autosell.category-off"
                )
        );
    }

    private String[] settingsPairs(PlayerAutosellSettings settings) {
        String[] pairs = new String[]{
                "state", messageService.raw(player, settings.enabled() ? "gui.autosell.state-on" : "gui.autosell.state-off"),
                "trigger", messageService.raw(player, triggerKey(settings.trigger())),
                "notify", messageService.raw(player, notifyKey(settings.notifyMode())),
                "min_price", String.valueOf(settings.minUnitPrice()),
                "payout", messageService.raw(player, payoutKey(settings.payoutTarget()))
        };
        return pairs;
    }

    private String triggerKey(String trigger) {
        if (AutosellTrigger.isBuyer(trigger)) {
            return "gui.autosell.trigger-buyer";
        }
        if (AutosellTrigger.isChest(trigger)) {
            return "gui.autosell.trigger-chest";
        }
        return "gui.autosell.trigger-pickup";
    }

    private Material triggerMaterial(String trigger) {
        if (AutosellTrigger.isBuyer(trigger)) {
            return Material.EMERALD;
        }
        if (AutosellTrigger.isChest(trigger)) {
            return Material.CHEST;
        }
        return Material.HOPPER;
    }

    private String payoutKey(String payout) {
        if (AutosellPayout.isPlayerPoints(payout)) {
            return "gui.autosell.payout-playerpoints";
        }
        return "gui.autosell.payout-vault";
    }

    private Material payoutMaterial(String payout) {
        if (AutosellPayout.isPlayerPoints(payout)) {
            return Material.AMETHYST_SHARD;
        }
        return Material.GOLD_INGOT;
    }

    private String notifyKey(String notify) {
        return switch (AutosellNotify.normalize(notify)) {
            case AutosellNotify.CHAT -> "gui.autosell.notify-chat";
            case AutosellNotify.OFF -> "gui.autosell.notify-off";
            default -> "gui.autosell.notify-actionbar";
        };
    }

    private void indexCategories() {
        categoryBySlot.clear();
        for (GuiGeneralSettings.GuiElementSettings element : autosellGui.elements.values()) {
            if ("AUTO_CATEGORY".equals(element.action) && element.slot >= 0) {
                categoryBySlot.put(element.slot, element.categoryFilter == null ? "" : element.categoryFilter);
            }
        }
    }

    private void indexReservedSlots() {
        reservedSlots.clear();
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : autosellGui.elements.entrySet()) {
            if ("payout".equals(entry.getKey()) && !autosellService.payoutChoiceAvailable()) {
                continue;
            }
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot >= 0) {
                reservedSlots.add(element.slot);
            }
        }
    }
}
