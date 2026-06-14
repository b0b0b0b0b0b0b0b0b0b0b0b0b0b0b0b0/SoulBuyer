package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultEconomyHook {

    private final JavaPlugin plugin;
    private final SoulBuyerDebugLog debug;
    private Economy economy;

    public VaultEconomyHook(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        this.plugin = plugin;
        this.debug = debug;
    }

    public boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            debug.log("vault hook: Vault plugin missing");
            return false;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            debug.log("vault hook: Economy service not registered yet");
            return false;
        }
        economy = provider.getProvider();
        if (economy == null) {
            debug.log("vault hook: Economy provider null");
            return false;
        }
        debug.log("vault hook OK: " + economy.getName());
        return true;
    }

    public boolean available() {
        return economy != null;
    }

    public boolean deposit(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        boolean success = economy.depositPlayer(player, amount).transactionSuccess();
        debug.log("vault deposit " + player.getName() + " amount=" + amount + " success=" + success);
        return success;
    }
}
