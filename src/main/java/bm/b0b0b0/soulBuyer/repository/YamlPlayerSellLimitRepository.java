package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.limit.SellLimitUsageCodec;
import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import bm.b0b0b0.soulBuyer.model.SellLine;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlPlayerSellLimitRepository implements PlayerSellLimitRepository {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Executor executor;

    public YamlPlayerSellLimitRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<PlayerSellLimitUsage> find(UUID playerId, String periodKey) {
        return CompletableFuture.supplyAsync(() -> read(playerId, periodKey), executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerSellLimitUsage usage) {
        return CompletableFuture.runAsync(() -> write(usage), executor);
    }

    @Override
    public CompletableFuture<Void> recordSale(UUID playerId, String periodKey, List<SellLine> lines) {
        return find(playerId, periodKey).thenCompose(existing -> {
            Map<String, Integer> sold = new HashMap<>(existing.soldByItemId());
            for (SellLine line : lines) {
                sold.merge(line.itemId(), line.amount(), Integer::sum);
            }
            return save(new PlayerSellLimitUsage(playerId, periodKey, sold));
        });
    }

    private PlayerSellLimitUsage read(UUID playerId, String periodKey) {
        Path path = playerPath(playerId);
        if (!Files.exists(path)) {
            return PlayerSellLimitUsage.empty(playerId, periodKey);
        }
        try {
            return SellLimitUsageCodec.decode(playerId, Files.readString(path), periodKey);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read sell limit file " + playerId, exception);
        }
    }

    private void write(PlayerSellLimitUsage usage) {
        try {
            Path path = playerPath(usage.playerId());
            Files.createDirectories(path.getParent());
            Files.writeString(path, SellLimitUsageCodec.encode(usage));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write sell limit file " + usage.playerId(), exception);
        }
    }

    private Path playerPath(UUID playerId) {
        return plugin.getDataFolder().toPath()
                .resolve(config.storage().sellLimitsFolder)
                .resolve(playerId + ".yml");
    }
}
