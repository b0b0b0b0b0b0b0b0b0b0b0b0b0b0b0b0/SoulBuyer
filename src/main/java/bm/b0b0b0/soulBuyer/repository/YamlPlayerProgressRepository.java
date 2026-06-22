package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlPlayerProgressRepository implements PlayerProgressRepository {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Executor executor;

    public YamlPlayerProgressRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<PlayerProgress> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> read(playerId), executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerProgress progress) {
        return CompletableFuture.runAsync(() -> write(progress), executor);
    }

    @Override
    public CompletableFuture<Void> addPointsAndCategoryXp(UUID playerId, double points, Map<String, Double> categoryXpDelta) {
        return find(playerId).thenCompose(existing -> {
            Map<String, Double> categoryXp = new HashMap<>(existing.categoryXp());
            categoryXpDelta.forEach((key, value) -> categoryXp.merge(key, value, Double::sum));
            return save(new PlayerProgress(playerId, existing.points() + points, categoryXp));
        });
    }

    @Override
    public CompletableFuture<Boolean> trySpendPoints(UUID playerId, double amount) {
        return find(playerId).thenCompose(existing -> {
            if (existing.points() + 1.0E-9D < amount) {
                return CompletableFuture.completedFuture(false);
            }
            return save(new PlayerProgress(playerId, existing.points() - amount, existing.categoryXp()))
                    .thenApply(ignored -> true);
        });
    }

    private PlayerProgress read(UUID playerId) {
        Path path = playerPath(playerId);
        if (!Files.exists(path)) {
            return new PlayerProgress(playerId, 0.0D, new HashMap<>());
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path.toFile());
        Map<String, Double> categoryXp = new HashMap<>();
        if (yaml.isConfigurationSection("category-xp")) {
            for (String key : yaml.getConfigurationSection("category-xp").getKeys(false)) {
                categoryXp.put(key, yaml.getDouble("category-xp." + key));
            }
        }
        return new PlayerProgress(playerId, yaml.getDouble("points"), categoryXp);
    }

    private void write(PlayerProgress progress) {
        try {
            Path path = playerPath(progress.playerId());
            Files.createDirectories(path.getParent());
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("points", progress.points());
            yaml.set("updated-at", System.currentTimeMillis());
            for (Map.Entry<String, Double> entry : progress.categoryXp().entrySet()) {
                yaml.set("category-xp." + entry.getKey(), entry.getValue());
            }
            yaml.save(path.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write player file " + progress.playerId(), exception);
        }
    }

    private Path playerPath(UUID playerId) {
        return plugin.getDataFolder().toPath()
                .resolve(config.storage().playersFolder)
                .resolve(playerId + ".yml");
    }
}
