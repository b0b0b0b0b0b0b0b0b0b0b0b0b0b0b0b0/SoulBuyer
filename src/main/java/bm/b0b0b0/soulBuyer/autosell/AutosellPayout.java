package bm.b0b0b0.soulBuyer.autosell;

import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;

import java.util.List;

public final class AutosellPayout {

    public static final String VAULT = "vault";
    public static final String PLAYER_POINTS = "player-points";

    private static final List<String> CYCLE = List.of(VAULT, PLAYER_POINTS);

    private AutosellPayout() {
    }

    public static String normalize(String payout) {
        if (PLAYER_POINTS.equals(payout) || "playerpoints".equals(payout) || "player_points".equals(payout)) {
            return PLAYER_POINTS;
        }
        return VAULT;
    }

    public static String next(String payout) {
        String current = normalize(payout);
        int index = CYCLE.indexOf(current);
        if (index < 0) {
            return VAULT;
        }
        return CYCLE.get((index + 1) % CYCLE.size());
    }

    public static boolean isVault(String payout) {
        return VAULT.equals(normalize(payout));
    }

    public static boolean isPlayerPoints(String payout) {
        return PLAYER_POINTS.equals(normalize(payout));
    }

    public static BuyerPayoutMode toMode(String payout) {
        return isPlayerPoints(payout) ? BuyerPayoutMode.PLAYER_POINTS : BuyerPayoutMode.VAULT;
    }
}
