package bm.b0b0b0.soulBuyer.message;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageLoader {

    private final JavaPlugin plugin;
    private final String defaultLocale;
    private final String fallbackLocale;
    private final Map<String, Map<String, Object>> locales = new LinkedHashMap<>();

    public MessageLoader(JavaPlugin plugin, String defaultLocale, String fallbackLocale) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale;
        this.fallbackLocale = fallbackLocale;
    }

    public synchronized void load() {
        locales.clear();
        ensureLocaleFile("ru");
        ensureLocaleFile("en");
        loadLocale(defaultLocale);
        if (!fallbackLocale.equals(defaultLocale)) {
            loadLocale(fallbackLocale);
        }
    }

    public String defaultLocale() {
        return defaultLocale;
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

    private void loadLocale(String locale) {
        Path path = langPath(locale);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path.toFile());
        Map<String, Object> flat = new LinkedHashMap<>();
        flatten("", yaml, flat);
        mergeMissing(flat, defaultsFor(locale));
        locales.put(locale, flat);
    }

    private void mergeMissing(Map<String, Object> target, Map<String, Object> defaults) {
        defaults.forEach(target::putIfAbsent);
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

    private Object resolve(String locale, String key) {
        Map<String, Object> primary = locales.get(locale);
        if (primary != null && primary.containsKey(key)) {
            return primary.get(key);
        }
        Map<String, Object> fallback = locales.get(fallbackLocale);
        if (fallback != null && fallback.containsKey(key)) {
            return fallback.get(key);
        }
        return key;
    }
}
