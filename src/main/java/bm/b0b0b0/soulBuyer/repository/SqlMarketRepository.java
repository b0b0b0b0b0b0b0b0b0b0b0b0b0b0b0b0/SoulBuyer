package bm.b0b0b0.soulBuyer.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SqlMarketRepository implements MarketRepository {

    private final DataSource dataSource;
    private final Executor executor;

    public SqlMarketRepository(DataSource dataSource, Executor executor) {
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Map<String, Double>> loadAllCoefficients() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Double> coefficients = new HashMap<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT item_id, coefficient FROM soulbuyer_market");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coefficients.put(resultSet.getString("item_id"), resultSet.getDouble("coefficient"));
                }
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
            return coefficients;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveCoefficient(String itemId, double coefficient, long soldDelta) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO soulbuyer_market (item_id, coefficient, sold_total, updated_at)
                         VALUES (?, ?, ?, ?)
                         ON DUPLICATE KEY UPDATE
                         coefficient = VALUES(coefficient),
                         sold_total = sold_total + VALUES(sold_total),
                         updated_at = VALUES(updated_at)
                         """)) {
                statement.setString(1, itemId);
                statement.setDouble(2, coefficient);
                statement.setLong(3, soldDelta);
                statement.setLong(4, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }, executor);
    }
}
