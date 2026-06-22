package bm.b0b0b0.soulBuyer.booster;

import bm.b0b0b0.soulBuyer.model.ActiveBooster;
import bm.b0b0b0.soulBuyer.model.BoosterType;
import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BoosterStateCodec {

    private BoosterStateCodec() {
    }

    public static String encode(PlayerBoosterState state) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<BoosterType, ActiveBooster> entry : state.active().entrySet()) {
            ActiveBooster booster = entry.getValue();
            String path = "active." + entry.getKey().name().toLowerCase();
            yaml.set(path + ".effect", booster.effect());
            yaml.set(path + ".expires-at", booster.expiresAtMillis());
        }
        return yaml.saveToString();
    }

    public static PlayerBoosterState decode(UUID playerId, String payload) {
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(BoosterType.class);
        if (payload == null || payload.isBlank()) {
            return new PlayerBoosterState(playerId, active);
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid booster payload for " + playerId, exception);
        }
        if (!yaml.isConfigurationSection("active")) {
            return new PlayerBoosterState(playerId, active);
        }
        for (String key : yaml.getConfigurationSection("active").getKeys(false)) {
            BoosterType type = BoosterType.parse(key);
            double effect = yaml.getDouble("active." + key + ".effect");
            long expiresAt = yaml.getLong("active." + key + ".expires-at");
            active.put(type, new ActiveBooster(type, effect, expiresAt));
        }
        return new PlayerBoosterState(playerId, active);
    }
}
