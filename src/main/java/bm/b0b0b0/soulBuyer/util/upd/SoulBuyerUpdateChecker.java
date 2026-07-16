package bm.b0b0b0.soulBuyer.util.upd;

import bm.b0b0b0.soulBuyer.util.PluginSchedulers;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public final class SoulBuyerUpdateChecker {

    private static final String VERSION_URL = "https://b0b0b0.dev/pl/souls/soulbuyer.txt";
    private static final String RESOURCE_URL = "https://bm.wtf/resources/11079/";
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
                SoulBuyerConsole.error("Не удалось проверить обновления: не прочитана актуальная версия.");
                return;
            }
            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                printOutdated(currentVersion, latestVersion);
                return;
            }
            SoulBuyerConsole.line(SoulBuyerConsole.green("\u2713 ")
                    + "Проверка обновлений: установлена актуальная версия "
                    + SoulBuyerConsole.green(currentVersion) + ".");
        } catch (Exception exception) {
            SoulBuyerConsole.error("Ошибка проверки обновлений: " + exception.getMessage());
        }
    }

    private static void printOutdated(String currentVersion, String latestVersion) {
        SoulBuyerConsole.blank();
        SoulBuyerConsole.line(SoulBuyerConsole.border());
        SoulBuyerConsole.warn("Доступна новая версия SoulBuyer!");
        SoulBuyerConsole.line("  Текущая: " + SoulBuyerConsole.gray(currentVersion));
        SoulBuyerConsole.line("  Актуальная: " + SoulBuyerConsole.green(latestVersion));
        SoulBuyerConsole.line("  Скачать: " + SoulBuyerConsole.gray(RESOURCE_URL));
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
            SoulBuyerConsole.error("Ошибка подключения к " + VERSION_URL + ": " + exception.getMessage());
            return null;
        }
    }
}
