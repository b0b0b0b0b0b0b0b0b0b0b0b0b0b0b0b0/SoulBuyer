package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.GuiGeneralSettings;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class BuyerCategoryIconAnimator {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final ItemRegistry itemRegistry;
    private final GuiItemFactory itemFactory;
    private final Inventory inventory;
    private final List<CategorySlot> categorySlots = new ArrayList<>();
    private final Map<String, Integer> previewIndex = new HashMap<>();

    private Supplier<String> activeCategoryFilter = () -> "";
    private BooleanSupplier processingCheck = () -> false;
    private BukkitTask task;

    public BuyerCategoryIconAnimator(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            ItemRegistry itemRegistry,
            GuiItemFactory itemFactory,
            Inventory inventory,
            Map<String, GuiGeneralSettings.GuiElementSettings> elements
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.itemRegistry = itemRegistry;
        this.itemFactory = itemFactory;
        this.inventory = inventory;
        indexCategorySlots(elements);
    }

    public void bind(
            Supplier<String> activeCategoryFilter,
            BooleanSupplier processingCheck
    ) {
        this.activeCategoryFilter = activeCategoryFilter;
        this.processingCheck = processingCheck;
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
        if (!(player.getOpenInventory().getTopInventory().getHolder(false) instanceof BuyerMenu)) {
            stop();
            return;
        }
        if (processingCheck.getAsBoolean()) {
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
        String activeFilter = activeCategoryFilter.get();
        if (activeFilter == null) {
            activeFilter = "";
        }
        for (CategorySlot categorySlot : categorySlots) {
            SellableItemDefinition preview = currentPreview(categorySlot.categoryId);
            Material material = preview == null
                    ? parseMaterial(categorySlot.element.material)
                    : parseMaterial(preview.material());
            boolean selected = activeFilter.equals(categorySlot.categoryId);
            ItemStack itemStack = selected
                    ? itemFactory.buildSelectedMaterial(player, categorySlot.element, material)
                    : itemFactory.buildMaterial(player, categorySlot.element, material);
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

    private void indexCategorySlots(Map<String, GuiGeneralSettings.GuiElementSettings> elements) {
        categorySlots.clear();
        for (Map.Entry<String, GuiGeneralSettings.GuiElementSettings> entry : elements.entrySet()) {
            if (!entry.getKey().startsWith("category-")) {
                continue;
            }
            GuiGeneralSettings.GuiElementSettings element = entry.getValue();
            if (!"CATEGORY_FILTER".equals(element.action) || element.slot < 0) {
                continue;
            }
            String categoryId = element.categoryFilter == null ? "" : element.categoryFilter;
            if (categoryId.isEmpty()) {
                continue;
            }
            categorySlots.add(new CategorySlot(element.slot, element, categoryId));
        }
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Material.STONE;
        }
    }

    private record CategorySlot(int slot, GuiGeneralSettings.GuiElementSettings element, String categoryId) {
    }
}
