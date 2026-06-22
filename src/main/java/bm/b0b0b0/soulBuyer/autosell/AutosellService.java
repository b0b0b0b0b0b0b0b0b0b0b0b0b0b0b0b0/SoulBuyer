package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.repository.PlayerAutosellRepository;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import bm.b0b0b0.soulBuyer.service.SaleDelivery;
import bm.b0b0b0.soulBuyer.service.SellService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AutosellService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final ItemRegistry itemRegistry;
    private final PlayerAutosellRepository repository;
    private final SellService sellService;
    private final Map<UUID, PlayerAutosellSettings> cache = new ConcurrentHashMap<>();

    public AutosellService(
            JavaPlugin plugin,
            PluginConfig config,
            ItemRegistry itemRegistry,
            PlayerAutosellRepository repository,
            SellService sellService
    ) {
        this.plugin = plugin;
        this.config = config;
        this.itemRegistry = itemRegistry;
        this.repository = repository;
        this.sellService = sellService;
    }

    public boolean featureEnabled() {
        return config.autosellFeatureEnabled();
    }

    public boolean canAccess(Player player) {
        return featureEnabled() && player.hasPermission(config.permissionAutosell());
    }

    public PlayerAutosellSettings settings(Player player) {
        return cache.computeIfAbsent(
                player.getUniqueId(),
                id -> PlayerAutosellSettings.defaults(id, config.autosell())
        );
    }

    public void preload(Player player) {
        repository.find(player.getUniqueId()).thenAccept(settings ->
                Bukkit.getScheduler().runTask(plugin, () -> cache.put(player.getUniqueId(), settings))
        );
    }

    public void unload(Player player) {
        cache.remove(player.getUniqueId());
    }

    public void toggleEnabled(Player player, Runnable onSaved) {
        if (!canAccess(player)) {
            return;
        }
        PlayerAutosellSettings current = settings(player);
        save(player, current.withEnabled(!current.enabled()), onSaved);
    }

    public void setEnabled(Player player, boolean enabled, Runnable onSaved) {
        if (!canAccess(player)) {
            return;
        }
        save(player, settings(player).withEnabled(enabled), onSaved);
    }

    public void cycleTrigger(Player player, Runnable onSaved) {
        if (!canAccess(player)) {
            return;
        }
        PlayerAutosellSettings current = settings(player);
        save(player, current.withTrigger(AutosellTrigger.next(current.trigger())), onSaved);
    }

    public void cycleNotify(Player player, Runnable onSaved) {
        PlayerAutosellSettings current = settings(player);
        save(player, current.withNotifyMode(AutosellNotify.next(current.notifyMode())), onSaved);
    }

    public boolean payoutChoiceAvailable() {
        return config.donateBuyerActive();
    }

    public BuyerPayoutMode resolvePayoutMode(PlayerAutosellSettings autosellSettings) {
        if (!payoutChoiceAvailable()) {
            return config.defaultOpenPayoutMode();
        }
        return AutosellPayout.toMode(autosellSettings.payoutTarget());
    }

    public void cyclePayout(Player player, Runnable onSaved) {
        if (!canAccess(player) || !payoutChoiceAvailable()) {
            return;
        }
        PlayerAutosellSettings current = settings(player);
        save(player, current.withPayoutTarget(AutosellPayout.next(current.payoutTarget())), onSaved);
    }

    public void cycleMinUnitPrice(Player player, Runnable onSaved) {
        List<Double> steps = config.autosell().minUnitPriceSteps;
        if (steps == null || steps.isEmpty()) {
            return;
        }
        PlayerAutosellSettings current = settings(player);
        double currentValue = current.minUnitPrice();
        int index = 0;
        for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
            if (Math.abs(steps.get(stepIndex) - currentValue) < 0.001D) {
                index = (stepIndex + 1) % steps.size();
                break;
            }
        }
        save(player, current.withMinUnitPrice(steps.get(index)), onSaved);
    }

    public void toggleCategory(Player player, String categoryId, Runnable onSaved) {
        if (categoryId == null || categoryId.isBlank()) {
            return;
        }
        PlayerAutosellSettings current = settings(player);
        Set<String> categories = new LinkedHashSet<>(current.categories());
        if (categories.contains(categoryId)) {
            categories.remove(categoryId);
        } else {
            categories.add(categoryId);
        }
        if (categories.isEmpty()) {
            categories.add(categoryId);
        }
        save(player, current.withCategories(categories), onSaved);
    }

    public void trySellOnBuyerOpen(Player player, Runnable onOpen) {
        trySellBulk(player, AutosellTrigger.BUYER, onOpen);
    }

    public void onItemAcquired(Player player, ItemStack pickedStack) {
        sellAcquiredStack(player, pickedStack, AutosellTrigger.PICKUP);
    }

    public void trySellChestOnOpen(Player player, org.bukkit.inventory.Inventory chestInventory) {
        if (!canAccess(player) || chestInventory == null) {
            return;
        }
        if (!AutosellInventoryGuard.isStorageContainer(chestInventory)) {
            return;
        }
        PlayerAutosellSettings autosellSettings = settings(player);
        if (!autosellSettings.enabled() || !AutosellTrigger.isChest(autosellSettings.trigger())) {
            return;
        }
        if (sellService.isProcessing(player.getUniqueId())) {
            return;
        }
        sellService.sellFromContainer(
                player,
                chestInventory,
                definition -> passesFilter(player, autosellSettings, definition),
                deliveryFor(autosellSettings),
                null,
                resolvePayoutMode(autosellSettings)
        );
    }

    private void sellAcquiredStack(Player player, ItemStack stack, String requiredTrigger) {
        if (!canAccess(player) || stack == null || stack.getType().isAir()) {
            return;
        }
        if (sellService.isProcessing(player.getUniqueId())) {
            return;
        }
        PlayerAutosellSettings autosellSettings = settings(player);
        if (!autosellSettings.enabled() || !requiredTrigger.equals(AutosellTrigger.normalize(autosellSettings.trigger()))) {
            return;
        }
        Optional<SellableItemDefinition> definition = itemRegistry.findInPool(stack);
        if (definition.isEmpty()) {
            return;
        }
        SellableItemDefinition resolved = definition.get();
        if (!passesFilter(player, autosellSettings, resolved)) {
            return;
        }
        int amount = Math.min(stack.getAmount(), InventorySellHelper.countMatching(player, itemRegistry, resolved.id()));
        if (amount <= 0) {
            return;
        }
        sellService.sellItemAmountFromInventory(
                player,
                resolved.id(),
                amount,
                deliveryFor(autosellSettings),
                null,
                resolvePayoutMode(autosellSettings)
        );
    }

    private void trySellBulk(Player player, String requiredTrigger, Runnable onComplete) {
        if (requiredTrigger == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (!canAccess(player)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        PlayerAutosellSettings autosellSettings = settings(player);
        if (!autosellSettings.enabled() || !requiredTrigger.equals(AutosellTrigger.normalize(autosellSettings.trigger()))) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (sellService.isProcessing(player.getUniqueId())) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        sellService.sellFromInventory(
                player,
                definition -> passesFilter(player, autosellSettings, definition),
                deliveryFor(autosellSettings),
                onComplete,
                false,
                resolvePayoutMode(autosellSettings)
        );
    }

    public boolean matches(PlayerAutosellSettings autosellSettings, SellableItemDefinition definition) {
        return autosellSettings.categories().contains(definition.categoryId());
    }

    public SaleDelivery deliveryFor(PlayerAutosellSettings autosellSettings) {
        return switch (AutosellNotify.normalize(autosellSettings.notifyMode())) {
            case AutosellNotify.CHAT -> SaleDelivery.AUTOSell_CHAT;
            case AutosellNotify.OFF -> SaleDelivery.SILENT;
            default -> SaleDelivery.ACTION_BAR;
        };
    }

    private boolean passesFilter(
            Player player,
            PlayerAutosellSettings autosellSettings,
            SellableItemDefinition definition
    ) {
        if (!matches(autosellSettings, definition)) {
            return false;
        }
        double unitPrice = sellService.unitQuote(player, definition).unitPrice();
        return unitPrice + 0.001D >= autosellSettings.minUnitPrice();
    }

    private void save(Player player, PlayerAutosellSettings updated, Runnable onSaved) {
        cache.put(player.getUniqueId(), updated);
        repository.save(updated).thenRun(() -> {
            if (onSaved != null) {
                Bukkit.getScheduler().runTask(plugin, onSaved);
            }
        });
    }
}
