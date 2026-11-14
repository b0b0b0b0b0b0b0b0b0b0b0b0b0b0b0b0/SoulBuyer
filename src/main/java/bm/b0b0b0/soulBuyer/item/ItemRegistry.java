package bm.b0b0b0.soulBuyer.item;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.util.ItemStacks;
import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemRegistry {

    private volatile Map<String, SellableItemDefinition> poolById = Map.of();
    private volatile Set<String> activeIds = Set.of();

    public ItemRegistry(PluginConfig config) {
        reload(config);
    }

    public void reload(PluginConfig config) {
        LinkedHashMap<String, SellableItemDefinition> nextPool = new LinkedHashMap<>();
        for (Map.Entry<String, SoulBuyerSettings.SellableItemSettings> entry : config.items().entrySet()) {
            SoulBuyerSettings.SellableItemSettings settings = entry.getValue();
            nextPool.put(entry.getKey(), new SellableItemDefinition(
                    entry.getKey(),
                    settings.material,
                    settings.displayMaterial,
                    settings.category,
                    settings.basePrice,
                    settings.basePoints,
                    settings.customModelData == null ? -1 : settings.customModelData
            ));
        }
        poolById = Map.copyOf(nextPool);
        activateAll();
    }

    public void activateAll() {
        activeIds = Set.copyOf(poolById.keySet());
    }

    public void applyActiveIds(Set<String> ids) {
        Map<String, SellableItemDefinition> pool = poolById;
        LinkedHashSet<String> validated = new LinkedHashSet<>();
        for (String id : ids) {
            if (pool.containsKey(id)) {
                validated.add(id);
            }
        }
        if (validated.isEmpty() && !pool.isEmpty()) {
            validated.add(pool.keySet().iterator().next());
        }
        activeIds = Set.copyOf(validated);
    }

    public Collection<SellableItemDefinition> all() {
        Map<String, SellableItemDefinition> pool = poolById;
        Set<String> active = activeIds;
        List<SellableItemDefinition> result = new ArrayList<>(active.size());
        for (String id : active) {
            SellableItemDefinition definition = pool.get(id);
            if (definition != null) {
                result.add(definition);
            }
        }
        return result;
    }

    public List<SellableItemDefinition> activeByCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return List.of();
        }
        Map<String, SellableItemDefinition> pool = poolById;
        Set<String> active = activeIds;
        List<SellableItemDefinition> result = new ArrayList<>();
        for (String id : active) {
            SellableItemDefinition definition = pool.get(id);
            if (definition != null && categoryId.equals(definition.categoryId())) {
                result.add(definition);
            }
        }
        result.sort(java.util.Comparator.comparing(SellableItemDefinition::id));
        return result;
    }

    public List<SellableItemDefinition> pool() {
        return List.copyOf(poolById.values());
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

    public boolean existsInPool(String itemId) {
        return itemId != null && !itemId.isBlank() && poolById.containsKey(itemId);
    }

    public Optional<SellableItemDefinition> definitionInPool(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(poolById.get(itemId));
    }

    public Optional<SellableItemDefinition> resolve(String itemId) {
        return byId(itemId).or(() -> definitionInPool(itemId));
    }

    public Optional<SellableItemDefinition> find(ItemStack itemStack) {
        if (ItemStacks.isAbsent(itemStack)) {
            return Optional.empty();
        }
        Map<String, SellableItemDefinition> pool = poolById;
        for (String id : activeIds) {
            SellableItemDefinition definition = pool.get(id);
            if (definition != null && matches(definition, itemStack)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public Optional<SellableItemDefinition> findInPool(ItemStack itemStack) {
        if (ItemStacks.isAbsent(itemStack)) {
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
        Material material = MaterialParser.parse(definition.material());
        if (itemStack.getType() != material) {
            return false;
        }
        if (!definition.usesCustomModelData()) {
            return true;
        }
        return ItemStacks.matchesCustomModelData(itemStack, definition.customModelData());
    }
}
