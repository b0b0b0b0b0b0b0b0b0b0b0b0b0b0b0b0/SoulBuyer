package bm.b0b0b0.soulBuyer.model;

import java.util.List;

public record CatalogRotationState(List<String> activeItemIds, long nextRotationAtMs) {
}
