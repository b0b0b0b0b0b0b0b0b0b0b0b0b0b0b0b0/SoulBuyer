package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.CatalogRotationState;

import java.util.concurrent.CompletableFuture;

public interface CatalogRotationRepository {

    CompletableFuture<CatalogRotationState> load();

    CompletableFuture<Void> save(CatalogRotationState state);
}
