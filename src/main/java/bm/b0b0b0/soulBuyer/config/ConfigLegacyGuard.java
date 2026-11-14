package bm.b0b0b0.soulBuyer.config;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.bukkit.plugin.java.JavaPlugin;

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

        stripLegacyGuiKeys(dataFolder.resolve("gui"), debug);

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
            if (trimmed.equals("items:") || trimmed.startsWith("items:")) {
                return true;
            }
        }
        return false;
    }

    private static void stripLegacyGuiKeys(Path guiFolder, SoulBuyerDebugLog debug) {
        if (!Files.isDirectory(guiFolder)) {
            return;
        }
        try (var files = Files.list(guiFolder)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                stripLegacyGuiKeysInFile(file, debug);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan gui configs for legacy keys", exception);
        }
    }

    private static void stripLegacyGuiKeysInFile(Path file, SoulBuyerDebugLog debug) throws IOException {
        String content = Files.readString(file);
        if (!content.contains("sort-filter")) {
            return;
        }
        String lineSeparator = content.contains("\r\n") ? "\r\n" : "\n";
        String cleaned = content.lines()
                .filter(line -> !line.trim().startsWith("sort-filter:"))
                .collect(Collectors.joining(lineSeparator));
        if (cleaned.equals(content)) {
            return;
        }
        if (!cleaned.endsWith(lineSeparator)) {
            cleaned += lineSeparator;
        }
        Files.writeString(file, cleaned);
        debug.boot("legacy guard: removed sort-filter from " + file.getFileName());
    }
}
