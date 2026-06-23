package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.util.MaterialParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BuyerCategoryIconAnimator {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final ItemRegistry itemRegistry;
    private final GuiItemFactory itemFactory;
    private final Inventory inventory;
    private final List<CategorySlot> categorySlots = new ArrayList<>();
    private final Map<String, Integer> previewIndex = new HashMap<>();

    private Function<String, Boolean> categorySelected = categoryId -> false;
    private Function<String, String[]> categoryPairs = categoryId -> new String[0];
    private BooleanSupplier pausedCheck = () -> false;
    private BooleanSupplier menuOpenCheck = () -> false;
    private BukkitTask task;

    public BuyerCategoryIconAnimator(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            ItemRegistry itemRegistry,
            GuiItemFactory itemFactory,
            Inventory inventory,
            Map<String, GuiGeneralSettings.GuiElementSettings> elements,
            String categoryAction
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.itemRegistry = itemRegistry;
        this.itemFactory = itemFactory;
        this.inventory = inventory;
        indexCategorySlots(elements, categoryAction);
    }

    public void bind(
            Function<String, Boolean> categorySelected,
            Function<String, String[]> categoryPairs,
            BooleanSupplier pausedCheck,
            BooleanSupplier menuOpenCheck
    ) {
        this.categorySelected = categorySelected == null ? categoryId -> false : categorySelected;
        this.categoryPairs = categoryPairs == null ? categoryId -> new String[0] : categoryPairs;
        this.pausedCheck = pausedCheck == null ? () -> false : pausedCheck;
        this.menuOpenCheck = menuOpenCheck == null ? () -> false : menuOpenCheck;
    }

    public void onMenuRendered() {
        if (!config.categoryIconAnimation().enabled) {
            stop();
            return;
        }
        applyCurrentIcons();
        ensureTask();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void ensureTask() {
        if (task != null) {
            return;
        }
        long periodTicks = Math.max(20L, config.categoryIconAnimation().intervalSeconds * 20L);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, periodTicks, periodTicks);
    }

    private void tick() {
        if (!player.isOnline()) {
            stop();
            return;
        }
        if (!menuOpenCheck.getAsBoolean()) {
            stop();
            return;
        }
        if (pausedCheck.getAsBoolean()) {
            return;
        }
        advanceIndices();
        applyCurrentIcons();
    }

    private void advanceIndices() {
        for (CategorySlot categorySlot : categorySlots) {
            List<SellableItemDefinition> pool = sellableInCategory(categorySlot.categoryId);
            if (pool.isEmpty()) {
                continue;
            }
            String key = categorySlot.categoryId;
            int next = previewIndex.getOrDefault(key, 0) + 1;
            if (next >= pool.size()) {
                next = 0;
            }
            previewIndex.put(key, next);
        }
    }

    private void applyCurrentIcons() {
        seedIndicesIfNeeded();
        for (CategorySlot categorySlot : categorySlots) {
            SellableItemDefinition preview = currentPreview(categorySlot.categoryId);
            Material material = preview == null
                    ? MaterialParser.parse(categorySlot.element.material)
                    : MaterialParser.parse(preview.material());
            String[] pairs = categoryPairs.apply(categorySlot.categoryId);
            boolean selected = Boolean.TRUE.equals(categorySelected.apply(categorySlot.categoryId));
            ItemStack itemStack = selected
                    ? itemFactory.buildSelectedMaterial(player, categorySlot.element, material, pairs)
                    : itemFactory.buildMaterial(player, categorySlot.element, material, pairs);
            if (preview != null && preview.usesCustomModelData()) {
                itemFactory.applyCustomModelData(itemStack, preview.customModelData());
            }
            inventory.setItem(categorySlot.slot, itemStack);
        }
    }

    private void seedIndicesIfNeeded() {
        for (CategorySlot categorySlot : categorySlots) {
            if (previewIndex.containsKey(categorySlot.categoryId)) {
                continue;
            }
            List<SellableItemDefinition> pool = sellableInCategory(categorySlot.categoryId);
            if (pool.isEmpty()) {
                continue;
            }
            previewIndex.put(categorySlot.categoryId, ThreadLocalRandom.current().nextInt(pool.size()));
        }
    }

    private SellableItemDefinition currentPreview(String categoryId) {
        List<SellableItemDefinition> pool = sellableInCategory(categoryId);
        if (pool.isEmpty()) {
            return null;
        }
        int index = previewIndex.getOrDefault(categoryId, 0) % pool.size();
        return pool.get(index);
    }

    private List<SellableItemDefinition> sellableInCategory(String categoryId) {
        if (categoryId.isEmpty()) {
            return new ArrayList<>(itemRegistry.all());
        }
        return itemRegistry.all().stream()
                .filter(definition -> categoryId.equals(definition.categoryId()))
                .toList();
    }

    private void indexCategorySlots(Map<String, GuiGeneralSettings.GuiElementSettings> elements, String categoryAction) {
        categorySlots.clear();
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : elements.entrySet()) {
            if (!entry.getKey().startsWith("category-")) {
                continue;
            }
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (!categoryAction.equals(element.action) || element.slot < 0) {
                continue;
            }
            String categoryId = element.categoryFilter == null ? "" : element.categoryFilter;
            if (categoryId.isEmpty()) {
                continue;
            }
            categorySlots.add(new CategorySlot(element.slot, element, categoryId));
        }
    }

    private record CategorySlot(int slot, GuiGeneralSettings.GuiElementSettings element, String categoryId) {
    }
}
