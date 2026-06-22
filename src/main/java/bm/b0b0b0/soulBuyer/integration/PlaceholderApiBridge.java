package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PlaceholderApiBridge {

    private static final String EXPANSION_CLASS =
            "bm.b0b0b0.soulBuyer.integration.SoulBuyerPlaceholderExpansion";

    private final JavaPlugin plugin;
    private volatile boolean registered;

    public PlaceholderApiBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean available() {
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public boolean registerExpansion(BuyerStatsService buyerStatsService) {
        if (!available() || registered) {
            return false;
        }
        try {
            Class<?> expansionClass = Class.forName(EXPANSION_CLASS, true, plugin.getClass().getClassLoader());
            Object expansion = expansionClass
                    .getConstructor(JavaPlugin.class, BuyerStatsService.class)
                    .newInstance(plugin, buyerStatsService);
            Object result = expansionClass.getMethod("register").invoke(expansion);
            if (result instanceof Boolean success && success) {
                registered = true;
                return true;
            }
        } catch (ClassNotFoundException | NoClassDefFoundError exception) {
            plugin.getLogger().log(Level.WARNING, "PlaceholderAPI classes missing, expansion skipped");
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "PlaceholderAPI expansion registration failed", exception);
        }
        return false;
    }

    public void unregister() {
        registered = false;
    }

    public String apply(org.bukkit.entity.Player player, String input) {
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
