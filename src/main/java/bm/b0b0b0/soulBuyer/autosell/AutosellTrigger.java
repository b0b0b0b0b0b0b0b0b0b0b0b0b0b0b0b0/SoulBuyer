package bm.b0b0b0.soulBuyer.autosell;

import java.util.List;

public final class AutosellTrigger {

    public static final String PICKUP = "pickup";
    public static final String BUYER = "buyer";
    public static final String CHEST = "chest";

    private static final List<String> CYCLE = List.of(PICKUP, BUYER, CHEST);

    private AutosellTrigger() {
    }

    public static String normalize(String trigger) {
        if (BUYER.equals(trigger)) {
            return BUYER;
        }
        if (CHEST.equals(trigger)) {
            return CHEST;
        }
        return PICKUP;
    }

    public static String next(String trigger) {
        String current = normalize(trigger);
        int index = CYCLE.indexOf(current);
        if (index < 0) {
            return PICKUP;
        }
        return CYCLE.get((index + 1) % CYCLE.size());
    }

    public static boolean isPickup(String trigger) {
        return PICKUP.equals(normalize(trigger));
    }

    public static boolean isBuyer(String trigger) {
        return BUYER.equals(normalize(trigger));
    }

    public static boolean isChest(String trigger) {
        return CHEST.equals(normalize(trigger));
    }
}
