package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerAutosellRepository {

    CompletableFuture<PlayerAutosellSettings> find(UUID playerId);

    CompletableFuture<Void> save(PlayerAutosellSettings settings);
}
