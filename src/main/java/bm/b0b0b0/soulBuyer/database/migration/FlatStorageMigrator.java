package bm.b0b0b0.soulBuyer.database.migration;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class FlatStorageMigrator {

    private FlatStorageMigrator() {
    }

    public static StorageSnapshot export(JavaPlugin plugin, PluginConfig config) {
        Set<UUID> playerIds = collectPlayerIds(plugin, config);
        List<PlayerProgress> players = new ArrayList<>();
        Map<UUID, String> autosell = new HashMap<>();
        Map<UUID, String> boosters = new HashMap<>();
        Map<UUID, String> sellLimits = new HashMap<>();
        for (UUID playerId : playerIds) {
            PlayerProgress progress = readProgress(plugin, config, playerId);
            if (progress.points() > 0.0D || !progress.categoryXp().isEmpty()) {
                players.add(progress);
            }
            readOptionalPayload(plugin, config.storage().autosellFolder, playerId).ifPresent(value ->
                    autosell.put(playerId, value)
            );
            readOptionalPayload(plugin, config.storage().boostersFolder, playerId).ifPresent(value ->
                    boosters.put(playerId, value)
            );
            readOptionalPayload(plugin, config.storage().sellLimitsFolder, playerId).ifPresent(value ->
                    sellLimits.put(playerId, value)
            );
        }
        return new StorageSnapshot(players, exportMarket(plugin, config), autosell, boosters, sellLimits);
    }

    public static void importSnapshot(JavaPlugin plugin, PluginConfig config, StorageSnapshot snapshot) {
        for (PlayerProgress progress : snapshot.players()) {
            writeProgress(plugin, config, progress);
        }
        writeMarket(plugin, config, snapshot.market());
        importPayloads(plugin, config.storage().autosellFolder, snapshot.autosellPayloads());
        importPayloads(plugin, config.storage().boostersFolder, snapshot.boosterPayloads());
        importPayloads(plugin, config.storage().sellLimitsFolder, snapshot.sellLimitPayloads());
    }

    private static Set<UUID> collectPlayerIds(JavaPlugin plugin, PluginConfig config) {
        Set<UUID> playerIds = new HashSet<>();
        collectFromFolder(plugin, config.storage().playersFolder, playerIds);
        collectFromFolder(plugin, config.storage().autosellFolder, playerIds);
        collectFromFolder(plugin, config.storage().boostersFolder, playerIds);
        collectFromFolder(plugin, config.storage().sellLimitsFolder, playerIds);
        return playerIds;
    }

    private static void collectFromFolder(JavaPlugin plugin, String relativeFolder, Set<UUID> playerIds) {
        Path folder = plugin.getDataFolder().toPath().resolve(relativeFolder);
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".yml")).forEach(path -> {
                String name = path.getFileName().toString();
                try {
                    playerIds.add(UUID.fromString(name.substring(0, name.length() - 4)));
                } catch (IllegalArgumentException ignored) {
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan folder " + relativeFolder, exception);
        }
    }

    private static PlayerProgress readProgress(JavaPlugin plugin, PluginConfig config, UUID playerId) {
        Path path = plugin.getDataFolder().toPath()
                .resolve(config.storage().playersFolder)
                .resolve(playerId + ".yml");
        if (!Files.exists(path)) {
            return new PlayerProgress(playerId, 0.0D, Map.of());
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

    private static void writeProgress(JavaPlugin plugin, PluginConfig config, PlayerProgress progress) {
        try {
            Path path = plugin.getDataFolder().toPath()
                    .resolve(config.storage().playersFolder)
                    .resolve(progress.playerId() + ".yml");
            Files.createDirectories(path.getParent());
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("points", progress.points());
            yaml.set("updated-at", System.currentTimeMillis());
            for (Map.Entry<String, Double> entry : progress.categoryXp().entrySet()) {
                yaml.set("category-xp." + entry.getKey(), entry.getValue());
            }
            yaml.save(path.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write player " + progress.playerId(), exception);
        }
    }

    private static List<MarketItemSnapshot> exportMarket(JavaPlugin plugin, PluginConfig config) {
        Path marketPath = plugin.getDataFolder().toPath().resolve(config.storage().marketFile);
        List<MarketItemSnapshot> market = new ArrayList<>();
        if (!Files.exists(marketPath)) {
            return market;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(marketPath.toFile());
        if (!yaml.isConfigurationSection("items")) {
            return market;
        }
        for (String itemId : yaml.getConfigurationSection("items").getKeys(false)) {
            String base = "items." + itemId;
            market.add(new MarketItemSnapshot(
                    itemId,
                    yaml.getDouble(base + ".coefficient", 1.0D),
                    yaml.getLong(base + ".sold-total", 0L)
            ));
        }
        return market;
    }

    private static void writeMarket(JavaPlugin plugin, PluginConfig config, List<MarketItemSnapshot> market) {
        if (market.isEmpty()) {
            return;
        }
        try {
            Path marketPath = plugin.getDataFolder().toPath().resolve(config.storage().marketFile);
            if (marketPath.getParent() != null) {
                Files.createDirectories(marketPath.getParent());
            }
            YamlConfiguration yaml = Files.exists(marketPath)
                    ? YamlConfiguration.loadConfiguration(marketPath.toFile())
                    : new YamlConfiguration();
            for (MarketItemSnapshot item : market) {
                String base = "items." + item.itemId();
                yaml.set(base + ".coefficient", item.coefficient());
                yaml.set(base + ".sold-total", item.soldTotal());
            }
            yaml.set("updated-at", System.currentTimeMillis());
            yaml.save(marketPath.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write market.yml", exception);
        }
    }

    private static java.util.Optional<String> readOptionalPayload(
            JavaPlugin plugin,
            String relativeFolder,
            UUID playerId
    ) {
        Path path = plugin.getDataFolder().toPath().resolve(relativeFolder).resolve(playerId + ".yml");
        if (!Files.exists(path)) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }

    private static void importPayloads(JavaPlugin plugin, String relativeFolder, Map<UUID, String> payloads) {
        for (Map.Entry<UUID, String> entry : payloads.entrySet()) {
            try {
                Path path = plugin.getDataFolder().toPath().resolve(relativeFolder).resolve(entry.getKey() + ".yml");
                Files.createDirectories(path.getParent());
                Files.writeString(path, entry.getValue());
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to write payload " + entry.getKey(), exception);
            }
        }
    }
}
