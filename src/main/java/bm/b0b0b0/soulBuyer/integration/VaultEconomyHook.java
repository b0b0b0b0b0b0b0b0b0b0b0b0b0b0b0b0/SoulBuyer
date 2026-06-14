package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultEconomyHook {

    public enum ProbeState {
        READY,
        VAULT_ABSENT,
        ECONOMY_ABSENT
    }

    private final JavaPlugin plugin;
    private final SoulBuyerDebugLog debug;
    private Economy economy;

    public VaultEconomyHook(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        this.plugin = plugin;
        this.debug = debug;
    }

    public ProbeState probe() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            debug.log("vault hook: Vault plugin missing");
            return ProbeState.VAULT_ABSENT;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            debug.log("vault hook: Economy service not registered yet");
            return ProbeState.ECONOMY_ABSENT;
        }
        economy = provider.getProvider();
        if (economy == null) {
            debug.log("vault hook: Economy provider null");
            return ProbeState.ECONOMY_ABSENT;
        }
        debug.log("vault hook OK: " + economy.getName());
        return ProbeState.READY;
    }

    public boolean hook() {
        return probe() == ProbeState.READY;
    }

    public boolean available() {
        return economy != null;
    }

    public String providerName() {
        return economy == null ? "" : economy.getName();
    }

    public boolean deposit(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        boolean success = economy.depositPlayer(player, amount).transactionSuccess();
        debug.log("vault deposit " + player.getName() + " amount=" + amount + " success=" + success);
        return success;
    }

    public boolean has(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null) {
            return false;
        }
        return economy.has(player, amount);
    }

    public boolean withdraw(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        boolean success = economy.withdrawPlayer(player, amount).transactionSuccess();
        debug.log("vault withdraw " + player.getName() + " amount=" + amount + " success=" + success);
        return success;
    }
}
