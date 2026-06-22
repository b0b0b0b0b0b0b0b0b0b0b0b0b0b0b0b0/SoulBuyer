package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerDailySaleStats;
import bm.b0b0b0.soulBuyer.model.SellLine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SqlSaleLogRepository implements SaleLogRepository {

    private final DataSource dataSource;
    private final Executor executor;
    private final List<PendingSale> pending = new ArrayList<>();

    public SqlSaleLogRepository(DataSource dataSource, Executor executor) {
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public synchronized void enqueue(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
        pending.add(new PendingSale(playerId, serverId, List.copyOf(lines), money, points));
    }

    @Override
    public synchronized void flushPending() {
        drainPending();
    }

    @Override
    public synchronized CompletableFuture<Void> drainPending() {
        if (pending.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<PendingSale> batch = new ArrayList<>(pending);
        pending.clear();
        return CompletableFuture.runAsync(() -> writeBatch(batch), executor);
    }

    @Override
    public CompletableFuture<Void> append(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
        return CompletableFuture.runAsync(() -> writeBatch(List.of(new PendingSale(playerId, serverId, lines, money, points))), executor);
    }

    @Override
    public CompletableFuture<PlayerDailySaleStats> loadDailyStats(UUID playerId, long sinceEpochMs) {
        return CompletableFuture.supplyAsync(() -> queryDailyStats(playerId, sinceEpochMs), executor);
    }

    private PlayerDailySaleStats queryDailyStats(UUID playerId, long sinceEpochMs) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT COALESCE(SUM(money), 0), COALESCE(SUM(points), 0), COUNT(*)
                     FROM soulbuyer_sales
                     WHERE player_uuid = ? AND created_at >= ?
                     """)) {
            statement.setString(1, playerId.toString());
            statement.setLong(2, sinceEpochMs);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return PlayerDailySaleStats.empty();
                }
                return new PlayerDailySaleStats(
                        resultSet.getDouble(1),
                        resultSet.getDouble(2),
                        resultSet.getInt(3)
                );
            }
        } catch (Exception exception) {
            return PlayerDailySaleStats.empty();
        }
    }

    private void writeBatch(List<PendingSale> batch) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO soulbuyer_sales (player_uuid, server_id, item_id, amount, money, points, created_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """)) {
            long now = System.currentTimeMillis();
            for (PendingSale sale : batch) {
                for (SellLine line : sale.lines()) {
                    statement.setString(1, sale.playerId().toString());
                    statement.setString(2, sale.serverId());
                    statement.setString(3, line.itemId());
                    statement.setInt(4, line.amount());
                    statement.setDouble(5, line.totalMoney());
                    statement.setDouble(6, line.totalPoints());
                    statement.setLong(7, now);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record PendingSale(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
    }
}
