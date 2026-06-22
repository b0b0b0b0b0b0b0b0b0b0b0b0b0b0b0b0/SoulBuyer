package bm.b0b0b0.soulBuyer.booster;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.integration.PlayerPointsEconomyHook;
import bm.b0b0b0.soulBuyer.integration.VaultEconomyHook;
import bm.b0b0b0.soulBuyer.message.MessageService;
import bm.b0b0b0.soulBuyer.model.ActiveBooster;
import bm.b0b0b0.soulBuyer.model.BoosterCurrency;
import bm.b0b0b0.soulBuyer.model.BoosterType;
import bm.b0b0b0.soulBuyer.model.PlayerBoosterState;
import bm.b0b0b0.soulBuyer.repository.PlayerBoosterRepository;
import bm.b0b0b0.soulBuyer.repository.PlayerProgressRepository;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoosterService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;
    private final PlayerBoosterRepository boosterRepository;
    private final PlayerProgressRepository progressRepository;
    private final VaultEconomyHook vaultEconomyHook;
    private final PlayerPointsEconomyHook playerPointsEconomyHook;
    private final java.util.function.Consumer<java.util.UUID> progressCacheRefresher;
    private final Map<UUID, PlayerBoosterState> cache = new ConcurrentHashMap<>();

    public BoosterService(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService,
            PlayerBoosterRepository boosterRepository,
            PlayerProgressRepository progressRepository,
            VaultEconomyHook vaultEconomyHook,
            PlayerPointsEconomyHook playerPointsEconomyHook,
            java.util.function.Consumer<java.util.UUID> progressCacheRefresher
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
        this.boosterRepository = boosterRepository;
        this.progressRepository = progressRepository;
        this.vaultEconomyHook = vaultEconomyHook;
        this.playerPointsEconomyHook = playerPointsEconomyHook;
        this.progressCacheRefresher = progressCacheRefresher;
    }

    public boolean featureEnabled() {
        return config.boosters().enabled;
    }

    public boolean canAccess(Player player) {
        return player.hasPermission(config.permissionBoosters());
    }

    public BoosterCurrency currency() {
        return BoosterCurrency.parse(config.boosters().currency);
    }

    public SoulBuyerSettings.BoostersSettings settings() {
        return config.boosters();
    }

    public void preload(Player player) {
        boosterRepository.find(player.getUniqueId()).thenAccept(state -> cache.put(player.getUniqueId(), prune(state)));
    }

    public void unload(Player player) {
        cache.remove(player.getUniqueId());
    }

    public double additiveMultiplier(Player player) {
        ActiveBooster booster = active(player, BoosterType.MULTIPLIER);
        return booster == null ? 0.0D : booster.effect();
    }

    public double moneyMultiplier(Player player) {
        ActiveBooster booster = active(player, BoosterType.MONEY);
        return booster == null ? 1.0D : booster.effect();
    }

    public double limitMultiplier(Player player) {
        ActiveBooster booster = active(player, BoosterType.LIMIT);
        return booster == null ? 1.0D : booster.effect();
    }

    public ActiveBooster active(Player player, BoosterType type) {
        PlayerBoosterState state = cached(player.getUniqueId());
        ActiveBooster booster = state.active().get(type);
        if (booster == null || !booster.active(System.currentTimeMillis())) {
            return null;
        }
        return booster;
    }

    public long remainingMillis(Player player, BoosterType type) {
        ActiveBooster booster = active(player, type);
        if (booster == null) {
            return 0L;
        }
        return Math.max(0L, booster.expiresAtMillis() - System.currentTimeMillis());
    }

    public void purchase(Player player, String offerId, Runnable onComplete) {
        if (!featureEnabled() || !canAccess(player)) {
            messageService.send(player, "boosters.no-permission");
            finish(player, onComplete, false, null);
            return;
        }
        SoulBuyerSettings.BoosterOfferSettings offer = config.boosters().offers.get(offerId);
        if (offer == null) {
            finish(player, onComplete, false, null);
            return;
        }
        BoosterType type = BoosterType.parse(offer.type);
        BoosterCurrency currency = currency();
        UUID playerId = player.getUniqueId();
        CompletableFuture<Boolean> payment = switch (currency) {
            case PROGRESSION_POINTS -> progressRepository.trySpendPoints(playerId, offer.price);
            case VAULT -> CompletableFuture.completedFuture(vaultEconomyHook.withdraw(player, offer.price));
            case PLAYER_POINTS -> CompletableFuture.completedFuture(playerPointsEconomyHook.withdraw(player, offer.price));
        };
        payment.thenCompose(paid -> {
            if (!paid) {
                return CompletableFuture.completedFuture(false);
            }
            return boosterRepository.find(playerId).thenCompose(state -> {
                long expiresAt = System.currentTimeMillis() + offer.durationSeconds * 1000L;
                Map<BoosterType, ActiveBooster> active = new EnumMap<>(state.active());
                active.put(type, new ActiveBooster(type, offer.effect, expiresAt));
                PlayerBoosterState updated = prune(new PlayerBoosterState(playerId, active));
                cache.put(playerId, updated);
                return boosterRepository.save(updated).thenApply(ignored -> true);
            });
        }).thenAccept(success -> finish(player, onComplete, success, offer));
    }

    private void finish(
            Player player,
            Runnable onComplete,
            boolean success,
            SoulBuyerSettings.BoosterOfferSettings offer
    ) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                if (success && offer != null) {
                    messageService.send(player, "boosters.purchased", new String[]{
                            "offer", messageService.guiRaw(player, offer.nameKey)
                    });
                    if (currency() == BoosterCurrency.PROGRESSION_POINTS && progressCacheRefresher != null) {
                        progressCacheRefresher.accept(player.getUniqueId());
                    }
                } else if (!success && offer != null) {
                    messageService.send(player, insufficientKey(currency()));
                }
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private PlayerBoosterState cached(UUID playerId) {
        return cache.getOrDefault(playerId, PlayerBoosterState.empty(playerId));
    }

    private PlayerBoosterState prune(PlayerBoosterState state) {
        long now = System.currentTimeMillis();
        Map<BoosterType, ActiveBooster> active = new EnumMap<>(BoosterType.class);
        for (Map.Entry<BoosterType, ActiveBooster> entry : state.active().entrySet()) {
            if (entry.getValue().active(now)) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return new PlayerBoosterState(state.playerId(), active);
    }

    private String insufficientKey(BoosterCurrency currency) {
        return switch (currency) {
            case VAULT -> "boosters.insufficient-vault";
            case PLAYER_POINTS -> "boosters.insufficient-playerpoints";
            default -> "boosters.insufficient-points";
        };
    }
}
