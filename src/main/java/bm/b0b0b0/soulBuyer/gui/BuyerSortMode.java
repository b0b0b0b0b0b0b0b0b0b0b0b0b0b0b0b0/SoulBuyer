package bm.b0b0b0.soulBuyer.gui;

import java.util.List;

public final class BuyerSortMode {

    public static final String DEFAULT = "default";
    public static final String PRICE_DESC = "price-desc";
    public static final String PRICE_ASC = "price-asc";
    public static final String POINTS_DESC = "points-desc";
    public static final String POINTS_ASC = "points-asc";
    public static final String INVENTORY_DESC = "inventory";

    private static final List<String> CYCLE = List.of(
            PRICE_DESC,
            PRICE_ASC,
            POINTS_DESC,
            POINTS_ASC
    );

    private BuyerSortMode() {
    }

    public static List<String> cycleModes() {
        return CYCLE;
    }

    public static boolean isCycleMode(String mode) {
        return CYCLE.contains(mode);
    }

    public static String normalize(String mode) {
        if (isCycleMode(mode)) {
            return mode;
        }
        return PRICE_DESC;
    }

    public static String nextInCycle(String current) {
        String normalized = normalize(current);
        int index = CYCLE.indexOf(normalized);
        return CYCLE.get((index + 1) % CYCLE.size());
    }

    public static String labelKey(String mode) {
        return switch (normalize(mode)) {
            case PRICE_ASC -> "gui.buyer.sort-price-asc";
            case POINTS_DESC -> "gui.buyer.sort-points-desc";
            case POINTS_ASC -> "gui.buyer.sort-points-asc";
            default -> "gui.buyer.sort-price-desc";
        };
    }
}
