package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.booster.BoosterDurationFormatter;
import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiBoostersSettings;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.BoosterType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BuyerBoostersMenu implements InventoryHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final ItemNameResolver itemNameResolver;
    private final BoosterService boosterService;
    private final BuyerMenuNavigation navigation;
    private final BuyerMenuSession parentSession;
    private final Inventory inventory;
    private final GuiBoostersSettings boostersGui;
    private final Map<Integer, String> actions;
    private final Map<Integer, String> offerBySlot = new HashMap<>();
    private boolean closingIntentionally;
    private boolean purchasing;

    public BuyerBoostersMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            ItemNameResolver itemNameResolver,
            BoosterService boosterService,
            BuyerMenuNavigation navigation,
            BuyerMenuSession parentSession
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.itemNameResolver = itemNameResolver;
        this.boosterService = boosterService;
        this.navigation = navigation;
        this.parentSession = parentSession == null ? BuyerMenuSession.empty() : parentSession;
        this.boostersGui = config.boostersGui();
        this.actions = GuiLayoutHelper.actionBySlot(boostersGui.elements);
        indexOffers();
        Component title = messageService.guiText(player, boostersGui.titleKey);
        this.inventory = Bukkit.createInventory(this, boostersGui.size, title);
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(int rawSlot) {
        if (purchasing) {
            return;
        }
        String action = actions.getOrDefault(rawSlot, "NONE");
        if ("BOOSTER_BUY".equals(action)) {
            purchase(offerBySlot.getOrDefault(rawSlot, ""));
            return;
        }
        if ("BOOSTER_BACK".equals(action)) {
            goBack();
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

    private void purchase(String offerId) {
        if (offerId == null || offerId.isBlank()) {
            return;
        }
        purchasing = true;
        boosterService.purchase(player, offerId, () -> Bukkit.getScheduler().runTask(plugin, () -> {
            purchasing = false;
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerBoostersMenu) {
                render();
            }
        }));
    }

    private void render() {
        inventory.clear();
        fillFrame();
        fillOffers();
        fillStaticButtons();
    }

    private void fillFrame() {
        GuiGeneralSettings.GuiElementSettings border = boostersGui.elements.get("border");
        GuiGeneralSettings.GuiElementSettings separator = boostersGui.elements.get("separator");
        if (border == null) {
            return;
        }
        for (int slot : GuiLayoutHelper.frameSlots(boostersGui.size)) {
            if (actions.containsKey(slot)) {
                continue;
            }
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        if (separator != null) {
            for (int slot : List.of(10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34)) {
                if (!actions.containsKey(slot)) {
                    inventory.setItem(slot, itemFactory.filler(player, separator));
                }
            }
        }
    }

    private void fillOffers() {
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : boostersGui.elements.entrySet()) {
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot < 0 || !"BOOSTER_BUY".equals(element.action)) {
                continue;
            }
            SoulBuyerSettings.BoosterOfferSettings offer = config.boosters().offers.get(element.offerId);
            if (offer == null) {
                continue;
            }
            BoosterType type = BoosterType.parse(offer.type);
            long remaining = boosterService.remainingMillis(player, type);
            String[] pairs = new String[]{
                    "price", formatPrice(offer.price),
                    "duration", BoosterDurationFormatter.format(offer.durationSeconds),
                    "effect", formatEffect(offer),
                    "remaining", BoosterDurationFormatter.formatMillis(remaining),
                    "active_state", messageService.raw(player, remaining > 0L
                    ? "gui.boosters.active-yes"
                    : "gui.boosters.active-no")
            };
            GuiGeneralSettings.GuiElementSettings view = copyOfferElement(element, offer);
            if (remaining > 0L) {
                inventory.setItem(element.slot, itemFactory.buildSelected(player, view, pairs));
            } else {
                inventory.setItem(element.slot, itemFactory.build(player, view, pairs));
            }
        }
    }

    private void fillStaticButtons() {
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : boostersGui.elements.entrySet()) {
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (element.slot < 0 || "BOOSTER_BUY".equals(element.action)) {
                continue;
            }
            if ("border".equals(entry.getKey()) || "separator".equals(entry.getKey())) {
                continue;
            }
            String[] pairs = new String[]{
                    "currency", messageService.raw(player, currencyLabelKey())
            };
            inventory.setItem(element.slot, itemFactory.build(player, element, pairs));
        }
    }

    private GuiGeneralSettings.GuiElementSettings copyOfferElement(
            GuiGeneralSettings.GuiElementSettings element,
            SoulBuyerSettings.BoosterOfferSettings offer
    ) {
        GuiGeneralSettings.GuiElementSettings view = new GuiGeneralSettings.GuiElementSettings();
        view.slot = element.slot;
        view.material = offer.material == null || offer.material.isBlank() ? element.material : offer.material;
        view.nameKey = offer.nameKey == null || offer.nameKey.isBlank() ? element.nameKey : offer.nameKey;
        view.loreKeys = offer.loreKeys == null || offer.loreKeys.isEmpty() ? element.loreKeys : offer.loreKeys;
        view.action = element.action;
        view.offerId = element.offerId;
        return view;
    }

    private String formatPrice(double price) {
        if (boosterService.currency() == BoosterCurrency.PROGRESSION_POINTS) {
            return itemNameResolver.formatMoney(price);
        }
        return itemNameResolver.formatMoney(price);
    }

    private String formatEffect(SoulBuyerSettings.BoosterOfferSettings offer) {
        BoosterType type = BoosterType.parse(offer.type);
        return switch (type) {
            case MULTIPLIER -> "+" + itemNameResolver.formatMoney(offer.effect);
            case MONEY, LIMIT -> "×" + itemNameResolver.formatMoney(offer.effect);
        };
    }

    private String currencyLabelKey() {
        return switch (boosterService.currency()) {
            case VAULT -> "gui.boosters.currency-vault";
            case PLAYER_POINTS -> "gui.boosters.currency-playerpoints";
            default -> "gui.boosters.currency-points";
        };
    }

    private void indexOffers() {
        for (GuiGeneralSettings.GuiElementSettings element : boostersGui.elements.values()) {
            if (element.slot >= 0 && "BOOSTER_BUY".equals(element.action)) {
                offerBySlot.put(element.slot, element.offerId);
            }
        }
    }
}
