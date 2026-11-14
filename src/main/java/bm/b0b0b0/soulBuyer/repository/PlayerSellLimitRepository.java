package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import bm.b0b0b0.soulBuyer.model.SellLine;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerSellLimitRepository {

    CompletableFuture<PlayerSellLimitUsage> find(UUID playerId, String periodKey);

    CompletableFuture<Void> save(PlayerSellLimitUsage usage);

    CompletableFuture<Void> recordSale(UUID playerId, String periodKey, List<SellLine> lines);
}
