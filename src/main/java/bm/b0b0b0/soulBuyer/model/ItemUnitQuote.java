package bm.b0b0b0.soulBuyer.model;

public record ItemUnitQuote(
        double unitPrice,
        double unitPoints,
        double marketCoefficient,
        double playerMultiplier,
        int inventoryAmount
) {
}
