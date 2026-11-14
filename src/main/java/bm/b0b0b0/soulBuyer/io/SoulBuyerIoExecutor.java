package bm.b0b0b0.soulBuyer.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SoulBuyerIoExecutor {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "SoulBuyer-IO");
        thread.setDaemon(true);
        return thread;
    });

    private SoulBuyerIoExecutor() {
    }

    public static ExecutorService executor() {
        return EXECUTOR;
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            EXECUTOR.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            EXECUTOR.shutdownNow();
        }
    }
}
