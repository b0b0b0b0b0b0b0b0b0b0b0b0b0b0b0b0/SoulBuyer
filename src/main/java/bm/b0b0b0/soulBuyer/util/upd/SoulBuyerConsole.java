package bm.b0b0b0.soulBuyer.util.upd;

import bm.b0b0b0.soulBuyer.bootstrap.SoulBuyerStartupLog;
import org.bukkit.Bukkit;

final class SoulBuyerConsole {

    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";
    private static final String WHITE = "\u001B[37m";
    private static final String RESET = "\u001B[0m";

    private SoulBuyerConsole() {
    }

    static void blank() {
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    static void line(String message) {
        Bukkit.getConsoleSender().sendMessage(SoulBuyerStartupLog.PREFIX + message);
    }

    static void success(String message) {
        Bukkit.getConsoleSender().sendMessage(SoulBuyerStartupLog.PREFIX + GREEN + message + RESET);
    }

    static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(SoulBuyerStartupLog.PREFIX + YELLOW + message + RESET);
    }

    static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(SoulBuyerStartupLog.PREFIX + RED + message + RESET);
    }

    static String gray(String message) {
        return GRAY + message + RESET;
    }

    static String green(String message) {
        return GREEN + message + RESET;
    }

    static String cyan(String message) {
        return CYAN + message + RESET;
    }

    static String white(String message) {
        return WHITE + message + RESET;
    }

    static String border() {
        return GRAY + "==============================================" + RESET;
    }
}
