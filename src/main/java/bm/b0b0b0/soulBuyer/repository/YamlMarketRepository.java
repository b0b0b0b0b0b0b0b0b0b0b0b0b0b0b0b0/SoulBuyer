package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlMarketRepository implements MarketRepository {

    private final Path marketPath;
    private final Executor executor;

    public YamlMarketRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.marketPath = plugin.getDataFolder().toPath().resolve(config.storage().marketFile);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Map<String, Double>> loadAllCoefficients() {
        return CompletableFuture.supplyAsync(this::readCoefficients, executor);
    }

    @Override
    public CompletableFuture<Void> saveCoefficient(String itemId, double coefficient, long soldDelta) {
        return CompletableFuture.runAsync(() -> writeCoefficient(itemId, coefficient, soldDelta), executor);
    }

    private Map<String, Double> readCoefficients() {
        Map<String, Double> coefficients = new HashMap<>();
        if (!Files.exists(marketPath)) {
            return coefficients;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(marketPath.toFile());
        if (!yaml.isConfigurationSection("items")) {
            return coefficients;
        }
        for (String itemId : yaml.getConfigurationSection("items").getKeys(false)) {
            coefficients.put(itemId, yaml.getDouble("items." + itemId + ".coefficient", 1.0D));
        }
        return coefficients;
    }

    private synchronized void writeCoefficient(String itemId, double coefficient, long soldDelta) {
        try {
            if (marketPath.getParent() != null) {
                Files.createDirectories(marketPath.getParent());
            }
            YamlConfiguration yaml = Files.exists(marketPath)
                    ? YamlConfiguration.loadConfiguration(marketPath.toFile())
                    : new YamlConfiguration();
            String base = "items." + itemId;
            yaml.set(base + ".coefficient", coefficient);
            long soldTotal = yaml.getLong(base + ".sold-total", 0L) + soldDelta;
            yaml.set(base + ".sold-total", soldTotal);
            yaml.set("updated-at", System.currentTimeMillis());
            yaml.save(marketPath.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write market file", exception);
        }
    }
}
