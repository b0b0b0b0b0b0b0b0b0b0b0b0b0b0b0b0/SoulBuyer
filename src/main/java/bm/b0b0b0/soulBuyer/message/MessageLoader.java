package bm.b0b0b0.soulBuyer.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageLoader {

    private static final List<String> KNOWN_LOCALES = List.of("ru", "en");

    private final JavaPlugin plugin;
    private final String defaultLocale;
    private final String fallbackLocale;
    private volatile Map<String, Map<String, Object>> locales = Map.of();

    public MessageLoader(JavaPlugin plugin, String defaultLocale, String fallbackLocale) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale;
        this.fallbackLocale = fallbackLocale;
    }

    public void load() {
        Map<String, Map<String, Object>> next = new LinkedHashMap<>();
        for (String locale : KNOWN_LOCALES) {
            ensureLocaleFile(locale);
            syncBundledLocale(locale);
            next.put(locale, loadLocaleMap(locale));
        }
        locales = Map.copyOf(next);
    }

    public String defaultLocale() {
        return defaultLocale;
    }

    public boolean containsLocale(String locale) {
        return locales.containsKey(locale);
    }

    private void ensureLocaleFile(String locale) {
        Path path = langPath(locale);
        if (Files.exists(path)) {
            return;
        }
        if (copyBundledLocale(locale)) {
            return;
        }
        Map<String, Object> defaults = defaultsFor(locale);
        try {
            Files.createDirectories(path.getParent());
            YamlConfiguration yaml = new YamlConfiguration();
            defaults.forEach(yaml::set);
            yaml.save(path.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write lang/" + locale + ".yml", exception);
        }
    }

    private void syncBundledLocale(String locale) {
        YamlConfiguration bundled = loadBundledConfiguration(locale);
        if (bundled == null) {
            return;
        }
        Path path = langPath(locale);
        YamlConfiguration disk = YamlConfiguration.loadConfiguration(path.toFile());
        boolean changed = mergeMissingInto(disk, bundled);
        if (!changed) {
            return;
        }
        try {
            disk.save(path.toFile());
            plugin.getLogger().info("[SoulBuyer] Lang: added missing keys from JAR to lang/" + locale + ".yml");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to update lang/" + locale + ".yml", exception);
        }
    }

    private boolean mergeMissingInto(ConfigurationSection target, ConfigurationSection defaults) {
        boolean changed = false;
        for (String key : defaults.getKeys(false)) {
            if (defaults.isConfigurationSection(key)) {
                ConfigurationSection defaultSection = defaults.getConfigurationSection(key);
                if (defaultSection == null) {
                    continue;
                }
                ConfigurationSection targetSection = target.getConfigurationSection(key);
                if (targetSection == null) {
                    targetSection = target.createSection(key);
                    changed = true;
                }
                changed |= mergeMissingInto(targetSection, defaultSection);
                continue;
            }
            if (!target.contains(key)) {
                target.set(key, defaults.get(key));
                changed = true;
            }
        }
        return changed;
    }

    private YamlConfiguration loadBundledConfiguration(String locale) {
        String resourcePath = "lang/" + locale + ".yml";
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                return null;
            }
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return yaml;
        } catch (IOException | InvalidConfigurationException exception) {
            throw new IllegalStateException("Failed to read bundled lang/" + locale + ".yml", exception);
        }
    }

    private boolean copyBundledLocale(String locale) {
        String resourcePath = "lang/" + locale + ".yml";
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                return false;
            }
            Path path = langPath(locale);
            Files.createDirectories(path.getParent());
            Files.copy(stream, path);
            return true;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to copy lang/" + locale + ".yml", exception);
        }
    }

    private Path langPath(String locale) {
        return plugin.getDataFolder().toPath().resolve("lang").resolve(locale + ".yml");
    }

    private Map<String, Object> defaultsFor(String locale) {
        if ("en".equalsIgnoreCase(locale)) {
            return MessageDefaults.en();
        }
        return MessageDefaults.ru();
    }

    private Map<String, Object> loadLocaleMap(String locale) {
        Path path = langPath(locale);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path.toFile());
        Map<String, Object> flat = new LinkedHashMap<>();
        flatten("", yaml, flat);
        mergeMissing(flat, defaultsFor(locale));
        return Map.copyOf(flat);
    }

    private void mergeMissing(Map<String, Object> target, Map<String, Object> defaults) {
        defaults.forEach((key, value) -> target.putIfAbsent(key, value));
    }

    private void flatten(String prefix, ConfigurationSection section, Map<String, Object> target) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (section.isConfigurationSection(key)) {
                flatten(fullKey, section.getConfigurationSection(key), target);
            } else {
                target.put(fullKey, section.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> lore(String locale, String key) {
        Object value = resolve(locale, key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (value == null) {
            return List.of();
        }
        return List.of(String.valueOf(value));
    }

    public String raw(String locale, String key) {
        Object value = resolve(locale, key);
        return value == null ? key : String.valueOf(value);
    }

    public boolean hasKey(String locale, String key) {
        return containsKey(locale, key);
    }

    public boolean containsKey(String locale, String key) {
        Map<String, Map<String, Object>> snapshot = locales;
        Map<String, Object> primary = snapshot.get(locale);
        if (primary != null && primary.containsKey(key)) {
            return true;
        }
        Map<String, Object> fallback = snapshot.get(fallbackLocale);
        return fallback != null && fallback.containsKey(key);
    }

    private Object resolve(String locale, String key) {
        Map<String, Map<String, Object>> snapshot = locales;
        Map<String, Object> primary = snapshot.get(locale);
        if (primary != null && primary.containsKey(key)) {
            return primary.get(key);
        }
        Map<String, Object> fallback = snapshot.get(fallbackLocale);
        if (fallback != null && fallback.containsKey(key)) {
            return fallback.get(key);
        }
        return key;
    }
}
