package bm.b0b0b0.soulBuyer.limit;

import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.PlayerSellLimitUsage;
import bm.b0b0b0.soulBuyer.model.SellLimitSplit;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.repository.PlayerSellLimitRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SellLimitService {

    private final PluginConfig config;
    private final ItemRegistry itemRegistry;
    private final BoosterService boosterService;
    private final PlayerSellLimitRepository sellLimitRepository;

    public SellLimitService(
            PluginConfig config,
            ItemRegistry itemRegistry,
            BoosterService boosterService,
            PlayerSellLimitRepository sellLimitRepository
    ) {
        this.config = config;
        this.itemRegistry = itemRegistry;
        this.boosterService = boosterService;
        this.sellLimitRepository = sellLimitRepository;
    }

    public boolean enabled() {
        return config.sellLimits().enabled;
    }

    public String periodKey() {
        return SellLimitPeriod.dailyKey();
    }

    public int perItemLimit(Player player) {
        if (!enabled()) {
            return Integer.MAX_VALUE;
        }
        SoulBuyerSettings.SellLimitsSettings limits = config.sellLimits();
        int base = limits.defaultPerItem;
        for (Map.Entry<String, Integer> entry : limits.permissionLimits.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                base = Math.max(base, entry.getValue());
            }
        }
        return (int) Math.max(1, Math.floor(base * boosterService.limitMultiplier(player)));
    }

    public int remaining(Player player, PlayerSellLimitUsage usage, String itemId) {
        if (!enabled()) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, perItemLimit(player) - usage.sold(itemId));
    }

    public SellLimitSplit split(Player player, List<ItemStack> stacks, PlayerSellLimitUsage usage) {
        if (!enabled()) {
            return new SellLimitSplit(stacks, List.of());
        }
        Map<String, Integer> grouped = new HashMap<>();
        Map<String, List<ItemStack>> stacksByItem = new HashMap<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definitionOptional = itemRegistry.find(stack);
            if (definitionOptional.isEmpty()) {
                continue;
            }
            String itemId = definitionOptional.get().id();
            grouped.merge(itemId, stack.getAmount(), Integer::sum);
            stacksByItem.computeIfAbsent(itemId, ignored -> new ArrayList<>()).add(stack.clone());
        }

        List<ItemStack> sell = new ArrayList<>();
        List<ItemStack> returned = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            int allowed = remaining(player, usage, entry.getKey());
            int total = entry.getValue();
            int toSell = Math.min(total, allowed);
            int toReturn = total - toSell;
            partitionStacks(stacksByItem.get(entry.getKey()), toSell, toReturn, sell, returned);
        }
        return new SellLimitSplit(sell, returned);
    }

    private void partitionStacks(
            List<ItemStack> source,
            int sellAmount,
            int returnAmount,
            List<ItemStack> sell,
            List<ItemStack> returned
    ) {
        int sellLeft = sellAmount;
        int returnLeft = returnAmount;
        for (ItemStack stack : source) {
            int amount = stack.getAmount();
            int sellPart = Math.min(amount, sellLeft);
            sellLeft -= sellPart;
            int returnPart = Math.min(amount - sellPart, returnLeft);
            returnLeft -= returnPart;
            if (sellPart > 0) {
                ItemStack part = stack.clone();
                part.setAmount(sellPart);
                sell.add(part);
            }
            if (returnPart > 0) {
                ItemStack part = stack.clone();
                part.setAmount(returnPart);
                returned.add(part);
            }
        }
    }
}
