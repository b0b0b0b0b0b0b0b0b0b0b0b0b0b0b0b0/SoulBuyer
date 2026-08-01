package bm.b0b0b0.soulBuyer.service;

import bm.b0b0b0.soulBuyer.catalog.CatalogRotationService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.integration.PlaceholderApiBridge;
import bm.b0b0b0.soulBuyer.item.ItemNameResolver;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.PlayerDailySaleStats;
import bm.b0b0b0.soulBuyer.repository.SaleLogRepository;
import bm.b0b0b0.soulBuyer.util.DurationFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class BuyerStatsService {

    private final PluginConfig config;
    private final MessageService messageService;
    private final ItemRegistry itemRegistry;
    private final ItemNameResolver itemNameResolver;
    private final CatalogRotationService catalogRotationService;
    private final SaleLogRepository saleLogRepository;
    private final PlaceholderApiBridge placeholderApiBridge;

    private final Map<UUID, PlayerDailySaleStats> dailyCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> dailyLoadedDay = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerDailySaleStats> allTimeCache = new ConcurrentHashMap<>();
    private final Set<UUID> allTimeLoaded = ConcurrentHashMap.newKeySet();
    private volatile ZoneId dayZone = ZoneId.systemDefault();

    public BuyerStatsService(
            PluginConfig config,
            MessageService messageService,
            ItemRegistry itemRegistry,
            ItemNameResolver itemNameResolver,
            CatalogRotationService catalogRotationService,
            SaleLogRepository saleLogRepository,
            PlaceholderApiBridge placeholderApiBridge
    ) {
        this.config = config;
        this.messageService = messageService;
        this.itemRegistry = itemRegistry;
        this.itemNameResolver = itemNameResolver;
        this.catalogRotationService = catalogRotationService;
        this.saleLogRepository = saleLogRepository;
        this.placeholderApiBridge = placeholderApiBridge;
    }

    public void recordSale(UUID playerId, double money, double points, int stacks) {
        refreshDayIfNeeded(playerId);
        PlayerDailySaleStats delta = new PlayerDailySaleStats(money, points, stacks);
        dailyCache.merge(playerId, delta, (left, right) -> left.plus(right.money(), right.points(), right.stacks()));
        allTimeCache.merge(playerId, delta, (left, right) -> left.plus(right.money(), right.points(), right.stacks()));
    }

    public void preloadDaily(Player player) {
        UUID playerId = player.getUniqueId();
        preloadDailyStats(playerId);
        preloadAllTimeStats(playerId);
    }

    public String[] guiPairs(Player player) {
        preloadDaily(player);
        PlayerDailySaleStats today = dailyStats(player.getUniqueId());
        PlayerDailySaleStats allTime = allTimeStats(player.getUniqueId());
        Locale locale = localeOf(player);
        return new String[]{
                "rotation_left", rotationLeftText(player, locale),
                "rotation_seconds", String.valueOf(Math.max(0L, catalogRotationService.secondsUntilRotation())),
                "sold_today_money", itemNameResolver.formatMoney(today.money()),
                "sold_today_points", itemNameResolver.formatMoney(today.points()),
                "sold_today_stacks", String.valueOf(today.stacks()),
                "sold_alltime_money", itemNameResolver.formatMoney(allTime.money()),
                "sold_alltime_points", itemNameResolver.formatMoney(allTime.points()),
                "sold_alltime_stacks", String.valueOf(allTime.stacks()),
                "active_items", String.valueOf(itemRegistry.activeSize())
        };
    }

    public String resolvePlaceholder(Player player, String params) {
        String[] pairs = guiPairs(player);
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            if (pairs[index].equalsIgnoreCase(params)) {
                return pairs[index + 1];
            }
        }
        return null;
    }

    public PlaceholderApiBridge placeholderApiBridge() {
        return placeholderApiBridge;
    }

    private void preloadDailyStats(UUID playerId) {
        long dayStart = dayStartMs();
        Long loadedDay = dailyLoadedDay.get(playerId);
        if (loadedDay != null && loadedDay == dayStart) {
            return;
        }
        saleLogRepository.loadDailyStats(playerId, dayStart).thenAccept(stats -> {
            dailyCache.merge(
                    playerId,
                    stats,
                    (left, right) -> left.plus(right.money(), right.points(), right.stacks())
            );
            dailyLoadedDay.put(playerId, dayStart);
        });
    }

    private void preloadAllTimeStats(UUID playerId) {
        if (!allTimeLoaded.add(playerId)) {
            return;
        }
        saleLogRepository.loadAllTimeStats(playerId).thenAccept(stats -> allTimeCache.merge(
                playerId,
                stats,
                (left, right) -> left.plus(right.money(), right.points(), right.stacks())
        ));
    }

    private PlayerDailySaleStats dailyStats(UUID playerId) {
        refreshDayIfNeeded(playerId);
        return dailyCache.getOrDefault(playerId, PlayerDailySaleStats.empty());
    }

    private PlayerDailySaleStats allTimeStats(UUID playerId) {
        return allTimeCache.getOrDefault(playerId, PlayerDailySaleStats.empty());
    }

    private void refreshDayIfNeeded(UUID playerId) {
        long dayStart = dayStartMs();
        Long loadedDay = dailyLoadedDay.get(playerId);
        if (loadedDay != null && loadedDay == dayStart) {
            return;
        }
        dailyCache.remove(playerId);
        dailyLoadedDay.remove(playerId);
    }

    private String rotationLeftText(Player player, Locale locale) {
        if (!catalogRotationService.rotationEnabled()) {
            return messageService.raw(player, "gui.buyer.rotation-disabled");
        }
        long seconds = catalogRotationService.secondsUntilRotation();
        if (seconds <= 0L) {
            return messageService.raw(player, "gui.buyer.rotation-soon");
        }
        return DurationFormatter.formatSeconds(seconds, locale);
    }

    private Locale localeOf(Player player) {
        String code = messageService.locale(player);
        if (code == null || code.isBlank()) {
            return Locale.forLanguageTag(config.fallbackLocale());
        }
        return Locale.forLanguageTag(code);
    }

    private long dayStartMs() {
        LocalDate today = LocalDate.now(dayZone);
        return today.atStartOfDay(dayZone).toInstant().toEpochMilli();
    }
}
