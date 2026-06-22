package bm.b0b0b0.soulBuyer.model;

import bm.b0b0b0.soulBuyer.autosell.AutosellPayout;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record PlayerAutosellSettings(
        UUID playerId,
        boolean enabled,
        String trigger,
        Set<String> categories,
        String notifyMode,
        double minUnitPrice,
        String payoutTarget
) {

    public static PlayerAutosellSettings defaults(UUID playerId, SoulBuyerSettings.AutosellSettings defaults) {
        return new PlayerAutosellSettings(
                playerId,
                defaults.defaultEnabled,
                defaults.defaultTrigger,
                new LinkedHashSet<>(defaults.defaultCategories),
                defaults.defaultNotify,
                defaults.defaultMinUnitPrice,
                AutosellPayout.normalize(defaults.defaultPayout)
        );
    }

    public PlayerAutosellSettings withEnabled(boolean value) {
        return new PlayerAutosellSettings(playerId, value, trigger, categories, notifyMode, minUnitPrice, payoutTarget);
    }

    public PlayerAutosellSettings withTrigger(String value) {
        return new PlayerAutosellSettings(playerId, enabled, value, categories, notifyMode, minUnitPrice, payoutTarget);
    }

    public PlayerAutosellSettings withNotifyMode(String value) {
        return new PlayerAutosellSettings(playerId, enabled, trigger, categories, value, minUnitPrice, payoutTarget);
    }

    public PlayerAutosellSettings withMinUnitPrice(double value) {
        return new PlayerAutosellSettings(playerId, enabled, trigger, categories, notifyMode, value, payoutTarget);
    }

    public PlayerAutosellSettings withCategories(Set<String> value) {
        return new PlayerAutosellSettings(
                playerId,
                enabled,
                trigger,
                new LinkedHashSet<>(value),
                notifyMode,
                minUnitPrice,
                payoutTarget
        );
    }

    public PlayerAutosellSettings withPayoutTarget(String value) {
        return new PlayerAutosellSettings(
                playerId,
                enabled,
                trigger,
                categories,
                notifyMode,
                minUnitPrice,
                AutosellPayout.normalize(value)
        );
    }
}
