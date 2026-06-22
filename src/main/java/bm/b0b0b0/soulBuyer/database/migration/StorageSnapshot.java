package bm.b0b0b0.soulBuyer.database.migration;

import bm.b0b0b0.soulBuyer.model.PlayerProgress;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record StorageSnapshot(
        List<PlayerProgress> players,
        List<MarketItemSnapshot> market,
        Map<UUID, String> autosellPayloads,
        Map<UUID, String> boosterPayloads,
        Map<UUID, String> sellLimitPayloads
) {

    public static StorageSnapshot empty() {
        return new StorageSnapshot(List.of(), List.of(), Map.of(), Map.of(), Map.of());
    }

    public int playerCount() {
        return players.size();
    }

    public int marketCount() {
        return market.size();
    }
}
