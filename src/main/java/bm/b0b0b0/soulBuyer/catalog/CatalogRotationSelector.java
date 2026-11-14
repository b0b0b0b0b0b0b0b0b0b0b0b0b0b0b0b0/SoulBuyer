package bm.b0b0b0.soulBuyer.catalog;

import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CatalogRotationSelector {

    private CatalogRotationSelector() {
    }

    public static Set<String> select(
            List<SellableItemDefinition> pool,
            int targetCount,
            int minPerCategory,
            Map<String, SoulBuyerSettings.CategorySettings> categories
    ) {
        if (pool.isEmpty()) {
            return Set.of();
        }
        int safeTarget = Math.max(1, Math.min(targetCount, pool.size()));
        Map<String, List<SellableItemDefinition>> byCategory = new LinkedHashMap<>();
        for (SellableItemDefinition definition : pool) {
            byCategory.computeIfAbsent(definition.categoryId(), ignored -> new ArrayList<>()).add(definition);
        }
        for (List<SellableItemDefinition> definitions : byCategory.values()) {
            Collections.shuffle(definitions);
        }

        LinkedHashSet<String> selected = new LinkedHashSet<>();
        if (minPerCategory > 0) {
            List<String> categoryOrder = categories.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((left, right) -> Integer.compare(left.order, right.order)))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(ArrayList::new));
            for (String categoryId : categoryOrder) {
                List<SellableItemDefinition> definitions = byCategory.get(categoryId);
                if (definitions == null || definitions.isEmpty()) {
                    continue;
                }
                int pick = Math.min(minPerCategory, definitions.size());
                for (int index = 0; index < pick && selected.size() < safeTarget; index++) {
                    selected.add(definitions.get(index).id());
                }
            }
        }

        List<SellableItemDefinition> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        for (SellableItemDefinition definition : shuffled) {
            if (selected.size() >= safeTarget) {
                break;
            }
            selected.add(definition.id());
        }
        return selected;
    }
}
