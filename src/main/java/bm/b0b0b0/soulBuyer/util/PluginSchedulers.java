package bm.b0b0b0.soulBuyer.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class PluginSchedulers {

    private static final BooleanSupplier GLOBAL_TICK_THREAD = resolveGlobalTickThreadCheck();

    private PluginSchedulers() {
    }

    public static void runGlobal(Plugin plugin, Runnable task) {
        if (GLOBAL_TICK_THREAD.getAsBoolean()) {
            task.run();
            return;
        }
        plugin.getServer().getGlobalRegionScheduler().run(plugin, ignored -> task.run());
    }

    public static ScheduledTask runGlobalLater(Plugin plugin, Runnable task, long delayTicks) {
        return plugin.getServer().getGlobalRegionScheduler().runDelayed(
                plugin,
                ignored -> task.run(),
                Math.max(1L, delayTicks)
        );
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, ignored -> task.run());
    }

    public static ScheduledTask runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        return plugin.getServer().getAsyncScheduler().runDelayed(
                plugin,
                ignored -> task.run(),
                Math.max(1L, delayTicks) * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    public static ScheduledTask runAsyncTimer(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        return plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin,
                ignored -> task.run(),
                Math.max(1L, initialDelayTicks) * 50L,
                Math.max(1L, periodTicks) * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    public static void run(Plugin plugin, Entity entity, Runnable task) {
        if (entity == null) {
            runGlobal(plugin, task);
            return;
        }
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            task.run();
            return;
        }
        entity.getScheduler().run(plugin, ignored -> task.run(), null);
    }

    public static ScheduledTask runLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        return entity.getScheduler().runDelayed(
                plugin,
                ignored -> task.run(),
                null,
                Math.max(1L, delayTicks)
        );
    }

    public static ScheduledTask runTimer(
            Plugin plugin,
            Entity entity,
            Runnable task,
            long initialDelayTicks,
            long periodTicks
    ) {
        return entity.getScheduler().runAtFixedRate(
                plugin,
                ignored -> task.run(),
                null,
                Math.max(1L, initialDelayTicks),
                Math.max(1L, periodTicks)
        );
    }

    public static void runAt(Plugin plugin, Location location, Runnable task) {
        if (location == null || location.getWorld() == null) {
            runGlobal(plugin, task);
            return;
        }
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            task.run();
            return;
        }
        plugin.getServer().getRegionScheduler().run(plugin, location, ignored -> task.run());
    }

    private static BooleanSupplier resolveGlobalTickThreadCheck() {
        try {
            Method method = Bukkit.class.getMethod("isGlobalTickThread");
            return () -> {
                try {
                    return Boolean.TRUE.equals(method.invoke(null));
                } catch (ReflectiveOperationException ignored) {
                    return false;
                }
            };
        } catch (NoSuchMethodException ignored) {
            return Bukkit::isPrimaryThread;
        }
    }
}
