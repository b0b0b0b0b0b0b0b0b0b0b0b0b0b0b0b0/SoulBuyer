package bm.b0b0b0.soulBuyer.limit;

import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SellLimitUsageCodec {

    private SellLimitUsageCodec() {
    }

    public static String encode(PlayerSellLimitUsage usage) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("period-key", usage.periodKey());
        for (Map.Entry<String, Integer> entry : usage.soldByItemId().entrySet()) {
            yaml.set("sold." + entry.getKey(), entry.getValue());
        }
        return yaml.saveToString();
    }

    public static PlayerSellLimitUsage decode(UUID playerId, String payload, String currentPeriodKey) {
        if (payload == null || payload.isBlank()) {
            return PlayerSellLimitUsage.empty(playerId, currentPeriodKey);
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid sell limit payload for " + playerId, exception);
        }
        String periodKey = yaml.getString("period-key", currentPeriodKey);
        if (!currentPeriodKey.equals(periodKey)) {
            return PlayerSellLimitUsage.empty(playerId, currentPeriodKey);
        }
        Map<String, Integer> sold = new HashMap<>();
        if (yaml.isConfigurationSection("sold")) {
            for (String itemId : yaml.getConfigurationSection("sold").getKeys(false)) {
                sold.put(itemId, yaml.getInt("sold." + itemId));
            }
        }
        return new PlayerSellLimitUsage(playerId, periodKey, sold);
    }
}
