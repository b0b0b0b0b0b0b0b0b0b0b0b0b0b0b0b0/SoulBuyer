package bm.b0b0b0.soulBuyer.model;

import java.util.List;

public record SellQuote(
        List<SellLine> lines,
        double totalMoney,
        double totalPoints,
        double marketCoefficient,
        double playerMultiplier
) {
}
