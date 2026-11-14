package bm.b0b0b0.soulBuyer.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record PlayerSellLimitUsage(UUID playerId, String periodKey, Map<String, Integer> soldByItemId) {

    public static PlayerSellLimitUsage empty(UUID playerId, String periodKey) {
        return new PlayerSellLimitUsage(playerId, periodKey, new HashMap<>());
    }

    public int sold(String itemId) {
        return soldByItemId.getOrDefault(itemId, 0);
    }
}
