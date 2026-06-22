package bm.b0b0b0.soulBuyer.debug;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SoulBuyerDebugLog {

    private final JavaPlugin plugin;
    private volatile boolean enabled = false;

    public SoulBuyerDebugLog(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled;
    }

    public void boot(String message) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info(bootLine(message));
    }

    public void boot(String message, Throwable throwable) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().log(Level.INFO, bootLine(message), throwable);
    }

    public void log(String message) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info(line(message));
    }

    public void log(String message, Object detail) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info(line(message + " | " + detail));
    }

    public void warn(String message) {
        plugin.getLogger().warning(message);
    }

    public void error(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }

    private String bootLine(String message) {
        return "[SoulBuyer|BOOT] " + message + threadSuffix();
    }

    private String line(String message) {
        return "[SoulBuyer|DEBUG] " + message + threadSuffix();
    }

    private String threadSuffix() {
        return " [" + Thread.currentThread().getName() + "]";
    }
}
