package bm.b0b0b0.soulBuyer.database;

import bm.b0b0b0.soulBuyer.repository.MarketRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerAutosellRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerBoosterRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerProgressRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerSellLimitRepository;
import bm.b0b0b0.soulBuyer.repository.SaleLogRepository;
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
