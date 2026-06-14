package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class PlayerPointsEconomyHook {

    public enum ProbeState {
        READY,
        PLUGIN_ABSENT,
        NOT_READY
    }

    private final SoulBuyerDebugLog debug;
    private PlayerPointsAPI api;

    public PlayerPointsEconomyHook(SoulBuyerDebugLog debug) {
        this.debug = debug;
    }

    public ProbeState probe() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            debug.log("playerpoints hook: plugin missing");
            return ProbeState.PLUGIN_ABSENT;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            debug.log("playerpoints hook: plugin not enabled yet");
            return ProbeState.NOT_READY;
        }
        PlayerPoints playerPoints = PlayerPoints.getInstance();
        if (playerPoints == null) {
            debug.log("playerpoints hook: instance null");
            return ProbeState.NOT_READY;
        }
        api = playerPoints.getAPI();
        if (api == null) {
            debug.log("playerpoints hook: API null");
            return ProbeState.NOT_READY;
        }
        debug.log("playerpoints hook OK");
        return ProbeState.READY;
    }

    public boolean hook() {
        return probe() == ProbeState.READY;
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

    public boolean has(OfflinePlayer player, double amount) {
        if (api == null) {
            return false;
        }
        int points = (int) Math.ceil(amount);
        return api.look(player.getUniqueId()) >= points;
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (api == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        int points = (int) Math.ceil(amount);
        if (points <= 0) {
            return amount <= 0.0D;
        }
        boolean success = api.take(player.getUniqueId(), points);
        debug.log("playerpoints withdraw " + player.getName() + " amount=" + points + " success=" + success);
        return success;
    }
}
