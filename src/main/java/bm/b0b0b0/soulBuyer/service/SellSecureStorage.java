package bm.b0b0b0.soulBuyer.service;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SellSecureStorage {

    private final Map<UUID, SecureSession> sessions = new ConcurrentHashMap<>();

    public boolean isProcessing(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public void markProcessing(UUID playerId, List<ItemStack> securedItems) {
        sessions.put(playerId, new SecureSession(SecurePhase.LOCKED, cloneAll(securedItems)));
    }

    public List<ItemStack> tryAbort(UUID playerId) {
        SecureSession session = sessions.get(playerId);
        if (session == null) {
            return List.of();
        }
        synchronized (session) {
            if (session.phase != SecurePhase.LOCKED) {
                return List.of();
            }
            session.phase = SecurePhase.ABORTED;
            sessions.remove(playerId);
            return cloneAll(session.items);
        }
    }

    public boolean tryEnterFinalize(UUID playerId) {
        SecureSession session = sessions.get(playerId);
        if (session == null) {
            return false;
        }
        synchronized (session) {
            if (session.phase != SecurePhase.LOCKED) {
                return false;
            }
            session.phase = SecurePhase.FINALIZING;
            return true;
        }
    }

    public List<ItemStack> cancelFinalize(UUID playerId) {
        SecureSession session = sessions.get(playerId);
        if (session == null) {
            return List.of();
        }
        synchronized (session) {
            if (session.phase != SecurePhase.FINALIZING) {
                return List.of();
            }
            session.phase = SecurePhase.ABORTED;
            sessions.remove(playerId);
            return cloneAll(session.items);
        }
    }

    public void commitFinalize(UUID playerId) {
        sessions.remove(playerId);
    }

    private List<ItemStack> cloneAll(List<ItemStack> items) {
        List<ItemStack> clones = new ArrayList<>();
        for (ItemStack itemStack : items) {
            if (itemStack != null && !itemStack.getType().isAir()) {
                clones.add(itemStack.clone());
            }
        }
        return clones;
    }

    private enum SecurePhase {
        LOCKED,
        FINALIZING,
        ABORTED
    }

    private static final class SecureSession {
        private SecurePhase phase;
        private final List<ItemStack> items;

        private SecureSession(SecurePhase phase, List<ItemStack> items) {
            this.phase = phase;
            this.items = items;
        }
    }
}
