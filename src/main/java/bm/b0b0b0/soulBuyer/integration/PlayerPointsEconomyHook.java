package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public final class PlayerPointsEconomyHook {

    public enum ProbeState {
        READY,
        PLUGIN_ABSENT,
        NOT_READY
    }

    private final SoulBuyerDebugLog debug;
    private Object api;
    private Method give;
    private Method take;
    private Method look;

    public PlayerPointsEconomyHook(SoulBuyerDebugLog debug) {
        this.debug = debug;
    }

    public ProbeState probe() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (plugin == null) {
            debug.log("playerpoints hook: plugin missing");
            return ProbeState.PLUGIN_ABSENT;
        }
        if (!plugin.isEnabled()) {
            debug.log("playerpoints hook: plugin not enabled yet");
            return ProbeState.NOT_READY;
        }
        try {
            Class<?> playerPointsClass = Class.forName(
                    "org.black_ixx.playerpoints.PlayerPoints",
                    true,
                    plugin.getClass().getClassLoader()
            );
            Method getInstance = playerPointsClass.getMethod("getInstance");
            Object playerPoints = getInstance.invoke(null);
            if (playerPoints == null) {
                debug.log("playerpoints hook: instance null");
                return ProbeState.NOT_READY;
            }
            Method getApi = playerPointsClass.getMethod("getAPI");
            Object resolvedApi = getApi.invoke(playerPoints);
            if (resolvedApi == null) {
                debug.log("playerpoints hook: API null");
                return ProbeState.NOT_READY;
            }
            if (!bind(resolvedApi)) {
                debug.log("playerpoints hook: API methods missing");
                return ProbeState.NOT_READY;
            }
            debug.log("playerpoints hook OK");
            return ProbeState.READY;
        } catch (ClassNotFoundException | NoClassDefFoundError error) {
            debug.warn("playerpoints hook: API unavailable");
            return ProbeState.PLUGIN_ABSENT;
        } catch (ReflectiveOperationException exception) {
            debug.warn("playerpoints hook failed: " + exception.getMessage());
            return ProbeState.NOT_READY;
        }
    }

    public boolean hook() {
        return probe() == ProbeState.READY;
    }

    public boolean available() {
        return api != null;
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (api == null || give == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        int points = (int) Math.round(amount);
        if (points <= 0) {
            return amount <= 0.0D;
        }
        try {
            boolean success = Boolean.TRUE.equals(give.invoke(api, player.getUniqueId(), points));
            debug.log("playerpoints deposit " + player.getName() + " amount=" + points + " success=" + success);
            return success;
        } catch (ReflectiveOperationException exception) {
            debug.warn("playerpoints deposit failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (api == null || look == null) {
            return false;
        }
        int points = (int) Math.ceil(amount);
        try {
            Object balance = look.invoke(api, player.getUniqueId());
            return balance instanceof Number number && number.intValue() >= points;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (api == null || take == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        int points = (int) Math.ceil(amount);
        if (points <= 0) {
            return amount <= 0.0D;
        }
        try {
            boolean success = Boolean.TRUE.equals(take.invoke(api, player.getUniqueId(), points));
            debug.log("playerpoints withdraw " + player.getName() + " amount=" + points + " success=" + success);
            return success;
        } catch (ReflectiveOperationException exception) {
            debug.warn("playerpoints withdraw failed: " + exception.getMessage());
            return false;
        }
    }

    private boolean bind(Object resolvedApi) throws NoSuchMethodException {
        Class<?> apiClass = resolvedApi.getClass();
        give = apiClass.getMethod("give", UUID.class, int.class);
        take = apiClass.getMethod("take", UUID.class, int.class);
        look = apiClass.getMethod("look", UUID.class);
        api = resolvedApi;
        return true;
    }
}
