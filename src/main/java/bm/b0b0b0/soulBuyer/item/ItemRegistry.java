package bm.b0b0b0.soulBuyer.item;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class ItemRegistry {

    private final Map<String, SellableItemDefinition> poolById = new LinkedHashMap<>();
    private Set<String> activeIds = Set.of();

    public ItemRegistry(PluginConfig config) {
        reload(config);
    }

    public void reload(PluginConfig config) {
        poolById.clear();
        for (Map.Entry<String, SoulBuyerSettings.SellableItemSettings> entry : config.items().entrySet()) {
            SoulBuyerSettings.SellableItemSettings settings = entry.getValue();
            poolById.put(entry.getKey(), new SellableItemDefinition(
                    entry.getKey(),
                    settings.material,
                    settings.category,
                    settings.basePrice,
                    settings.basePoints,
                    settings.customModelData == null ? -1 : settings.customModelData
            ));
        }
        activateAll();
    }

    public void activateAll() {
        activeIds = new LinkedHashSet<>(poolById.keySet());
    }

    public void applyActiveIds(Set<String> ids) {
        LinkedHashSet<String> validated = new LinkedHashSet<>();
        for (String id : ids) {
            if (poolById.containsKey(id)) {
                validated.add(id);
            }
        }
        if (validated.isEmpty() && !poolById.isEmpty()) {
            validated.add(poolById.keySet().iterator().next());
        }
        activeIds = validated;
    }

    public Collection<SellableItemDefinition> all() {
        List<SellableItemDefinition> active = new ArrayList<>();
        for (String id : activeIds) {
            SellableItemDefinition definition = poolById.get(id);
            if (definition != null) {
                active.add(definition);
            }
        }
        return active;
    }

    public List<SellableItemDefinition> pool() {
        return new ArrayList<>(poolById.values());
    }

    public int poolSize() {
        return poolById.size();
    }

    public int activeSize() {
        return activeIds.size();
    }

    public boolean isActive(String itemId) {
        return activeIds.contains(itemId);
    }

    public Optional<SellableItemDefinition> find(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }
        for (String id : activeIds) {
            SellableItemDefinition definition = poolById.get(id);
            if (definition != null && matches(definition, itemStack)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public Optional<SellableItemDefinition> findInPool(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }
        for (SellableItemDefinition definition : poolById.values()) {
            if (matches(definition, itemStack)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public Optional<SellableItemDefinition> byId(String itemId) {
        if (!activeIds.contains(itemId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(poolById.get(itemId));
    }

    private boolean matches(SellableItemDefinition definition, ItemStack itemStack) {
        Material material = parseMaterial(definition.material());
        if (itemStack.getType() != material) {
            return false;
        }
        if (!definition.usesCustomModelData()) {
            return true;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasCustomModelData()
                && definition.customModelData() == meta.getCustomModelData();
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Material.STONE;
        }
    }
}
