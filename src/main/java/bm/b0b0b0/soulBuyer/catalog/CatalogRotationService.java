package bm.b0b0b0.soulBuyer.catalog;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.gui.BuyerMenu;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.market.MarketService;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.CatalogRotationState;
import bm.b0b0b0.soulBuyer.repository.CatalogRotationRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CatalogRotationService {

    private final JavaPlugin plugin;
    private final ItemRegistry itemRegistry;
    private final MarketService marketService;
    private final MessageService messageService;
    private final CatalogRotationRepository repository;
    private final SoulBuyerDebugLog debug;

    private volatile PluginConfig config;
    private BukkitTask scheduledTask;
    private volatile long nextRotationAtMs;

    public CatalogRotationService(
            JavaPlugin plugin,
            PluginConfig config,
            ItemRegistry itemRegistry,
            MarketService marketService,
            MessageService messageService,
            CatalogRotationRepository repository,
            SoulBuyerDebugLog debug
    ) {
        this.plugin = plugin;
        this.config = config;
        this.itemRegistry = itemRegistry;
        this.marketService = marketService;
        this.messageService = messageService;
        this.repository = repository;
        this.debug = debug;
    }

    public void start() {
        repository.load().thenAccept(state ->
                Bukkit.getScheduler().runTask(plugin, () -> bootstrap(state))
        );
    }

    public void reload(PluginConfig reloaded) {
        this.config = reloaded;
        cancelSchedule();
        if (!config.catalogRotation().enabled) {
            itemRegistry.activateAll();
            return;
        }
        repository.load().thenAccept(state -> Bukkit.getScheduler().runTask(plugin, () -> {
            Set<String> activeIds = resolveActiveIds(state);
            applyActiveIds(activeIds, false, false);
            long now = System.currentTimeMillis();
            if (state.nextRotationAtMs() > now) {
                nextRotationAtMs = state.nextRotationAtMs();
                scheduleAt(nextRotationAtMs);
                return;
            }
            rotate(false);
        }));
    }

    public void shutdown() {
        cancelSchedule();
    }

    public boolean rotationEnabled() {
        return config.catalogRotation().enabled;
    }

    public long nextRotationAtMs() {
        return nextRotationAtMs;
    }

    public long secondsUntilRotation() {
        if (!rotationEnabled()) {
            return -1L;
        }
        long remainingMs = nextRotationAtMs - System.currentTimeMillis();
        if (remainingMs <= 0L) {
            return 0L;
        }
        return (remainingMs + 999L) / 1000L;
    }

    private void bootstrap(CatalogRotationState state) {
        if (!config.catalogRotation().enabled) {
            itemRegistry.activateAll();
            debug.boot("catalog rotation disabled, active=" + itemRegistry.activeSize());
            return;
        }

        Set<String> activeIds = resolveActiveIds(state);
        applyActiveIds(activeIds, false, false);

        long intervalMs = intervalMs();
        long now = System.currentTimeMillis();
        if (state.nextRotationAtMs() > now) {
            nextRotationAtMs = state.nextRotationAtMs();
            scheduleAt(nextRotationAtMs);
            debug.boot("catalog rotation restored, active=" + itemRegistry.activeSize()
                    + " next in " + ((nextRotationAtMs - now) / 1000L) + "s");
            return;
        }

        if (state.activeItemIds().isEmpty()) {
            nextRotationAtMs = now + intervalMs;
            persistState(activeIds, nextRotationAtMs);
            scheduleAt(nextRotationAtMs);
            debug.boot("catalog rotation initial, active=" + itemRegistry.activeSize());
            return;
        }

        debug.boot("catalog rotation overdue, rotating now");
        rotate(false);
    }

    private Set<String> resolveActiveIds(CatalogRotationState state) {
        LinkedHashSet<String> saved = new LinkedHashSet<>();
        for (String itemId : state.activeItemIds()) {
            if (itemRegistry.pool().stream().anyMatch(definition -> definition.id().equals(itemId))) {
                saved.add(itemId);
            }
        }
        if (!saved.isEmpty()) {
            return saved;
        }
        return pickNewActiveIds();
    }

    private void rotate(boolean announce) {
        if (!config.catalogRotation().enabled) {
            return;
        }
        Set<String> activeIds = pickNewActiveIds();
        applyActiveIds(activeIds, announce, true);
        nextRotationAtMs = System.currentTimeMillis() + intervalMs();
        persistState(activeIds, nextRotationAtMs);
        scheduleAt(nextRotationAtMs);
    }

    private void applyActiveIds(Set<String> activeIds, boolean announce, boolean resetMarket) {
        itemRegistry.applyActiveIds(activeIds);
        if (resetMarket && config.catalogRotation().resetMarketOnRotation) {
            marketService.resetRotation(activeIds);
        }
        refreshOpenMenus();
        if (announce && config.catalogRotation().notify.enabled) {
            broadcastRotation(activeIds.size());
        }
        debug.log("catalog rotation applied, active=" + activeIds.size() + " announce=" + announce);
    }

    private Set<String> pickNewActiveIds() {
        return CatalogRotationSelector.select(
                itemRegistry.pool(),
                config.catalogRotation().activeItemCount,
                config.catalogRotation().minItemsPerCategory,
                config.categories()
        );
    }

    private void persistState(Set<String> activeIds, long nextRotationAt) {
        repository.save(new CatalogRotationState(new ArrayList<>(activeIds), nextRotationAt));
    }

    private void scheduleAt(long targetAtMs) {
        cancelSchedule();
        long delayMs = Math.max(1000L, targetAtMs - System.currentTimeMillis());
        long delayTicks = Math.max(1L, delayMs / 50L);
        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, () -> rotate(true), delayTicks);
    }

    private void cancelSchedule() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    private long intervalMs() {
        return Math.max(60L, config.catalogRotation().intervalSeconds) * 1000L;
    }

    private void broadcastRotation(int count) {
        String permission = config.permissionUse();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(permission)) {
                continue;
            }
            messageService.send(player, "rotation.announce", "count", String.valueOf(count));
        }
    }

    private void refreshOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerMenu menu) {
                menu.refresh();
            }
        }
    }
}
