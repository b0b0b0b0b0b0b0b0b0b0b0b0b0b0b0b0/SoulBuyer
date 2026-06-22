package bm.b0b0b0.soulBuyer.database.migration;

import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.util.SimpleJsonDoubles;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public final class SqlStorageMigrator {

    private SqlStorageMigrator() {
    }

    public static StorageSnapshot export(DataSource dataSource) {
        return new StorageSnapshot(
                exportPlayers(dataSource),
                exportMarket(dataSource),
                exportPayloads(dataSource, "soulbuyer_autosell", "settings"),
                exportPayloads(dataSource, "soulbuyer_boosters", "state"),
                exportPayloads(dataSource, "soulbuyer_sell_limits", "usage_data")
        );
    }

    public static void importSnapshot(DataSource dataSource, StorageSnapshot snapshot, boolean sqlite) {
        importPlayers(dataSource, snapshot.players(), sqlite);
        importMarket(dataSource, snapshot.market(), sqlite);
        importSimplePayloads(dataSource, snapshot.autosellPayloads(), buildAutosellUpsert(sqlite));
        importSimplePayloads(dataSource, snapshot.boosterPayloads(), buildBoosterUpsert(sqlite));
        importSellLimitPayloads(dataSource, snapshot.sellLimitPayloads(), sqlite);
    }

    private static List<PlayerProgress> exportPlayers(DataSource dataSource) {
        List<PlayerProgress> players = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_uuid, points, category_xp FROM soulbuyer_players");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                players.add(new PlayerProgress(
                        UUID.fromString(resultSet.getString("player_uuid")),
                        resultSet.getDouble("points"),
                        new HashMap<>(SimpleJsonDoubles.decode(resultSet.getString("category_xp")))
                ));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export SQL players", exception);
        }
        return players;
    }

    private static List<MarketItemSnapshot> exportMarket(DataSource dataSource) {
        List<MarketItemSnapshot> market = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT item_id, coefficient, sold_total FROM soulbuyer_market");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                market.add(new MarketItemSnapshot(
                        resultSet.getString("item_id"),
                        resultSet.getDouble("coefficient"),
                        resultSet.getLong("sold_total")
                ));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export SQL market", exception);
        }
        return market;
    }

    private static Map<UUID, String> exportPayloads(DataSource dataSource, String table, String column) {
        Map<UUID, String> payloads = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_uuid, " + column + " FROM " + table);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                payloads.put(UUID.fromString(resultSet.getString("player_uuid")), resultSet.getString(column));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export SQL table " + table, exception);
        }
        return payloads;
    }

    private static void importPlayers(DataSource dataSource, List<PlayerProgress> players, boolean sqlite) {
        String sql = sqlite
                ? """
                INSERT INTO soulbuyer_players (player_uuid, points, category_xp, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    points = excluded.points,
                    category_xp = excluded.category_xp,
                    updated_at = excluded.updated_at
                """
                : """
                INSERT INTO soulbuyer_players (player_uuid, points, category_xp, updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE points = VALUES(points), category_xp = VALUES(category_xp),
                updated_at = VALUES(updated_at)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (PlayerProgress progress : players) {
                statement.setString(1, progress.playerId().toString());
                statement.setDouble(2, progress.points());
                statement.setString(3, SimpleJsonDoubles.encode(progress.categoryXp()));
                statement.setLong(4, System.currentTimeMillis());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import SQL players", exception);
        }
    }

    private static void importMarket(DataSource dataSource, List<MarketItemSnapshot> market, boolean sqlite) {
        String sql = sqlite
                ? """
                INSERT INTO soulbuyer_market (item_id, coefficient, sold_total, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(item_id) DO UPDATE SET
                    coefficient = excluded.coefficient,
                    sold_total = excluded.sold_total,
                    updated_at = excluded.updated_at
                """
                : """
                INSERT INTO soulbuyer_market (item_id, coefficient, sold_total, updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE coefficient = VALUES(coefficient), sold_total = VALUES(sold_total),
                updated_at = VALUES(updated_at)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (MarketItemSnapshot item : market) {
                statement.setString(1, item.itemId());
                statement.setDouble(2, item.coefficient());
                statement.setLong(3, item.soldTotal());
                statement.setLong(4, System.currentTimeMillis());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import SQL market", exception);
        }
    }

    private static void importSimplePayloads(
            DataSource dataSource,
            Map<UUID, String> payloads,
            String sql
    ) {
        if (payloads.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<UUID, String> entry : payloads.entrySet()) {
                statement.setString(1, entry.getKey().toString());
                statement.setString(2, entry.getValue());
                statement.setLong(3, System.currentTimeMillis());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import SQL payloads", exception);
        }
    }

    private static void importSellLimitPayloads(DataSource dataSource, Map<UUID, String> payloads, boolean sqlite) {
        if (payloads.isEmpty()) {
            return;
        }
        String sql = buildSellLimitUpsert(sqlite);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<UUID, String> entry : payloads.entrySet()) {
                statement.setString(1, entry.getKey().toString());
                statement.setString(2, extractPeriodKey(entry.getValue()));
                statement.setString(3, entry.getValue());
                statement.setLong(4, System.currentTimeMillis());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import SQL sell limits", exception);
        }
    }

    private static String buildAutosellUpsert(boolean sqlite) {
        return sqlite
                ? """
                INSERT INTO soulbuyer_autosell (player_uuid, settings, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET settings = excluded.settings, updated_at = excluded.updated_at
                """
                : """
                INSERT INTO soulbuyer_autosell (player_uuid, settings, updated_at)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE settings = VALUES(settings), updated_at = VALUES(updated_at)
                """;
    }

    private static String buildBoosterUpsert(boolean sqlite) {
        return sqlite
                ? """
                INSERT INTO soulbuyer_boosters (player_uuid, state, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET state = excluded.state, updated_at = excluded.updated_at
                """
                : """
                INSERT INTO soulbuyer_boosters (player_uuid, state, updated_at)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE state = VALUES(state), updated_at = VALUES(updated_at)
                """;
    }

    private static String buildSellLimitUpsert(boolean sqlite) {
        return sqlite
                ? """
                INSERT INTO soulbuyer_sell_limits (player_uuid, period_key, usage_data, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET period_key = excluded.period_key,
                    usage_data = excluded.usage_data, updated_at = excluded.updated_at
                """
                : """
                INSERT INTO soulbuyer_sell_limits (player_uuid, period_key, usage_data, updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE period_key = VALUES(period_key),
                    usage_data = VALUES(usage_data), updated_at = VALUES(updated_at)
                """;
    }

    private static String extractPeriodKey(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(payload);
        } catch (Exception exception) {
            return "";
        }
        return yaml.getString("period-key", "");
    }
}
