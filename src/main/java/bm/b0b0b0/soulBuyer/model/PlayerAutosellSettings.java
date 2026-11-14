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
        Set<String> disabledItems,
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
                Set.of(),
                defaults.defaultNotify,
                defaults.defaultMinUnitPrice,
                AutosellPayout.normalize(defaults.defaultPayout)
        );
    }

    public boolean isItemDisabled(String itemId) {
        return itemId != null && disabledItems.contains(itemId);
    }

    public PlayerAutosellSettings withEnabled(boolean value) {
        return new PlayerAutosellSettings(
                playerId, value, trigger, categories, disabledItems, notifyMode, minUnitPrice, payoutTarget
        );
    }

    public PlayerAutosellSettings withTrigger(String value) {
        return new PlayerAutosellSettings(
                playerId, enabled, value, categories, disabledItems, notifyMode, minUnitPrice, payoutTarget
        );
    }

    public PlayerAutosellSettings withNotifyMode(String value) {
        return new PlayerAutosellSettings(
                playerId, enabled, trigger, categories, disabledItems, value, minUnitPrice, payoutTarget
        );
    }

    public PlayerAutosellSettings withMinUnitPrice(double value) {
        return new PlayerAutosellSettings(
                playerId, enabled, trigger, categories, disabledItems, notifyMode, value, payoutTarget
        );
    }

    public PlayerAutosellSettings withCategories(Set<String> value) {
        return new PlayerAutosellSettings(
                playerId,
                enabled,
                trigger,
                new LinkedHashSet<>(value),
                disabledItems,
                notifyMode,
                minUnitPrice,
                payoutTarget
        );
    }

    public PlayerAutosellSettings withDisabledItems(Set<String> value) {
        return new PlayerAutosellSettings(
                playerId,
                enabled,
                trigger,
                categories,
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
                disabledItems,
                notifyMode,
                minUnitPrice,
                AutosellPayout.normalize(value)
        );
    }
}
