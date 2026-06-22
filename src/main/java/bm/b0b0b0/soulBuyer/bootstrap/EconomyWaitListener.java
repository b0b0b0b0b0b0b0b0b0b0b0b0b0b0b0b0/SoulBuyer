package bm.b0b0b0.soulBuyer.bootstrap;

import bm.b0b0b0.soulBuyer.SoulBuyer;
import bm.b0b0b0.soulBuyer.integration.EconomyPluginPresence;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;

public final class EconomyWaitListener implements Listener {

    private final SoulBuyer plugin;

    public EconomyWaitListener(SoulBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService() != Economy.class) {
            return;
        }
        plugin.retryEconomyActivation();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        String name = event.getPlugin().getName();
        if ("Vault".equalsIgnoreCase(name)
                || "PlayerPoints".equalsIgnoreCase(name)
                || EconomyPluginPresence.isKnownEconomyPlugin(name)) {
            plugin.retryEconomyActivation();
        }
    }
}
