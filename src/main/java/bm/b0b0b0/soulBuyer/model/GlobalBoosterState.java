package bm.b0b0b0.soulBuyer.model;

import java.util.EnumMap;
import java.util.Map;

public record GlobalBoosterState(Map<BoosterType, ActiveBooster> active) {

    public static GlobalBoosterState empty() {
        return new GlobalBoosterState(new EnumMap<>(BoosterType.class));
    }
}
