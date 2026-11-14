package bm.b0b0b0.soulBuyer.listener;

import bm.b0b0b0.soulBuyer.service.SellService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerDataWarmupListener implements Listener {

    private final SellService sellService;

    public PlayerDataWarmupListener(SellService sellService) {
        this.sellService = sellService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sellService.preloadProgress(event.getPlayer());
        sellService.preloadSellLimitUsage(event.getPlayer());
    }
}
