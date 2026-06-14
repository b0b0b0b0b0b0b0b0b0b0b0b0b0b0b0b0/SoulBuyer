package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerPointsEconomyHook {

    private final SoulBuyerDebugLog debug;
    private PlayerPointsAPI api;

    public PlayerPointsEconomyHook(SoulBuyerDebugLog debug) {
        this.debug = debug;
    }

    public boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            debug.log("playerpoints hook: plugin missing");
            return false;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            debug.log("playerpoints hook: plugin not enabled yet");
            return false;
        }
        PlayerPoints playerPoints = PlayerPoints.getInstance();
        if (playerPoints == null) {
            debug.log("playerpoints hook: instance null");
            return false;
        }
        api = playerPoints.getAPI();
        if (api == null) {
            debug.log("playerpoints hook: API null");
            return false;
        }
        debug.log("playerpoints hook OK");
        return true;
    }

    public boolean available() {
        return api != null;
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (api == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        int points = (int) Math.round(amount);
        if (points <= 0) {
            return amount <= 0.0D;
        }
        boolean success = api.give(player.getUniqueId(), points);
        debug.log("playerpoints deposit " + player.getName() + " amount=" + points + " success=" + success);
        return success;
    }
}
