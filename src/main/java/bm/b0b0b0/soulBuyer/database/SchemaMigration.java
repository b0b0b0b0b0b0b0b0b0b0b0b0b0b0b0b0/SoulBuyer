package bm.b0b0b0.soulBuyer.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaMigration {

    private SchemaMigration() {
    }

    public static void migrate(DataSource dataSource, boolean sqlite) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_schema (
                        version INT NOT NULL PRIMARY KEY
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_players (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        points DOUBLE NOT NULL DEFAULT 0,
                        category_xp TEXT NOT NULL,
                        updated_at BIGINT NOT NULL DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_market (
                        item_id VARCHAR(64) PRIMARY KEY,
                        coefficient DOUBLE NOT NULL DEFAULT 1,
                        sold_total BIGINT NOT NULL DEFAULT 0,
                        updated_at BIGINT NOT NULL DEFAULT 0
                    )
                    """);
            if (sqlite) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS soulbuyer_sales (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            player_uuid VARCHAR(36) NOT NULL,
                            server_id VARCHAR(64) NOT NULL,
                            item_id VARCHAR(64) NOT NULL,
                            amount INT NOT NULL,
                            money DOUBLE NOT NULL,
                            points DOUBLE NOT NULL,
                            created_at BIGINT NOT NULL
                        )
                        """);
                statement.execute("INSERT OR IGNORE INTO soulbuyer_schema (version) VALUES (1)");
            } else {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS soulbuyer_sales (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            player_uuid VARCHAR(36) NOT NULL,
                            server_id VARCHAR(64) NOT NULL,
                            item_id VARCHAR(64) NOT NULL,
                            amount INT NOT NULL,
                            money DOUBLE NOT NULL,
                            points DOUBLE NOT NULL,
                            created_at BIGINT NOT NULL
                        )
                        """);
                statement.execute("INSERT IGNORE INTO soulbuyer_schema (version) VALUES (1)");
            }
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_autosell (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        settings TEXT NOT NULL,
                        updated_at BIGINT NOT NULL DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_boosters (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        state TEXT NOT NULL,
                        updated_at BIGINT NOT NULL DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulbuyer_sell_limits (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        period_key VARCHAR(16) NOT NULL,
                        usage_data TEXT NOT NULL,
                        updated_at BIGINT NOT NULL DEFAULT 0
                    )
                    """);
            if (sqlite) {
                statement.execute("INSERT OR IGNORE INTO soulbuyer_schema (version) VALUES (2)");
            } else {
                statement.execute("INSERT IGNORE INTO soulbuyer_schema (version) VALUES (2)");
            }
        }
    }
}
