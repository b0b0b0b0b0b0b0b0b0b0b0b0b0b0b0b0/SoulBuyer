package bm.b0b0b0.soulBuyer.model;

public record SellableItemDefinition(
        String id,
        String material,
        String categoryId,
        double basePrice,
        double basePoints,
        int customModelData
) {
    public boolean usesCustomModelData() {
        return customModelData >= 0;
    }
}
