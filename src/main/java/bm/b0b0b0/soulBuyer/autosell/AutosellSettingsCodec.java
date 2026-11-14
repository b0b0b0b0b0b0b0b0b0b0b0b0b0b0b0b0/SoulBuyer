package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.model.PlayerAutosellSettings;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        appendCsv(builder, settings.categories());
        builder.append('\n');
        builder.append("disabled-items=");
        appendCsv(builder, settings.disabledItems());
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
        Set<String> disabledItems = new LinkedHashSet<>();
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
                case "disabled-items" -> disabledItems = parseCsv(value);
                default -> {
                }
            }
        }
        return new PlayerAutosellSettings(
                playerId,
                enabled,
                trigger,
                categories,
                disabledItems,
                notify,
                minUnitPrice,
                payout
        );
    }

    private static void appendCsv(StringBuilder builder, Iterable<String> values) {
        boolean first = true;
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            builder.append(value);
            first = false;
        }
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
        Set<String> categories = parseCsv(value);
        if (categories.isEmpty()) {
            return new LinkedHashSet<>(fallback);
        }
        return categories;
    }

    private static Set<String> parseCsv(String value) {
        Set<String> values = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return values;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    public static List<String> splitLines(String payload) {
        List<String> lines = new ArrayList<>();
        if (payload == null) {
            return lines;
        }
        for (String line : payload.split("\n")) {
            lines.add(line);
        }
        return lines;
    }
}
