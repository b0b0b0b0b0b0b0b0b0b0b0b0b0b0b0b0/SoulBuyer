package bm.b0b0b0.soulBuyer.booster;

public final class BoosterDurationFormatter {

    private BoosterDurationFormatter() {
    }

    public static String format(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0";
        }
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h";
        }
        if (minutes > 0 && seconds > 0) {
            return minutes + "m " + seconds + "s";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }

    public static String formatMillis(long millis) {
        if (millis <= 0L) {
            return "0";
        }
        return format((int) Math.ceil(millis / 1000.0D));
    }
}
