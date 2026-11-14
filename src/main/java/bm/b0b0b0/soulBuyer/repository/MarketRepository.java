package bm.b0b0b0.soulBuyer.repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MarketRepository {

    CompletableFuture<Map<String, Double>> loadAllCoefficients();

    CompletableFuture<Void> saveCoefficient(String itemId, double coefficient, long soldDelta);
}
