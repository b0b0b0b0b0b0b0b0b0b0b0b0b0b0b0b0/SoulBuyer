package bm.b0b0b0.soulBuyer.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public final class SoulBuyerStartupLog {

    public static final String PREFIX = "\u001B[37m[\u001B[90mSoulBuyer\u001B[37m]\u001B[0m ";

    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GRAY = "\u001B[90m";
    private static final String RESET = "\u001B[0m";

    private final ConsoleCommandSender console;

    public SoulBuyerStartupLog() {
        this.console = Bukkit.getConsoleSender();
    }

    public void bannerStart(String version) {
        console.sendMessage(" ");
        console.sendMessage(PREFIX + "==============================");
        console.sendMessage(PREFIX + "Version:" + GRAY + " " + version + " " + RESET + "| Author:" + GRAY + " b0b0b0" + RESET);
        console.sendMessage(PREFIX + " ");
        console.sendMessage(PREFIX + " Инициализация:");
    }

    public void bannerSuccess() {
        console.sendMessage(PREFIX + GREEN + "SoulBuyer успешно загружен" + RESET);
        console.sendMessage(PREFIX + "==============================");
        console.sendMessage(" ");
    }

    public void abort(String message) {
        console.sendMessage(PREFIX + RED + message + RESET);
        console.sendMessage(PREFIX + "==============================");
        console.sendMessage(" ");
    }

    public void bannerFailure(String reason) {
        abort(reason);
    }

    public void info(String message) {
        console.sendMessage(PREFIX + message);
    }

    public void stepOk(String message) {
        console.sendMessage(PREFIX + GREEN + "✓ " + RESET + message);
    }

    public void stepFail(String message) {
        console.sendMessage(PREFIX + RED + "❌ " + RESET + message);
    }

    public void stepWaiting(String message) {
        console.sendMessage(PREFIX + YELLOW + message + RESET);
    }

    public void stepSkipped(String message) {
        console.sendMessage(PREFIX + GRAY + "— " + message + RESET);
    }

    public void unload() {
        console.sendMessage(PREFIX + GRAY + "SoulBuyer выгружен" + RESET);
    }
}
