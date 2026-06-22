package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;

import java.util.*;

public final class AutosellSettingsCodec {

    private AutosellSettingsCodec() {
    }

    public static String encode(PlayerAutosellSettings settings) {
        StringBuilder builder = new StringBuilder();
        builder.append("enabled=").append(settings.enabled()).append('\n');
        builder.append("trigger=").append(settings.trigger()).append('\n');
        builder.append("notify=").append(settings.notifyMode()).append('\n');
        builder.append("min-unit-price=").append(settings.minUnitPrice()).append('\n');
        builder.append("payout=").append(settings.payoutTarget()).append('\n');
        builder.append("categories=");
        boolean first = true;
        for (String category : settings.categories()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(category);
            first = false;
        }
        return builder.toString();
    }

    public static PlayerAutosellSettings decode(
            UUID playerId,
            String payload,
            SoulBuyerSettings.AutosellSettings defaults
    ) {
        if (payload == null || payload.isBlank()) {
            return PlayerAutosellSettings.defaults(playerId, defaults);
        }
        boolean enabled = defaults.defaultEnabled;
        String trigger = defaults.defaultTrigger;
        String notify = defaults.defaultNotify;
        double minUnitPrice = defaults.defaultMinUnitPrice;
        String payout = AutosellPayout.normalize(defaults.defaultPayout);
        Set<String> categories = new LinkedHashSet<>(defaults.defaultCategories);
        for (String line : payload.split("\n")) {
            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            switch (key) {
                case "enabled" -> enabled = Boolean.parseBoolean(value);
                case "trigger" -> trigger = AutosellTrigger.normalize(value);
                case "notify" -> notify = AutosellNotify.normalize(value);
                case "min-unit-price" -> minUnitPrice = parseDouble(value, defaults.defaultMinUnitPrice);
                case "payout" -> payout = AutosellPayout.normalize(value);
                case "categories" -> categories = parseCategories(value, defaults.defaultCategories);
                default -> {
                }
            }
        }
        return new PlayerAutosellSettings(playerId, enabled, trigger, categories, notify, minUnitPrice, payout);
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static Set<String> parseCategories(String value, List<String> fallback) {
        if (value.isBlank()) {
            return new LinkedHashSet<>(fallback);
        }
        Set<String> categories = new LinkedHashSet<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                categories.add(trimmed);
            }
        }
        if (categories.isEmpty()) {
            return new LinkedHashSet<>(fallback);
        }
        return categories;
    }

    public static List<String> splitLines(String payload) {
        List<String> lines = new ArrayList<>();
        if (payload == null) {
            return lines;
        }
        lines.addAll(Arrays.asList(payload.split("\n")));
        return lines;
    }
}
