package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.autosell.AutosellSettingsCodec;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlPlayerAutosellRepository implements PlayerAutosellRepository {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Executor executor;

    public YamlPlayerAutosellRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<PlayerAutosellSettings> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> read(playerId), executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerAutosellSettings settings) {
        return CompletableFuture.runAsync(() -> write(settings), executor);
    }

    private PlayerAutosellSettings read(UUID playerId) {
        Path path = playerPath(playerId);
        if (!Files.exists(path)) {
            return PlayerAutosellSettings.defaults(playerId, config.autosell());
        }
        try {
            String payload = Files.readString(path);
            return AutosellSettingsCodec.decode(playerId, payload, config.autosell());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read autosell file " + playerId, exception);
        }
    }

    private void write(PlayerAutosellSettings settings) {
        try {
            Path path = playerPath(settings.playerId());
            Files.createDirectories(path.getParent());
            Files.writeString(path, AutosellSettingsCodec.encode(settings));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write autosell file " + settings.playerId(), exception);
        }
    }

    private Path playerPath(UUID playerId) {
        return plugin.getDataFolder().toPath()
                .resolve(config.storage().autosellFolder)
                .resolve(playerId + ".yml");
    }
}
