package bm.b0b0b0.soulBuyer.model;

import java.util.Map;
import java.util.UUID;

public record PlayerProgress(UUID playerId, double points, Map<String, Double> categoryXp) {
}
