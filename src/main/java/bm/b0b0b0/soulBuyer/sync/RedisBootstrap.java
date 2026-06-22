package bm.b0b0b0.soulBuyer.sync;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.io.SoulBuyerIoExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;
import java.util.logging.Level;

public final class RedisBootstrap {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final SoulBuyerDebugLog debug;
    private JedisPool pool;
    private Thread subscriberThread;
    private JedisPubSub pubSub;

    public RedisBootstrap(JavaPlugin plugin, PluginConfig config, SoulBuyerDebugLog debug) {
        this.plugin = plugin;
        this.config = config;
        this.debug = debug;
    }

    public void connect() {
        if (config.singleServer() || !config.redis().enabled) {
            debug.boot("redis skipped (single-server=" + config.singleServer()
                    + ", enabled=" + config.redis().enabled + ")");
            return;
        }
        debug.boot("redis connecting " + config.redis().host + ":" + config.redis().port
                + " db=" + config.redis().database);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        String password = config.redis().password;
        if (password == null || password.isEmpty()) {
            pool = new JedisPool(poolConfig, config.redis().host, config.redis().port, 2000, null, config.redis().database);
        } else {
            pool = new JedisPool(poolConfig, config.redis().host, config.redis().port, 2000, password, config.redis().database);
        }
        try (Jedis jedis = pool.getResource()) {
            String pong = jedis.ping();
            debug.boot("redis ping OK: " + pong);
        } catch (Exception exception) {
            debug.error("redis connection failed", exception);
            shutdown();
            throw new IllegalStateException("Redis connection failed", exception);
        }
    }

    public void startSubscriber(Consumer<String> marketUpdateListener) {
        if (pool == null) {
            debug.log("redis subscriber skipped (no pool)");
            return;
        }
        debug.boot("redis subscriber starting on channel=" + config.redis().marketChannel);
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (config.redis().marketChannel.equals(channel)) {
                    debug.log("redis market message: " + message);
                    marketUpdateListener.accept(message);
                }
            }
        };
        subscriberThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.subscribe(pubSub, config.redis().marketChannel);
                } catch (Exception exception) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    plugin.getLogger().log(Level.WARNING, "Redis subscriber disconnected, retrying...", exception);
                    debug.warn("redis subscriber disconnected, retry in 2s");
                    sleepQuietly(2000L);
                }
            }
        }, "SoulBuyer-Redis-Sub");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    public JedisPool pool() {
        return pool;
    }

    public boolean connected() {
        return pool != null;
    }

    public void publishMarketUpdate(String payload) {
        if (pool == null) {
            return;
        }
        SoulBuyerIoExecutor.executor().execute(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.publish(config.redis().marketChannel, payload);
                debug.log("redis published: " + payload);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Redis publish failed", exception);
                debug.warn("redis publish failed: " + exception.getMessage());
            }
        });
    }

    public void shutdown() {
        debug.log("redis shutdown");
        if (pubSub != null) {
            try {
                pubSub.unsubscribe();
            } catch (Exception ignored) {
            }
        }
        if (subscriberThread != null) {
            subscriberThread.interrupt();
        }
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
