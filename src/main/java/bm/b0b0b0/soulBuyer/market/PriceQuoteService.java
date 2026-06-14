package bm.b0b0b0.soulBuyer.market;

import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.SellLine;
import bm.b0b0b0.soulBuyer.model.SellQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PriceQuoteService {

    private final ItemRegistry itemRegistry;
    private final MarketService marketService;
    private final ProgressionService progressionService;

    public PriceQuoteService(
            ItemRegistry itemRegistry,
            MarketService marketService,
            ProgressionService progressionService
    ) {
        this.itemRegistry = itemRegistry;
        this.marketService = marketService;
        this.progressionService = progressionService;
    }

    public SellQuote quote(Player player, List<ItemStack> stacks, PlayerProgress progress) {
        double playerMultiplier = progressionService.permissionMultiplier(player)
                * progressionService.categoryBonus(progress);
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
            double unitPrice = definition.basePrice() * market * playerMultiplier;
            double unitPoints = progressionService.pointsForSale(definition, 1, unitPrice) * playerMultiplier;
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
        double playerMultiplier = progressionService.permissionMultiplier(player)
                * progressionService.categoryBonus(progress);
        double market = marketService.coefficient(definition.id());
        double unitPrice = definition.basePrice() * market * playerMultiplier;
        double unitPoints = progressionService.pointsForSale(definition, 1, unitPrice) * playerMultiplier;
        int inventoryAmount = InventorySellHelper.countMatching(player, itemRegistry, definition.id());
        return new ItemUnitQuote(unitPrice, unitPoints, market, playerMultiplier, inventoryAmount);
    }
}
