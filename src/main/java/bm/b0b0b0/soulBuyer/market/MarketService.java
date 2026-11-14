package bm.b0b0b0.soulBuyer.market;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.repository.MarketRepository;
import bm.b0b0b0.soulBuyer.sync.RedisBootstrap;
import bm.b0b0b0.soulBuyer.util.PluginSchedulers;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarketService {

    private final PluginConfig config;
    private final MarketRepository marketRepository;
    private final RedisBootstrap redisBootstrap;
    private final Map<String, Double> coefficients = new ConcurrentHashMap<>();
    private volatile boolean marketReady;
    private ScheduledTask decayTask;

    public MarketService(
            JavaPlugin plugin,
            PluginConfig config,
            MarketRepository marketRepository,
            RedisBootstrap redisBootstrap
    ) {
        this.config = config;
        this.marketRepository = marketRepository;
        this.redisBootstrap = redisBootstrap;
        startTasks(plugin);
    }

    public boolean isMarketReady() {
        return marketReady;
    }

    public void loadFromDatabase(Map<String, Double> loaded) {
        if (loaded != null) {
            for (Map.Entry<String, Double> entry : loaded.entrySet()) {
                coefficients.merge(entry.getKey(), clamp(entry.getValue()), Math::min);
            }
        }
        marketReady = true;
    }

    public double coefficient(String itemId) {
        return coefficients.getOrDefault(itemId, 1.0D);
    }

    public void applyRemoteUpdate(String payload) {
        int separator = payload.indexOf(':');
        if (separator <= 0) {
            return;
        }
        String itemId = payload.substring(0, separator);
        double value = Double.parseDouble(payload.substring(separator + 1));
        coefficients.merge(itemId, clamp(value), Math::min);
    }

    public void recordSale(String itemId, int amount) {
        if (amount <= 0) {
            return;
        }
        double current = coefficient(itemId);
        double next = clamp(current - config.market().dropPerUnit * amount);
        coefficients.put(itemId, next);
        marketRepository.saveCoefficient(itemId, next, amount);
        if (redisBootstrap.pool() != null) {
            redisBootstrap.publishMarketUpdate(itemId + ":" + next);
        }
    }

    public void shutdown() {
        if (decayTask != null) {
            decayTask.cancel();
        }
    }

    public void resetRotation(Set<String> activeItemIds) {
        coefficients.keySet().removeIf(itemId -> !activeItemIds.contains(itemId));
        for (String itemId : activeItemIds) {
            coefficients.put(itemId, 1.0D);
            marketRepository.saveCoefficient(itemId, 1.0D, 0L);
        }
    }

    private void startTasks(JavaPlugin plugin) {
        int decaySeconds = Math.max(30, config.market().decayIntervalSeconds);
        long periodTicks = decaySeconds * 20L;
        decayTask = PluginSchedulers.runAsyncTimer(plugin, this::runDecay, periodTicks, periodTicks);
    }

    private void runDecay() {
        double step = config.market().decayPerInterval;
        for (Map.Entry<String, Double> entry : coefficients.entrySet()) {
            double value = entry.getValue();
            if (value >= 1.0D) {
                continue;
            }
            double next = Math.min(1.0D, value + step);
            entry.setValue(next);
            marketRepository.saveCoefficient(entry.getKey(), next, 0L);
        }
    }

    private double clamp(double value) {
        if (!Double.isFinite(value)) {
            return 1.0D;
        }
        return Math.max(config.minMarketCoefficient(), Math.min(1.0D, value));
    }
}
