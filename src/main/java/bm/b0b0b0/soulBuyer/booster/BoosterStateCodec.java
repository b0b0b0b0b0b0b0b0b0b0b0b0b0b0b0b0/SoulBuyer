package bm.b0b0b0.soulBuyer.booster;

import bm.b0b0b0.soulBuyer.model.ActiveBooster;
import bm.b0b0b0.soulBuyer.model.BoosterType;
import bm.b0b0b0.soulBuyer.model.GlobalBoosterState;
import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BoosterStateCodec {

    private BoosterStateCodec() {
    }

    public static String encode(PlayerBoosterState state) {
        return encodeActive(state.active());
    }

    public static String encode(GlobalBoosterState state) {
        return encodeActive(state.active());
    }

    public static PlayerBoosterState decode(UUID playerId, String payload) {
        return new PlayerBoosterState(playerId, decodeActive(payload));
    }

    public static GlobalBoosterState decodeGlobal(String payload) {
        return new GlobalBoosterState(decodeActive(payload));
    }

    private static String encodeActive(Map<BoosterType, ActiveBooster> active) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<BoosterType, ActiveBooster> entry : active.entrySet()) {
            ActiveBooster booster = entry.getValue();
            String path = "active." + entry.getKey().name().toLowerCase();
            yaml.set(path + ".effect", booster.effect());
            yaml.set(path + ".expires-at", booster.expiresAtMillis());
        }
        return yaml.saveToString();
    }

    private static Map<BoosterType, ActiveBooster> decodeActive(String payload) {
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(BoosterType.class);
        if (payload == null || payload.isBlank()) {
            return active;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid booster payload", exception);
        }
        if (!yaml.isConfigurationSection("active")) {
            return active;
        }
        for (String key : yaml.getConfigurationSection("active").getKeys(false)) {
            BoosterType type = BoosterType.parse(key);
            double effect = yaml.getDouble("active." + key + ".effect");
            long expiresAt = yaml.getLong("active." + key + ".expires-at");
            active.put(type, new ActiveBooster(type, effect, expiresAt));
        }
        return active;
    }
}
