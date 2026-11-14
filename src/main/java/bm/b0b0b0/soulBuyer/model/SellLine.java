package bm.b0b0b0.soulBuyer.model;

public record SellLine(String itemId, int amount, double unitPrice, double unitPoints) {

    public double totalMoney() {
        return unitPrice * amount;
    }

    public double totalPoints() {
        return unitPoints * amount;
    }
}
