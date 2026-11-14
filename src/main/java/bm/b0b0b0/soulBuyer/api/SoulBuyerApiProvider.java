package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.SoulBuyer;
import org.bukkit.Bukkit;

public final class SoulBuyerApiProvider {

    private SoulBuyerApiProvider() {
    }

    public static SoulBuyerApi get() {
        if (Bukkit.getPluginManager().getPlugin("SoulBuyer") instanceof SoulBuyer soulBuyer) {
            SoulBuyerApi api = soulBuyer.api();
            if (api != null) {
                return api;
            }
        }
        return UnavailableSoulBuyerApi.INSTANCE;
    }
}
