package bm.b0b0b0.soulBuyer.config;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ConfigLegacyGuard {

    private static final long LEGACY_SIZE_BYTES = 120_000L;

    private ConfigLegacyGuard() {
    }

    public static void prepare(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        Path dataFolder = plugin.getDataFolder().toPath();
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create plugin data folder", exception);
        }

        Path configPath = dataFolder.resolve("config.yml");
        if (!Files.exists(configPath)) {
            debug.boot("legacy guard: no config.yml yet, fresh Elytrium write");
            return;
        }

        try {
            long size = Files.size(configPath);
            String sample = Files.readString(configPath);
            boolean legacyItems = containsLegacyItemsSection(sample);
            if (!legacyItems && size <= LEGACY_SIZE_BYTES) {
                debug.boot("legacy guard: config.yml OK (size=" + size + " bytes)");
                return;
            }

            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path backup = dataFolder.resolve("config.yml.legacy-" + stamp);
            Files.move(configPath, backup, StandardCopyOption.REPLACE_EXISTING);
            debug.boot("legacy guard: moved bloated/legacy config.yml -> " + backup.getFileName()
                    + " (size=" + size + ", legacyItems=" + legacyItems + ")");
            debug.boot("legacy guard: Elytrium will regenerate config.yml + items.yml on this start");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect legacy config.yml", exception);
        }
    }

    private static boolean containsLegacyItemsSection(String yaml) {
        for (String line : yaml.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("items:")) {
                return true;
            }
        }
        return false;
    }
}
