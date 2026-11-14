package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.booster.BoosterStateCodec;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.GlobalBoosterState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlGlobalBoosterRepository implements GlobalBoosterRepository {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Executor executor;

    public YamlGlobalBoosterRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<GlobalBoosterState> load() {
        return CompletableFuture.supplyAsync(this::read, executor);
    }

    @Override
    public CompletableFuture<Void> save(GlobalBoosterState state) {
        return CompletableFuture.runAsync(() -> write(state), executor);
    }

    private GlobalBoosterState read() {
        Path path = path();
        if (!Files.exists(path)) {
            return GlobalBoosterState.empty();
        }
        try {
            return BoosterStateCodec.decodeGlobal(Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read global boosters", exception);
        }
    }

    private void write(GlobalBoosterState state) {
        try {
            Path path = path();
            Files.createDirectories(path.getParent());
            Files.writeString(path, BoosterStateCodec.encode(state));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write global boosters", exception);
        }
    }

    private Path path() {
        return plugin.getDataFolder().toPath().resolve(config.storage().globalBoostersFile);
    }
}
