package bm.b0b0b0.soulBuyer.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public record PlayerBoosterState(UUID playerId, Map<BoosterType, ActiveBooster> active) {

    public static PlayerBoosterState empty(UUID playerId) {
        return new PlayerBoosterState(playerId, new EnumMap<>(BoosterType.class));
    }
}
