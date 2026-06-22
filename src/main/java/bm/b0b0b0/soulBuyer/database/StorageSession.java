package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.repository.*;

import java.util.concurrent.Executor;

public record StorageSession(
        PlayerProgressRepository playerProgress,
        PlayerAutosellRepository playerAutosell,
        PlayerBoosterRepository playerBoosters,
        PlayerSellLimitRepository playerSellLimits,
        MarketRepository market,
        SaleLogRepository saleLog,
        Executor executor,
        DataSourceProvider jdbcProvider,
        Runnable shutdown
) {
}
