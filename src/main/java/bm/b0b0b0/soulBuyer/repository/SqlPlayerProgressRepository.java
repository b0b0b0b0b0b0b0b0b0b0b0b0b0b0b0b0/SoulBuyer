package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.util.SimpleJsonDoubles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public final class SqlPlayerProgressRepository implements PlayerProgressRepository {

    private final DataSource dataSource;
    private final Executor executor;

    public SqlPlayerProgressRepository(DataSource dataSource, Executor executor) {
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<PlayerProgress> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT points, category_xp FROM soulbuyer_players WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlayerProgress(
                                playerId,
                                resultSet.getDouble("points"),
                                parseCategoryXp(resultSet.getString("category_xp"))
                        );
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
            return new PlayerProgress(playerId, 0.0D, new HashMap<>());
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerProgress progress) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO soulbuyer_players (player_uuid, points, category_xp, updated_at)
                         VALUES (?, ?, ?, ?)
                         ON DUPLICATE KEY UPDATE points = VALUES(points), category_xp = VALUES(category_xp),
                         updated_at = VALUES(updated_at)
                         """)) {
                statement.setString(1, progress.playerId().toString());
                statement.setDouble(2, progress.points());
                statement.setString(3, SimpleJsonDoubles.encode(progress.categoryXp()));
                statement.setLong(4, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> addPointsAndCategoryXp(UUID playerId, double points, Map<String, Double> categoryXpDelta) {
        return find(playerId).thenCompose(existing -> {
            Map<String, Double> categoryXp = new HashMap<>(existing.categoryXp());
            categoryXpDelta.forEach((key, value) -> categoryXp.merge(key, value, Double::sum));
            return save(new PlayerProgress(playerId, existing.points() + points, categoryXp));
        });
    }

    @Override
    public CompletableFuture<Boolean> trySpendPoints(UUID playerId, double amount) {
        return find(playerId).thenCompose(existing -> {
            if (existing.points() + 1.0E-9D < amount) {
                return CompletableFuture.completedFuture(false);
            }
            return save(new PlayerProgress(playerId, existing.points() - amount, existing.categoryXp()))
                    .thenApply(ignored -> true);
        });
    }

    private Map<String, Double> parseCategoryXp(String json) {
        return new HashMap<>(SimpleJsonDoubles.decode(json));
    }
}
