package bm.b0b0b0.soulBuyer.model;

public record ActiveBooster(BoosterType type, double effect, long expiresAtMillis) {

    public boolean active(long nowMillis) {
        return expiresAtMillis > nowMillis;
    }
}
