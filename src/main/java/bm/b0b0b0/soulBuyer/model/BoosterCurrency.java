package bm.b0b0b0.soulBuyer.model;

public enum BoosterCurrency {
    PROGRESSION_POINTS,
    VAULT,
    PLAYER_POINTS;

    public static BoosterCurrency parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return PROGRESSION_POINTS;
        }
        return switch (raw.toLowerCase().replace('-', '_')) {
            case "vault" -> VAULT;
            case "playerpoints", "player_points" -> PLAYER_POINTS;
            default -> PROGRESSION_POINTS;
        };
    }
}
