package bm.b0b0b0.soulBuyer.booster;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import bm.b0b0b0.soulBuyer.model.ActiveBooster;
import bm.b0b0b0.soulBuyer.model.BoosterType;
import bm.b0b0b0.soulBuyer.model.GlobalBoosterState;
import bm.b0b0b0.soulBuyer.repository.GlobalBoosterRepository;
import bm.b0b0b0.soulBuyer.sync.RedisBootstrap;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class GlobalBoosterService {

    private final PluginConfig config;
    private final GlobalBoosterRepository repository;
    private final RedisBootstrap redisBootstrap;
    private final SoulBuyerDebugLog debug;
    private final AtomicReference<GlobalBoosterState> state = new AtomicReference<>(GlobalBoosterState.empty());

    public GlobalBoosterService(
            PluginConfig config,
            GlobalBoosterRepository repository,
            RedisBootstrap redisBootstrap,
            SoulBuyerDebugLog debug
    ) {
        this.config = config;
        this.repository = repository;
        this.redisBootstrap = redisBootstrap;
        this.debug = debug;
    }

    public CompletableFuture<Void> start() {
        return repository.load().thenAccept(loaded -> {
            state.set(prune(loaded));
            debug.boot("global boosters loaded, active=" + state.get().active().size());
        });
    }

    public boolean featureEnabled() {
        return config.boosters().enabled && config.boosters().globalEnabled;
    }

    public GlobalBoosterState snapshot() {
        return prune(state.get());
    }

    public ActiveBooster active(BoosterType type) {
        if (!featureEnabled()) {
            return null;
        }
        ActiveBooster booster = snapshot().active().get(type);
        if (booster == null || !booster.active(System.currentTimeMillis())) {
            return null;
        }
        return booster;
    }

    public double additiveMultiplier() {
        ActiveBooster booster = active(BoosterType.MULTIPLIER);
        return booster == null ? 0.0D : booster.effect();
    }

    public double moneyMultiplier() {
        ActiveBooster booster = active(BoosterType.MONEY);
        return booster == null ? 1.0D : booster.effect();
    }

    public double limitMultiplier() {
        ActiveBooster booster = active(BoosterType.LIMIT);
        return booster == null ? 1.0D : booster.effect();
    }

    public long remainingMillis(BoosterType type) {
        ActiveBooster booster = active(type);
        if (booster == null) {
            return 0L;
        }
        return Math.max(0L, booster.expiresAtMillis() - System.currentTimeMillis());
    }

    public CompletableFuture<Boolean> activate(String offerId, Long durationSecondsOverride) {
        if (!featureEnabled()) {
            return CompletableFuture.completedFuture(false);
        }
        SoulBuyerSettings.BoosterOfferSettings offer = config.boosters().offers.get(offerId);
        if (offer == null) {
            return CompletableFuture.completedFuture(false);
        }
        BoosterType type = BoosterType.parse(offer.type);
        long durationSeconds = durationSecondsOverride == null
                ? Math.max(1L, offer.durationSeconds)
                : Math.max(1L, durationSecondsOverride);
        long expiresAt = System.currentTimeMillis() + durationSeconds * 1000L;
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(snapshot().active());
        active.put(type, new ActiveBooster(type, offer.effect, expiresAt));
        return persist(new GlobalBoosterState(active)).thenApply(ignored -> true);
    }

    public CompletableFuture<Boolean> clear(BoosterType type) {
        if (!featureEnabled()) {
            return CompletableFuture.completedFuture(false);
        }
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(snapshot().active());
        if (active.remove(type) == null) {
            return CompletableFuture.completedFuture(false);
        }
        return persist(new GlobalBoosterState(active)).thenApply(ignored -> true);
    }

    public CompletableFuture<Void> clearAll() {
        return persist(GlobalBoosterState.empty());
    }

    public void applyRemoteUpdate(String payload) {
        try {
            GlobalBoosterState remote = BoosterStateCodec.decodeGlobal(payload);
            state.set(prune(remote));
            debug.log("global boosters remote update applied, active=" + state.get().active().size());
        } catch (Exception exception) {
            debug.warn("global boosters remote update failed: " + exception.getMessage());
        }
    }

    private CompletableFuture<Void> persist(GlobalBoosterState next) {
        GlobalBoosterState pruned = prune(next);
        state.set(pruned);
        return repository.save(pruned).thenRun(() ->
                redisBootstrap.publishGlobalBoostersUpdate(BoosterStateCodec.encode(pruned))
        );
    }

    private GlobalBoosterState prune(GlobalBoosterState current) {
        long now = System.currentTimeMillis();
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(BoosterType.class);
        for (Map.Entry<BoosterType, ActiveBooster> entry : current.active().entrySet()) {
            if (entry.getValue().active(now)) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return new GlobalBoosterState(active);
    }
}
