package bm.b0b0b0.soulBuyer.repository;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.model.PlayerDailySaleStats;
import bm.b0b0b0.soulBuyer.model.SellLine;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class YamlSaleLogRepository implements SaleLogRepository {

    private final Path salesLogPath;
    private final Executor executor;
    private final List<PendingSale> pending = new ArrayList<>();

    public YamlSaleLogRepository(JavaPlugin plugin, PluginConfig config, Executor executor) {
        this.salesLogPath = plugin.getDataFolder().toPath().resolve(config.storage().salesLogFile);
        this.executor = executor;
    }

    @Override
    public synchronized void enqueue(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
        pending.add(new PendingSale(playerId, serverId, List.copyOf(lines), money, points));
    }

    @Override
    public synchronized void flushPending() {
        drainPending();
    }

    @Override
    public synchronized CompletableFuture<Void> drainPending() {
        if (pending.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<PendingSale> batch = new ArrayList<>(pending);
        pending.clear();
        return CompletableFuture.runAsync(() -> appendBatch(batch), executor);
    }

    @Override
    public CompletableFuture<Void> append(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
        return CompletableFuture.runAsync(
                () -> appendBatch(List.of(new PendingSale(playerId, serverId, lines, money, points))),
                executor
        );
    }

    @Override
    public CompletableFuture<PlayerDailySaleStats> loadDailyStats(UUID playerId, long sinceEpochMs) {
        return CompletableFuture.supplyAsync(() -> readDailyStats(playerId, sinceEpochMs), executor);
    }

    private PlayerDailySaleStats readDailyStats(UUID playerId, long sinceEpochMs) {
        if (!Files.exists(salesLogPath)) {
            return PlayerDailySaleStats.empty();
        }
        double money = 0.0D;
        double points = 0.0D;
        int stacks = 0;
        String playerKey = playerId.toString();
        try {
            for (String line : Files.readAllLines(salesLogPath)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 7) {
                    continue;
                }
                long timestamp = Long.parseLong(parts[0]);
                if (timestamp < sinceEpochMs) {
                    continue;
                }
                if (!playerKey.equals(parts[1])) {
                    continue;
                }
                money += Double.parseDouble(parts[5]);
                points += Double.parseDouble(parts[6]);
                stacks++;
            }
        } catch (IOException | NumberFormatException exception) {
            return PlayerDailySaleStats.empty();
        }
        return new PlayerDailySaleStats(money, points, stacks);
    }

    private void appendBatch(List<PendingSale> batch) {
        try {
            if (salesLogPath.getParent() != null) {
                Files.createDirectories(salesLogPath.getParent());
            }
            StringBuilder builder = new StringBuilder();
            long timestamp = System.currentTimeMillis();
            for (PendingSale sale : batch) {
                for (SellLine line : sale.lines()) {
                    builder.append(timestamp).append('|')
                            .append(sale.playerId()).append('|')
                            .append(sale.serverId()).append('|')
                            .append(line.itemId()).append('|')
                            .append(line.amount()).append('|')
                            .append(format(line.totalMoney())).append('|')
                            .append(format(line.totalPoints()))
                            .append(System.lineSeparator());
                }
            }
            Files.writeString(
                    salesLogPath,
                    builder.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to append sales log", exception);
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private record PendingSale(UUID playerId, String serverId, List<SellLine> lines, double money, double points) {
    }
}
