package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.model.GlobalBoosterState;
import java.util.concurrent.CompletableFuture;

public interface GlobalBoosterRepository {

    CompletableFuture<GlobalBoosterState> load();

    CompletableFuture<Void> save(GlobalBoosterState state);
}
