package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class StorageMetaStore {

    private static final String META_RELATIVE = "data/storage-meta.yml";
    private static final String KEY_ACTIVE_TYPE = "active-type";
    private static final String KEY_UPDATED_AT = "updated-at";

    private StorageMetaStore() {
    }

    public static Optional<StorageType> resolveMigrationSource(
            JavaPlugin plugin,
            PluginConfig config,
            StorageType target
    ) {
        StorageType recorded = readRecordedType(plugin);
        if (recorded != null && recorded != target) {
            return Optional.of(recorded);
        }
        if (recorded != null) {
            return Optional.empty();
        }
        StorageType inferred = StorageDataPresence.inferPopulatedType(plugin, config);
        if (inferred != null && inferred != target) {
            return Optional.of(inferred);
        }
        return Optional.empty();
    }

    public static StorageType readRecordedType(JavaPlugin plugin) {
        Path path = metaPath(plugin);
        if (!Files.exists(path)) {
            return null;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path.toFile());
        String raw = yaml.getString(KEY_ACTIVE_TYPE);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return StorageType.parse(raw);
    }

    public static void save(JavaPlugin plugin, StorageType type) {
        Path path = metaPath(plugin);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            YamlConfiguration yaml = Files.exists(path)
                    ? YamlConfiguration.loadConfiguration(path.toFile())
                    : new YamlConfiguration();
            yaml.set(KEY_ACTIVE_TYPE, type.id());
            yaml.set(KEY_UPDATED_AT, System.currentTimeMillis());
            applyComments(yaml);
            yaml.save(path.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write storage meta", exception);
        }
    }

    private static void applyComments(YamlConfiguration yaml) {
        yaml.options().header(String.join("\n", List.of(
                "╔══════════════════════════════════════════════════════════════════╗",
                "║  НЕ РЕДАКТИРОВАТЬ ЭТОТ ФАЙЛ ВРУЧНУЮ!                             ║",
                "╚══════════════════════════════════════════════════════════════════╝",
                "",
                "Служебный файл SoulBuyer. Пишется и обновляется только плагином при старте.",
                "Нужен, чтобы запомнить, какой storage-type использовался в прошлый раз",
                "(flat / sqlite / mysql) и при смене storage-type в config.yml автоматически",
                "конвертировать данные в новый формат.",
                "",
                "Если изменить active-type руками — миграция может не сработать или пойти не туда.",
                "Меняйте тип хранилища ТОЛЬКО через storage-type в plugins/SoulBuyer/config.yml.",
                "После смены storage-type перезапустите сервер — конвертация выполнится сама.",
                "",
                "Старые файлы/БД после конвертации не удаляются (можно откатить вручную)."
        )));
        yaml.options().copyHeader(true);
        yaml.setComments(KEY_ACTIVE_TYPE, List.of(
                "Последний активный тип хранилища: flat | sqlite | mysql.",
                "Обновляется плагином. Не менять вручную."
        ));
        yaml.setComments(KEY_UPDATED_AT, List.of(
                "Unix-время (мс) последнего обновления meta плагином.",
                "Не менять вручную."
        ));
    }

    private static Path metaPath(JavaPlugin plugin) {
        return plugin.getDataFolder().toPath().resolve(META_RELATIVE);
    }
}
