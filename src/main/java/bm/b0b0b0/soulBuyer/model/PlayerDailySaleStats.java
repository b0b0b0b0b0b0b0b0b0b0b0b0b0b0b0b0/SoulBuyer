package bm.b0b0b0.soulBuyer.model;

public record PlayerDailySaleStats(double money, double points, int stacks) {

    public static PlayerDailySaleStats empty() {
        return new PlayerDailySaleStats(0.0D, 0.0D, 0);
    }

    public PlayerDailySaleStats plus(double addedMoney, double addedPoints, int addedStacks) {
        return new PlayerDailySaleStats(money + addedMoney, points + addedPoints, stacks + addedStacks);
    }
}
