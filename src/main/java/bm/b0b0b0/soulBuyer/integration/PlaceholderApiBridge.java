package bm.b0b0b0.soulBuyer.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaceholderApiBridge {

    private final JavaPlugin plugin;
    private volatile boolean registered;

    public PlaceholderApiBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean available() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public void register(SoulBuyerPlaceholderExpansion expansion) {
        if (!available() || registered) {
            return;
        }
        if (expansion.register()) {
            registered = true;
        }
    }

    public void unregister() {
        if (!registered) {
            return;
        }
        registered = false;
    }

    public String apply(Player player, String input) {
        if (player == null || input == null || input.isEmpty() || !available()) {
            return input;
        }
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, input);
        } catch (Throwable throwable) {
            return input;
        }
    }
}
