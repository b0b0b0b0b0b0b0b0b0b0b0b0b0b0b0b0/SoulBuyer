package bm.b0b0b0.soulBuyer.util.upd;

import bm.b0b0b0.soulBuyer.util.PluginSchedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulBuyerUpdateChecker {

    private static final String VERSION_URL = "https://b0b0b0.dev/pl/souls/soulbuyer.txt";
    private static final String RESOURCE_URL = "https://bm.wtf/resources/11079/";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/soulbuyer";
    private static final String MODRINTH_URL_RU = "https://modrinth.black/plugin/soulbuyer";
    private static final String HANGAR_URL = "https://hangar.papermc.io/b0b/SoulBuyer";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private SoulBuyerUpdateChecker() {
    }

    public static void schedule(JavaPlugin plugin, String currentVersion) {
        PluginSchedulers.runAsyncLater(plugin, () -> check(currentVersion), 60L);
    }

    public static void check(String currentVersion) {
        try {
            String latestVersion = fetchLatestVersion();
            if (latestVersion == null) {
                SoulBuyerConsole.error("Update check failed: could not read latest version.");
                return;
            }
            if (compareVersions(latestVersion, currentVersion) > 0) {
                printOutdated(currentVersion, latestVersion);
                return;
            }
            SoulBuyerConsole.line(SoulBuyerConsole.green("\u2713 ")
                    + "Update check: running latest version "
                    + SoulBuyerConsole.green(currentVersion) + ".");
        } catch (Exception exception) {
            SoulBuyerConsole.error("Update check error: " + exception.getMessage());
        }
    }

    static int compareVersions(String left, String right) {
        String[] leftParts = left.trim().split("\\.");
        String[] rightParts = right.trim().split("\\.");
        int length = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < length; i++) {
            int leftNumber = i < leftParts.length ? leadingNumber(leftParts[i]) : 0;
            int rightNumber = i < rightParts.length ? leadingNumber(rightParts[i]) : 0;
            if (leftNumber != rightNumber) {
                return Integer.compare(leftNumber, rightNumber);
            }
        }
        return 0;
    }

    private static int leadingNumber(String part) {
        int end = 0;
        while (end < part.length() && Character.isDigit(part.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(part.substring(0, end));
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private static void printOutdated(String currentVersion, String latestVersion) {
        SoulBuyerConsole.blank();
        SoulBuyerConsole.line(SoulBuyerConsole.border());
        SoulBuyerConsole.warn("A new SoulBuyer version is available!");
        SoulBuyerConsole.line("  Current: " + SoulBuyerConsole.gray(currentVersion));
        SoulBuyerConsole.line("  Latest: " + SoulBuyerConsole.green(latestVersion));
        SoulBuyerConsole.line("  Download: ");
        SoulBuyerConsole.line("    BM: " + SoulBuyerConsole.gray(RESOURCE_URL));
        SoulBuyerConsole.line("    Modrinth: " + SoulBuyerConsole.gray(MODRINTH_URL));
        SoulBuyerConsole.line("     Modrinth (RU): " + SoulBuyerConsole.gray(MODRINTH_URL_RU));
        SoulBuyerConsole.line("    Hangar: " + SoulBuyerConsole.gray(HANGAR_URL));
        SoulBuyerConsole.line(SoulBuyerConsole.border());
        SoulBuyerConsole.blank();
    }

    private static String fetchLatestVersion() {
        try {
            URL url = URI.create(VERSION_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line = reader.readLine();
                return line == null ? null : line.trim();
            }
        } catch (IOException exception) {
            SoulBuyerConsole.error("Connection error to " + VERSION_URL + ": " + exception.getMessage());
            return null;
        }
    }
}
