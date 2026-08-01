package bm.b0b0b0.soulBuyer.bootstrap;

import bm.b0b0b0.soulBuyer.SoulBuyer;
import org.bstats.bukkit.Metrics;

public final class SoulBuyerMetrics {

    private static final int PLUGIN_ID = 33055;

    private SoulBuyerMetrics() {
    }

    public static void tryStart(SoulBuyer plugin, boolean enabled) {
        if (!enabled) {
            return;
        }
        new Metrics(plugin, PLUGIN_ID);
    }
}
