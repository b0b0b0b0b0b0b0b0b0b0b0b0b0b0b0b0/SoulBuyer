package bm.b0b0b0.soulBuyer.progression;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import java.util.Comparator;
import java.util.Map;
import org.bukkit.entity.Player;

public final class ProgressionService {

    private final PluginConfig config;

    public ProgressionService(PluginConfig config) {
        this.config = config;
    }

    public double permissionMultiplier(Player player) {
        double multiplier = 1.0D;
        for (Map.Entry<String, Double> entry : config.progression().permissionMultipliers.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                multiplier = Math.max(multiplier, entry.getValue());
            }
        }
        return multiplier;
    }

    public double categoryBonus(PlayerProgress progress) {
        if (progress.categoryXp().isEmpty()) {
            return 1.0D;
        }
        double dominant = progress.categoryXp().values().stream()
                .max(Comparator.naturalOrder())
                .orElse(0.0D);
        double bonusPercent = dominant * config.progression().dominantCategoryBonusPerLevel / 100.0D;
        return 1.0D + bonusPercent;
    }

    public double pointsForSale(SellableItemDefinition definition, int amount, double unitPrice) {
        double basePoints = definition.basePoints() * amount;
        if (config.progression().pointsPerCurrency <= 0.0D) {
            return basePoints;
        }
        return basePoints + unitPrice * amount * config.progression().pointsPerCurrency;
    }

    public Map<String, Double> categoryXpDelta(SellableItemDefinition definition, double pointsEarned) {
        double xp = pointsEarned * config.progression().categoryXpPerPoint;
        return Map.of(definition.categoryId(), xp);
    }
}
