package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.CatalogRotationState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlCatalogRotationRepository implements CatalogRotationRepository {

    private final Path rotationPath;
    private final Executor executor;

    public YamlCatalogRotationRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.rotationPath = plugin.getDataFolder().toPath().resolve(config.storage().rotationFile);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<CatalogRotationState> load() {
        return CompletableFuture.supplyAsync(this::readState, executor);
    }

    @Override
    public CompletableFuture<Void> save(CatalogRotationState state) {
        return CompletableFuture.runAsync(() -> writeState(state), executor);
    }

    private CatalogRotationState readState() {
        if (!Files.exists(rotationPath)) {
            return new CatalogRotationState(List.of(), 0L);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(rotationPath.toFile());
        List<String> activeItems = yaml.getStringList("active-items");
        long nextRotationAt = yaml.getLong("next-rotation-at", 0L);
        return new CatalogRotationState(new ArrayList<>(activeItems), nextRotationAt);
    }

    private synchronized void writeState(CatalogRotationState state) {
        try {
            if (rotationPath.getParent() != null) {
                Files.createDirectories(rotationPath.getParent());
            }
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("active-items", new ArrayList<>(state.activeItemIds()));
            yaml.set("next-rotation-at", state.nextRotationAtMs());
            yaml.set("updated-at", System.currentTimeMillis());
            yaml.save(rotationPath.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write rotation file", exception);
        }
    }
}
