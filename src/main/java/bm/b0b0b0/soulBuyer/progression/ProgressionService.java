package bm.b0b0b0.soulBuyer.progression;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Map;

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
        if (!Double.isFinite(dominant) || dominant <= 0.0D) {
            return 1.0D;
        }
        double xpPerLevel = Math.max(1.0D, config.progression().categoryXpPerLevel);
        double level = dominant / xpPerLevel;
        double bonus = 1.0D + level * config.progression().dominantCategoryBonusPerLevel / 100.0D;
        double cap = Math.max(1.0D, config.progression().maxCategoryBonus);
        return Math.min(bonus, cap);
    }

    public double pointsForSale(SellableItemDefinition definition, int amount, double unitPriceBasis) {
        double basis = sanitizeUnit(unitPriceBasis);
        double basePoints = definition.basePoints() * amount;
        if (config.progression().pointsPerCurrency <= 0.0D) {
            return sanitizeUnit(basePoints);
        }
        return sanitizeUnit(basePoints + basis * amount * config.progression().pointsPerCurrency);
    }

    public double moneyUnitPrice(
            double basePrice,
            double marketCoefficient,
            double permissionMultiplier,
            double categoryBonus,
            double additiveMultiplier,
            double moneyBooster
    ) {
        double factor = permissionMultiplier * categoryBonus + additiveMultiplier;
        return sanitizeUnit(basePrice * marketCoefficient * factor * moneyBooster);
    }

    public double pointsUnitPriceBasis(double basePrice, double marketCoefficient, double permissionMultiplier) {
        return sanitizeUnit(basePrice * marketCoefficient * permissionMultiplier);
    }

    public boolean isValidPayout(double totalMoney) {
        if (!Double.isFinite(totalMoney) || totalMoney < 0.0D) {
            return false;
        }
        return totalMoney <= config.progression().maxPayoutPerSale;
    }

    public Map<String, Double> categoryXpDelta(SellableItemDefinition definition, double pointsEarned) {
        double xp = sanitizeUnit(pointsEarned * config.progression().categoryXpPerPoint);
        return Map.of(definition.categoryId(), xp);
    }

    private double sanitizeUnit(double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return Math.min(value, config.progression().maxUnitPrice);
    }
}
