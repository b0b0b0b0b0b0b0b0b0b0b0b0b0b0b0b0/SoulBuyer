package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProgressRepository {

    CompletableFuture<PlayerProgress> find(UUID playerId);

    CompletableFuture<Void> save(PlayerProgress progress);

    public CompletableFuture<Void> addPointsAndCategoryXp(UUID playerId, double points, Map<String, Double> categoryXpDelta);

    CompletableFuture<Boolean> trySpendPoints(UUID playerId, double amount);
}
