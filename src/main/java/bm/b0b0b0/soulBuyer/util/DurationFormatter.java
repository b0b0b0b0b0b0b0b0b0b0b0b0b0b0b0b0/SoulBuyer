package bm.b0b0b0.soulBuyer.util;

import java.util.Locale;

public final class DurationFormatter {

    private DurationFormatter() {
    }

    public static String formatSeconds(long totalSeconds, Locale locale) {
        long seconds = Math.max(0L, totalSeconds);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if ("ru".equalsIgnoreCase(locale.getLanguage())) {
            if (hours > 0L) {
                return hours + "ч " + minutes + "м";
            }
            if (minutes > 0L) {
                return minutes + "м " + secs + "с";
            }
            return secs + "с";
        }
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0L) {
            return minutes + "m " + secs + "s";
        }
        return secs + "s";
    }
}
