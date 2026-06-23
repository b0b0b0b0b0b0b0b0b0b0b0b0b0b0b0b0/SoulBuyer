package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.gui.BuyerMenuSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BuyerSubMenuNavigation {

    private BuyerSubMenuNavigation() {
    }

    public static void onUnexpectedClose(
            JavaPlugin plugin,
            BuyerMenuNavigation navigation,
            Player player,
            BuyerMenuSession session,
            boolean closingIntentionally
    ) {
        if (closingIntentionally || !player.isOnline()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> navigation.openBuyer(player, session));
    }

    public static void goBack(
            BuyerMenuNavigation navigation,
            Player player,
            BuyerMenuSession session,
            Runnable markIntentional
    ) {
        markIntentional.run();
        navigation.openBuyer(player, session);
    }
}
