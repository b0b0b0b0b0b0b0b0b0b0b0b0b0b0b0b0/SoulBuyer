package bm.b0b0b0.soulBuyer.model;

public enum BoosterType {
    MULTIPLIER,
    MONEY,
    LIMIT;

    public static BoosterType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return MULTIPLIER;
        }
        return switch (raw.toLowerCase()) {
            case "money" -> MONEY;
            case "limit" -> LIMIT;
            default -> MULTIPLIER;
        };
    }
}
