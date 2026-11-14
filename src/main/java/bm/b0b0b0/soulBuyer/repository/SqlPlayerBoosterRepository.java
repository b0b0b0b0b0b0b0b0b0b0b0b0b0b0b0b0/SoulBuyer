package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.booster.BoosterStateCodec;
import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public final class SqlPlayerBoosterRepository implements PlayerBoosterRepository {

    private final DataSource dataSource;
    private final Executor executor;
    private final boolean sqlite;

    public SqlPlayerBoosterRepository(DataSource dataSource, Executor executor, boolean sqlite) {
        this.dataSource = dataSource;
        this.executor = executor;
        this.sqlite = sqlite;
    }

    @Override
    public CompletableFuture<PlayerBoosterState> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT state FROM soulbuyer_boosters WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return BoosterStateCodec.decode(playerId, resultSet.getString("state"));
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to read booster state " + playerId, exception);
            }
            return PlayerBoosterState.empty(playerId);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerBoosterState state) {
        return CompletableFuture.runAsync(() -> {
            String sql = sqlite
                    ? """
                    INSERT INTO soulbuyer_boosters (player_uuid, state, updated_at)
                    VALUES (?, ?, ?)
                    ON CONFLICT(player_uuid) DO UPDATE SET
                        state = excluded.state,
                        updated_at = excluded.updated_at
                    """
                    : """
                    INSERT INTO soulbuyer_boosters (player_uuid, state, updated_at)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE state = VALUES(state), updated_at = VALUES(updated_at)
                    """;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, state.playerId().toString());
                statement.setString(2, BoosterStateCodec.encode(state));
                statement.setLong(3, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to save booster state " + state.playerId(), exception);
            }
        }, executor);
    }
}
