package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultEconomyHook {

    private static final String ECONOMY_CLASS = "net.milkbowl.vault.economy.Economy";

    public enum ProbeState {
        READY,
        VAULT_ABSENT,
        ECONOMY_ABSENT
    }

    private final SoulBuyerDebugLog debug;
    private Object economy;
    private Method depositPlayer;
    private Method withdrawPlayer;
    private Method has;
    private Method getBalance;
    private Method getName;
    private Method transactionSuccess;

    public VaultEconomyHook(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        this.debug = debug;
    }

    public ProbeState probe() {
        try {
            RegisteredServiceProvider<?> provider = findEconomyProvider();
            if (provider != null && provider.getProvider() != null) {
                if (!bind(provider.getProvider())) {
                    debug.log("vault hook: Economy provider methods missing");
                    return ProbeState.ECONOMY_ABSENT;
                }
                debug.log("vault hook OK: " + providerName());
                return ProbeState.READY;
            }
            if (!EconomyPluginPresence.vaultBridgeInstalled()) {
                debug.log("vault hook: Vault/VaultUnlocked missing and no Economy service");
                return ProbeState.VAULT_ABSENT;
            }
            debug.log("vault hook: Economy service not registered yet");
            return ProbeState.ECONOMY_ABSENT;
        } catch (NoClassDefFoundError | ExceptionInInitializerError error) {
            debug.warn("vault hook: Economy API unavailable (" + error.getClass().getSimpleName() + ")");
            return ProbeState.VAULT_ABSENT;
        }
    }

    public boolean hook() {
        return probe() == ProbeState.READY;
    }

    public boolean available() {
        return economy != null;
    }

    public String providerName() {
        if (economy == null || getName == null) {
            return "";
        }
        try {
            Object name = getName.invoke(economy);
            return name == null ? "" : String.valueOf(name);
        } catch (ReflectiveOperationException exception) {
            return "";
        }
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (economy == null || depositPlayer == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        try {
            Object response = depositPlayer.invoke(economy, player, amount);
            boolean success = isTransactionSuccess(response);
            debug.log("vault deposit " + player.getName() + " amount=" + amount + " success=" + success);
            return success;
        } catch (ReflectiveOperationException exception) {
            debug.warn("vault deposit failed: " + exception.getMessage());
            return false;
        }
    }

    public double balance(OfflinePlayer player) {
        if (economy == null || getBalance == null) {
            return 0.0D;
        }
        try {
            Object balance = getBalance.invoke(economy, player);
            return balance instanceof Number number ? number.doubleValue() : 0.0D;
        } catch (ReflectiveOperationException exception) {
            return 0.0D;
        }
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (economy == null || has == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(has.invoke(economy, player, amount));
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null || withdrawPlayer == null || amount <= 0.0D) {
            return amount <= 0.0D;
        }
        try {
            Object response = withdrawPlayer.invoke(economy, player, amount);
            boolean success = isTransactionSuccess(response);
            debug.log("vault withdraw " + player.getName() + " amount=" + amount + " success=" + success);
            return success;
        } catch (ReflectiveOperationException exception) {
            debug.warn("vault withdraw failed: " + exception.getMessage());
            return false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RegisteredServiceProvider<?> findEconomyProvider() {
        ServicesManager services = Bukkit.getServicesManager();
        for (Class<?> service : services.getKnownServices()) {
            if (!ECONOMY_CLASS.equals(service.getName())) {
                continue;
            }
            RegisteredServiceProvider provider = services.getRegistration(service);
            if (provider != null) {
                return provider;
            }
        }
        return null;
    }

    private boolean bind(Object provider) {
        try {
            Class<?> economyClass = provider.getClass();
            depositPlayer = findMethod(economyClass, "depositPlayer", OfflinePlayer.class, double.class);
            withdrawPlayer = findMethod(economyClass, "withdrawPlayer", OfflinePlayer.class, double.class);
            has = findMethod(economyClass, "has", OfflinePlayer.class, double.class);
            getBalance = findMethod(economyClass, "getBalance", OfflinePlayer.class);
            getName = findMethod(economyClass, "getName");
            if (depositPlayer == null || withdrawPlayer == null) {
                return false;
            }
            economy = provider;
            return true;
        } catch (Exception exception) {
            debug.warn("vault bind failed: " + exception.getMessage());
            economy = null;
            return false;
        }
    }

    private boolean isTransactionSuccess(Object response) throws ReflectiveOperationException {
        if (response == null) {
            return false;
        }
        if (transactionSuccess == null) {
            transactionSuccess = findMethod(response.getClass(), "transactionSuccess");
        }
        if (transactionSuccess == null) {
            return false;
        }
        return Boolean.TRUE.equals(transactionSuccess.invoke(response));
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... params) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            try {
                Method method = iface.getMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
