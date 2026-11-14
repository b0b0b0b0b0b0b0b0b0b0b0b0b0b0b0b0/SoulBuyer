package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.booster.BoosterStateCodec;
import bm.b0b0b0.soulBuyer.model.GlobalBoosterState;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public final class SqlGlobalBoosterRepository implements GlobalBoosterRepository {

    private static final int ROW_ID = 1;

    private final DataSource dataSource;
    private final Executor executor;
    private final boolean sqlite;

    public SqlGlobalBoosterRepository(DataSource dataSource, Executor executor, boolean sqlite) {
        this.dataSource = dataSource;
        this.executor = executor;
        this.sqlite = sqlite;
    }

    @Override
    public CompletableFuture<GlobalBoosterState> load() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT state FROM soulbuyer_global_boosters WHERE id = ?")) {
                statement.setInt(1, ROW_ID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return BoosterStateCodec.decodeGlobal(resultSet.getString("state"));
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to read global boosters", exception);
            }
            return GlobalBoosterState.empty();
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(GlobalBoosterState state) {
        return CompletableFuture.runAsync(() -> {
            String sql = sqlite
                    ? """
                    INSERT INTO soulbuyer_global_boosters (id, state, updated_at)
                    VALUES (?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                        state = excluded.state,
                        updated_at = excluded.updated_at
                    """
                    : """
                    INSERT INTO soulbuyer_global_boosters (id, state, updated_at)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE state = VALUES(state), updated_at = VALUES(updated_at)
                    """;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, ROW_ID);
                statement.setString(2, BoosterStateCodec.encode(state));
                statement.setLong(3, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to save global boosters", exception);
            }
        }, executor);
    }
}
