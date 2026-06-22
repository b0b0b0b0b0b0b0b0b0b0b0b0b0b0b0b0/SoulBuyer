package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.booster.BoosterStateCodec;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlPlayerBoosterRepository implements PlayerBoosterRepository {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Executor executor;

    public YamlPlayerBoosterRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<PlayerBoosterState> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> read(playerId), executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerBoosterState state) {
        return CompletableFuture.runAsync(() -> write(state), executor);
    }

    private PlayerBoosterState read(UUID playerId) {
        Path path = playerPath(playerId);
        if (!Files.exists(path)) {
            return PlayerBoosterState.empty(playerId);
        }
        try {
            return BoosterStateCodec.decode(playerId, Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read booster file " + playerId, exception);
        }
    }

    private void write(PlayerBoosterState state) {
        try {
            Path path = playerPath(state.playerId());
            Files.createDirectories(path.getParent());
            Files.writeString(path, BoosterStateCodec.encode(state));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write booster file " + state.playerId(), exception);
        }
    }

    private Path playerPath(UUID playerId) {
        return plugin.getDataFolder().toPath()
                .resolve(config.storage().boostersFolder)
                .resolve(playerId + ".yml");
    }
}
