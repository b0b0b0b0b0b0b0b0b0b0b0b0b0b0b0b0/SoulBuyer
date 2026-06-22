package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class StorageDataPresence {

    private StorageDataPresence() {
    }

    public static StorageType inferPopulatedType(JavaPlugin plugin, PluginConfig config) {
        if (hasFlatData(plugin, config)) {
            return StorageType.FLAT;
        }
        if (hasSqliteData(plugin, config)) {
            return StorageType.SQLITE;
        }
        return null;
    }

    public static boolean hasSourceData(JavaPlugin plugin, PluginConfig config, StorageType type) {
        return switch (type) {
            case FLAT -> hasFlatData(plugin, config);
            case SQLITE -> hasSqliteData(plugin, config);
            case MYSQL -> hasMysqlData(plugin, config);
        };
    }

    public static boolean hasTargetData(JavaPlugin plugin, PluginConfig config, StorageType type) {
        return hasSourceData(plugin, config, type);
    }

    public static boolean hasFlatData(JavaPlugin plugin, PluginConfig config) {
        return countYamlFiles(folder(plugin, config.storage().playersFolder)) > 0
                || marketPath(plugin, config) != null;
    }

    public static boolean hasSqliteData(JavaPlugin plugin, PluginConfig config) {
        Path databasePath = plugin.getDataFolder().toPath().resolve(config.storage().sqliteFile);
        if (!Files.exists(databasePath)) {
            return false;
        }
        DataSourceProvider provider = null;
        try {
            provider = DataSourceProvider.openSqlite(plugin, config);
            return sqlPlayerCount(provider.dataSource()) > 0 || sqlMarketCount(provider.dataSource()) > 0;
        } catch (Exception exception) {
            try {
                return Files.size(databasePath) > 0L;
            } catch (IOException ioException) {
                return false;
            }
        } finally {
            if (provider != null) {
                provider.close();
            }
        }
    }

    public static boolean hasMysqlData(JavaPlugin plugin, PluginConfig config) {
        DataSourceProvider provider = null;
        try {
            provider = DataSourceProvider.openMysql(plugin, config);
            return sqlPlayerCount(provider.dataSource()) > 0 || sqlMarketCount(provider.dataSource()) > 0;
        } catch (Exception exception) {
            return false;
        } finally {
            if (provider != null) {
                provider.close();
            }
        }
    }

    private static long countYamlFiles(Path folder) {
        if (!Files.isDirectory(folder)) {
            return 0L;
        }
        try (var stream = Files.list(folder)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(".yml")).count();
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static Path marketPath(JavaPlugin plugin, PluginConfig config) {
        Path path = plugin.getDataFolder().toPath().resolve(config.storage().marketFile);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return Files.size(path) > 0L ? path : null;
        } catch (IOException exception) {
            return null;
        }
    }

    private static Path folder(JavaPlugin plugin, String relative) {
        return plugin.getDataFolder().toPath().resolve(relative);
    }

    private static int sqlPlayerCount(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM soulbuyer_players")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (Exception exception) {
            return 0;
        }
    }

    private static int sqlMarketCount(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM soulbuyer_market")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (Exception exception) {
            return 0;
        }
    }
}
