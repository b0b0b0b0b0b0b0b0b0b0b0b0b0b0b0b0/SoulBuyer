package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerBoosterRepository {

    CompletableFuture<PlayerBoosterState> find(UUID playerId);

    CompletableFuture<Void> save(PlayerBoosterState state);
}
