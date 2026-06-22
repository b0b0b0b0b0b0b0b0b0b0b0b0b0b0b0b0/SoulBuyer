package bm.b0b0b0.soulBuyer.config;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SerializedConfigReloader {

    private SerializedConfigReloader() {
    }

    public static void reload(JavaPlugin plugin, YamlSerializable settings, Path relativePath, SoulBuyerDebugLog debug) {
        Path path = plugin.getDataFolder().toPath().resolve(relativePath);
        long startedAt = System.currentTimeMillis();
        debug.boot("elytrium reload start: " + relativePath + " -> " + path);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            settings.reload(path);
            debug.boot("elytrium reload OK: " + relativePath + " (" + (System.currentTimeMillis() - startedAt) + "ms)"
                    + " exists=" + Files.exists(path));
        } catch (Exception exception) {
            debug.error("elytrium reload FAIL: " + relativePath + " (" + (System.currentTimeMillis() - startedAt) + "ms)",
                    exception);
            throw new IllegalStateException(
                    "Failed to reload config at " + relativePath + ": " + exception.getMessage(),
                    exception
            );
        }
    }
}
