package bm.b0b0b0.soulBuyer.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class EconomyPluginPresence {

    private static final String[] KNOWN_ECONOMY_PLUGINS = {
            "Essentials",
            "EssentialsX",
            "EssentialsEco",
            "CMI",
            "TheNewEconomy",
            "EcoBits",
            "RoyaleEconomy",
            "XConomy",
            "UltimateEconomy",
            "BOSEconomy"
    };

    private EconomyPluginPresence() {
    }

    public static boolean isKnownEconomyPlugin(String pluginName) {
        if (pluginName == null || pluginName.isEmpty()) {
            return false;
        }
        for (String known : KNOWN_ECONOMY_PLUGINS) {
            if (known.equalsIgnoreCase(pluginName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean vaultInstalled() {
        return Bukkit.getPluginManager().getPlugin("Vault") != null;
    }

    public static boolean playerPointsInstalled() {
        return Bukkit.getPluginManager().getPlugin("PlayerPoints") != null;
    }

    public static boolean anyKnownEconomyPluginInstalled() {
        return !findInstalledKnownEconomyPlugins().isEmpty();
    }

    public static List<String> findInstalledKnownEconomyPlugins() {
        List<String> found = new ArrayList<>();
        for (String name : KNOWN_ECONOMY_PLUGINS) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin != null) {
                found.add(plugin.getName());
            }
        }
        return found;
    }
}
