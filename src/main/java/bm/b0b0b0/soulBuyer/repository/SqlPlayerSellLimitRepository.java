package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.limit.SellLimitUsageCodec;
import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import bm.b0b0b0.soulBuyer.model.SellLine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SqlPlayerSellLimitRepository implements PlayerSellLimitRepository {

    private final DataSource dataSource;
    private final Executor executor;
    private final boolean sqlite;

    public SqlPlayerSellLimitRepository(DataSource dataSource, Executor executor, boolean sqlite) {
        this.dataSource = dataSource;
        this.executor = executor;
        this.sqlite = sqlite;
    }

    @Override
    public CompletableFuture<PlayerSellLimitUsage> find(UUID playerId, String periodKey) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT period_key, usage_data FROM soulbuyer_sell_limits WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedPeriod = resultSet.getString("period_key");
                        if (!periodKey.equals(storedPeriod)) {
                            return PlayerSellLimitUsage.empty(playerId, periodKey);
                        }
                        return SellLimitUsageCodec.decode(playerId, resultSet.getString("usage_data"), periodKey);
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to read sell limits " + playerId, exception);
            }
            return PlayerSellLimitUsage.empty(playerId, periodKey);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerSellLimitUsage usage) {
        return CompletableFuture.runAsync(() -> {
            String sql = sqlite
                    ? """
                    INSERT INTO soulbuyer_sell_limits (player_uuid, period_key, usage_data, updated_at)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(player_uuid) DO UPDATE SET
                        period_key = excluded.period_key,
                        usage_data = excluded.usage_data,
                        updated_at = excluded.updated_at
                    """
                    : """
                    INSERT INTO soulbuyer_sell_limits (player_uuid, period_key, usage_data, updated_at)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE period_key = VALUES(period_key),
                        usage_data = VALUES(usage_data), updated_at = VALUES(updated_at)
                    """;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, usage.playerId().toString());
                statement.setString(2, usage.periodKey());
                statement.setString(3, SellLimitUsageCodec.encode(usage));
                statement.setLong(4, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to save sell limits " + usage.playerId(), exception);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> recordSale(UUID playerId, String periodKey, List<SellLine> lines) {
        return find(playerId, periodKey).thenCompose(existing -> {
            Map<String, Integer> sold = new HashMap<>(existing.soldByItemId());
            for (SellLine line : lines) {
                sold.merge(line.itemId(), line.amount(), Integer::sum);
            }
            return save(new PlayerSellLimitUsage(playerId, periodKey, sold));
        });
    }
}
