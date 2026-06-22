package bm.b0b0b0.soulBuyer.database.migration;

import bm.b0b0b0.soulBuyer.bootstrap.SoulBuyerStartupLog;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.database.*;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class StorageTypeMigrationService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final SoulBuyerDebugLog debug;
    private final SoulBuyerStartupLog startup;

    public StorageTypeMigrationService(
            JavaPlugin plugin,
            PluginConfig config,
            SoulBuyerDebugLog debug,
            SoulBuyerStartupLog startup
    ) {
        this.plugin = plugin;
        this.config = config;
        this.debug = debug;
        this.startup = startup;
    }

    public void migrateIfNeeded() {
        StorageType target = StorageType.parse(config.storageType());
        Optional<StorageType> sourceOptional = StorageMetaStore.resolveMigrationSource(plugin, config, target);
        if (sourceOptional.isEmpty()) {
            StorageMetaStore.save(plugin, target);
            return;
        }

        StorageType source = sourceOptional.get();
        if (source == target) {
            StorageMetaStore.save(plugin, target);
            return;
        }

        if (!StorageDataPresence.hasSourceData(plugin, config, source)) {
            debug.boot("storage migration skipped: source " + source.id() + " has no data");
            StorageMetaStore.save(plugin, target);
            return;
        }

        if (StorageDataPresence.hasTargetData(plugin, config, target)) {
            startup.stepFail("Конвертация пропущена — " + target.id() + " уже содержит данные");
            StorageMetaStore.save(plugin, target);
            return;
        }

        startup.info("Обнаружен новый тип хранилища: " + target.id() + " (был " + source.id() + ")");
        startup.info("Конвертация " + source.id() + " → " + target.id() + "...");

        StorageSnapshot snapshot = export(source);
        importTo(target, snapshot);
        StorageMetaStore.save(plugin, target);
        startup.stepOk("Конвертация завершена: игроков=" + snapshot.playerCount()
                + ", рынок=" + snapshot.marketCount());
        debug.boot("storage migration complete " + source.id() + " -> " + target.id());
    }

    private StorageSnapshot export(StorageType source) {
        if (source == StorageType.FLAT) {
            return FlatStorageMigrator.export(plugin, config);
        }
        DataSourceProvider provider = openSql(source);
        try {
            SchemaMigration.migrate(provider.dataSource(), source == StorageType.SQLITE);
            return SqlStorageMigrator.export(provider.dataSource());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export storage " + source.id(), exception);
        } finally {
            provider.close();
        }
    }

    private void importTo(StorageType target, StorageSnapshot snapshot) {
        if (target == StorageType.FLAT) {
            FlatStorageMigrator.importSnapshot(plugin, config, snapshot);
            return;
        }
        DataSourceProvider provider = openSql(target);
        try {
            SchemaMigration.migrate(provider.dataSource(), target == StorageType.SQLITE);
            SqlStorageMigrator.importSnapshot(provider.dataSource(), snapshot, target == StorageType.SQLITE);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import storage " + target.id(), exception);
        } finally {
            provider.close();
        }
    }

    private DataSourceProvider openSql(StorageType type) {
        if (type == StorageType.MYSQL) {
            return DataSourceProvider.openMysql(plugin, config);
        }
        return DataSourceProvider.openSqlite(plugin, config);
    }
}
