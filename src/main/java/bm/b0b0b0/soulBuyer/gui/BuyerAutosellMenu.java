package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.autosell.AutosellNotify;
import bm.b0b0b0.soulBuyer.autosell.AutosellPayout;
import bm.b0b0b0.soulBuyer.autosell.AutosellService;
import bm.b0b0b0.soulBuyer.autosell.AutosellTrigger;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiAutosellSettings;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class BuyerAutosellMenu implements InventoryHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final AutosellService autosellService;
    private final BuyerMenuNavigation navigation;
    private final BuyerMenuSession parentSession;
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
            AutosellService autosellService,
            BuyerMenuNavigation navigation,
            BuyerMenuSession parentSession
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.autosellService = autosellService;
        this.navigation = navigation;
        this.parentSession = parentSession == null ? BuyerMenuSession.empty() : parentSession;
        this.autosellGui = config.autosellGui();
        this.actions = GuiLayoutHelper.actionBySlot(autosellGui.elements);
        indexCategories();
        indexReservedSlots();
        Component title = messageService.guiText(player, autosellGui.titleKey);
        this.inventory = Bukkit.createInventory(this, autosellGui.size, title);
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
        String action = actions.getOrDefault(rawSlot, "NONE");
        switch (action) {
            case "AUTO_TOGGLE" -> autosellService.toggleEnabled(player, this::render);
            case "AUTO_TRIGGER" -> autosellService.cycleTrigger(player, this::render);
            case "AUTO_NOTIFY" -> autosellService.cycleNotify(player, this::render);
            case "AUTO_MIN_PRICE" -> autosellService.cycleMinUnitPrice(player, this::render);
            case "AUTO_PAYOUT" -> autosellService.cyclePayout(player, this::render);
            case "AUTO_CATEGORY" -> autosellService.toggleCategory(
                    player,
                    categoryBySlot.getOrDefault(rawSlot, ""),
                    this::render
            );
            case "AUTO_BACK" -> goBack();
            default -> {
            }
        }
    }

    public void onClose() {
        if (closingIntentionally || !player.isOnline()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> navigation.openBuyer(player, parentSession));
    }

    private void goBack() {
        closingIntentionally = true;
        navigation.openBuyer(player, parentSession);
    }

    private void render() {
        inventory.clear();
        fillFrame();
        fillControls();
    }

    private void fillFrame() {
        GuiGeneralSettings.GuiElementSettings border = autosellGui.elements.get("border");
        GuiGeneralSettings.GuiElementSettings separator = autosellGui.elements.get("separator");
        if (border == null) {
            return;
        }
        for (int slot : GuiLayoutHelper.frameSlots(autosellGui.size)) {
            if (reservedSlots.contains(slot)) {
                continue;
            }
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        if (separator != null) {
            for (int slot : List.of(10, 11, 12, 13, 14, 15, 16)) {
                if (!reservedSlots.contains(slot)) {
                    inventory.setItem(slot, itemFactory.filler(player, separator));
                }
            }
        }
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
                boolean selected = settings.categories().contains(categoryId);
                String[] categoryPairs = mergePairs(
                        pairs,
                        "category_state",
                        messageService.raw(
                                player,
                                selected ? "gui.autosell.category-on" : "gui.autosell.category-off"
                        )
                );
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

    private String[] settingsPairs(PlayerAutosellSettings settings) {
        return new String[]{
                "state", messageService.raw(player, settings.enabled() ? "gui.autosell.state-on" : "gui.autosell.state-off"),
                "trigger", messageService.raw(player, triggerKey(settings.trigger())),
                "notify", messageService.raw(player, notifyKey(settings.notifyMode())),
                "min_price", String.valueOf(settings.minUnitPrice()),
                "payout", messageService.raw(player, payoutKey(settings.payoutTarget()))
        };
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

    private String[] mergePairs(String[] base, String key, String value) {
        String[] merged = new String[base.length + 2];
        System.arraycopy(base, 0, merged, 0, base.length);
        merged[base.length] = key;
        merged[base.length + 1] = value;
        return merged;
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
