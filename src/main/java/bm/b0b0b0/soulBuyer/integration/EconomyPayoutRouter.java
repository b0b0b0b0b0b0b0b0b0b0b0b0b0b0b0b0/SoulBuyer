package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;
import org.bukkit.entity.Player;

public final class EconomyPayoutRouter {

    private final VaultEconomyHook vaultEconomyHook;
    private final PlayerPointsEconomyHook playerPointsEconomyHook;

    public EconomyPayoutRouter(
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook
    ) {
        this.vaultEconomyHook = vaultEconomyHook;
        this.playerPointsEconomyHook = playerPointsEconomyHook;
    }

    public boolean available(BuyerPayoutMode mode) {
        return switch (mode) {
            case VAULT -> vaultEconomyHook.available();
            case PLAYER_POINTS -> playerPointsEconomyHook.available();
        };
    }

    public boolean deposit(Player player, BuyerPayoutMode mode, double amount) {
        return switch (mode) {
            case VAULT -> vaultEconomyHook.deposit(player, amount);
            case PLAYER_POINTS -> playerPointsEconomyHook.deposit(player, amount);
        };
    }
}
