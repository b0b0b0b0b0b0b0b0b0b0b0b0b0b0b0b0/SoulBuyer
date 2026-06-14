package bm.b0b0b0.soulBuyer.gui;

import org.bukkit.entity.Player;

public interface BuyerMenuNavigation {

    void openBuyer(Player player, BuyerMenuSession session);

    void openQuantity(Player player, String itemId, BuyerMenuSession session);

    void openAutosell(Player player, BuyerMenuSession session);
}
