package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataSourceProvider {

    private final HikariDataSource dataSource;

    public DataSourceProvider(JavaPlugin plugin, PluginConfig config) {
        this(plugin, config, resolveMode(config));
    }

    public static DataSourceProvider openSqlite(JavaPlugin plugin, PluginConfig config) {
        return new DataSourceProvider(plugin, config, StorageType.SQLITE);
    }

    public static DataSourceProvider openMysql(JavaPlugin plugin, PluginConfig config) {
        return new DataSourceProvider(plugin, config, StorageType.MYSQL);
    }

    private DataSourceProvider(JavaPlugin plugin, PluginConfig config, StorageType mode) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SoulBuyer");
        hikariConfig.setMaximumPoolSize(resolvePoolSize(config, mode));
        hikariConfig.setConnectionTimeout(resolveTimeout(config, mode));
        if (mode == StorageType.MYSQL) {
            hikariConfig.setJdbcUrl(buildMysqlUrl(config));
            hikariConfig.setUsername(config.mysql().user);
            hikariConfig.setPassword(config.mysql().password);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            Path databasePath = plugin.getDataFolder().toPath().resolve(config.storage().sqliteFile);
            try {
                Path parent = databasePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to create database directory", exception);
            }
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + databasePath.toAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private static StorageType resolveMode(PluginConfig config) {
        if (config.isMysqlStorage()) {
            return StorageType.MYSQL;
        }
        if (config.isSqliteStorage()) {
            return StorageType.SQLITE;
        }
        return StorageType.FLAT;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public void close() {
        dataSource.close();
    }

    private int resolvePoolSize(PluginConfig config, StorageType mode) {
        if (mode == StorageType.MYSQL) {
            return config.mysql().poolSize;
        }
        return config.storage().poolSize;
    }

    private long resolveTimeout(PluginConfig config, StorageType mode) {
        if (mode == StorageType.MYSQL) {
            return config.mysql().connectionTimeoutMs;
        }
        return config.storage().connectionTimeoutMs;
    }

    private String buildMysqlUrl(PluginConfig config) {
        return "jdbc:mysql://"
                + config.mysql().host
                + ":"
                + config.mysql().port
                + "/"
                + config.mysql().database
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
    }
}
