package bm.b0b0b0.soulBuyer.market;

import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.*;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class PriceQuoteService {

    private final ItemRegistry itemRegistry;
    private final MarketService marketService;
    private final ProgressionService progressionService;
    private final BoosterService boosterService;

    public PriceQuoteService(
            ItemRegistry itemRegistry,
            MarketService marketService,
            ProgressionService progressionService,
            BoosterService boosterService
    ) {
        this.itemRegistry = itemRegistry;
        this.marketService = marketService;
        this.progressionService = progressionService;
        this.boosterService = boosterService;
    }

    public SellQuote quote(Player player, List<ItemStack> stacks, PlayerProgress progress) {
        double permissionMultiplier = progressionService.permissionMultiplier(player);
        double categoryBonus = progressionService.categoryBonus(progress);
        double additiveMultiplier = boosterService.additiveMultiplier(player);
        double moneyBooster = boosterService.moneyMultiplier(player);
        double playerMultiplier = permissionMultiplier * categoryBonus + additiveMultiplier;

        List<SellLine> lines = new ArrayList<>();
        double totalMoney = 0.0D;
        double totalPoints = 0.0D;
        double avgMarket = 0.0D;
        int marketSamples = 0;

        Map<String, Integer> grouped = new HashMap<>();
        Map<String, SellableItemDefinition> definitions = new HashMap<>();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<SellableItemDefinition> definitionOptional = itemRegistry.find(stack);
            if (definitionOptional.isEmpty()) {
                continue;
            }
            SellableItemDefinition definition = definitionOptional.get();
            grouped.merge(definition.id(), stack.getAmount(), Integer::sum);
            definitions.put(definition.id(), definition);
        }

        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            SellableItemDefinition definition = definitions.get(entry.getKey());
            double market = marketService.coefficient(definition.id());
            double unitPrice = progressionService.moneyUnitPrice(
                    definition.basePrice(),
                    market,
                    permissionMultiplier,
                    categoryBonus,
                    additiveMultiplier,
                    moneyBooster
            );
            double pointsBasis = progressionService.pointsUnitPriceBasis(
                    definition.basePrice(),
                    market,
                    permissionMultiplier
            );
            double unitPoints = progressionService.pointsForSale(definition, 1, pointsBasis);
            SellLine line = new SellLine(definition.id(), entry.getValue(), unitPrice, unitPoints);
            lines.add(line);
            totalMoney += line.totalMoney();
            totalPoints += line.totalPoints();
            avgMarket += market;
            marketSamples++;
        }

        double marketDisplay = marketSamples == 0 ? 1.0D : avgMarket / marketSamples;
        return new SellQuote(lines, totalMoney, totalPoints, marketDisplay, playerMultiplier);
    }

    public ItemUnitQuote unitQuote(Player player, SellableItemDefinition definition, PlayerProgress progress) {
        double permissionMultiplier = progressionService.permissionMultiplier(player);
        double categoryBonus = progressionService.categoryBonus(progress);
        double additiveMultiplier = boosterService.additiveMultiplier(player);
        double moneyBooster = boosterService.moneyMultiplier(player);
        double playerMultiplier = permissionMultiplier * categoryBonus + additiveMultiplier;
        double market = marketService.coefficient(definition.id());
        double unitPrice = progressionService.moneyUnitPrice(
                definition.basePrice(),
                market,
                permissionMultiplier,
                categoryBonus,
                additiveMultiplier,
                moneyBooster
        );
        double pointsBasis = progressionService.pointsUnitPriceBasis(
                definition.basePrice(),
                market,
                permissionMultiplier
        );
        double unitPoints = progressionService.pointsForSale(definition, 1, pointsBasis);
        int inventoryAmount = InventorySellHelper.countMatching(player, itemRegistry, definition.id());
        return new ItemUnitQuote(unitPrice, unitPoints, market, playerMultiplier, inventoryAmount);
    }
}
