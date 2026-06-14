package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.bukkit.plugin.java.JavaPlugin;

public final class DataSourceProvider {

    private final HikariDataSource dataSource;

    public DataSourceProvider(JavaPlugin plugin, PluginConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SoulBuyer");
        hikariConfig.setMaximumPoolSize(resolvePoolSize(config));
        hikariConfig.setConnectionTimeout(resolveTimeout(config));
        if (config.isMysqlStorage()) {
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

    public DataSource dataSource() {
        return dataSource;
    }

    public void close() {
        dataSource.close();
    }

    private int resolvePoolSize(PluginConfig config) {
        if (config.isMysqlStorage()) {
            return config.mysql().poolSize;
        }
        return config.storage().poolSize;
    }

    private long resolveTimeout(PluginConfig config) {
        if (config.isMysqlStorage()) {
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
