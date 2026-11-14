package bm.b0b0b0.soulBuyer.limit;

import java.time.LocalDate;
import java.time.ZoneOffset;

public final class SellLimitPeriod {

    private SellLimitPeriod() {
    }

    public static String dailyKey() {
        return LocalDate.now(ZoneOffset.UTC).toString();
    }
}
