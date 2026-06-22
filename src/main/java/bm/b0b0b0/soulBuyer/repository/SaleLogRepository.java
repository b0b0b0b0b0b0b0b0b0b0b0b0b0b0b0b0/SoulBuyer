package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerDailySaleStats;
import bm.b0b0b0.soulBuyer.model.SellLine;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SaleLogRepository {

    CompletableFuture<Void> append(UUID playerId, String serverId, List<SellLine> lines, double money, double points);

    void enqueue(UUID playerId, String serverId, List<SellLine> lines, double money, double points);

    void flushPending();

    CompletableFuture<Void> drainPending();

    CompletableFuture<PlayerDailySaleStats> loadDailyStats(UUID playerId, long sinceEpochMs);
}
