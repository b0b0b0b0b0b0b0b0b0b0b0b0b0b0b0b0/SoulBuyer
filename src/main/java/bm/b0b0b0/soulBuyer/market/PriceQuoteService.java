package bm.b0b0b0.soulBuyer.market;

import bm.b0b0b0.soulBuyer.booster.BoosterService;
import bm.b0b0b0.soulBuyer.item.ItemRegistry;
import bm.b0b0b0.soulBuyer.model.ItemUnitQuote;
import bm.b0b0b0.soulBuyer.model.PlayerProgress;
import bm.b0b0b0.soulBuyer.model.SellLine;
import bm.b0b0b0.soulBuyer.model.SellQuote;
import bm.b0b0b0.soulBuyer.model.SellableItemDefinition;
import bm.b0b0b0.soulBuyer.progression.ProgressionService;
import bm.b0b0b0.soulBuyer.service.InventorySellHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

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

    public SellQuote quote(Player player, List<org.bukkit.inventory.ItemStack> stacks, PlayerProgress progress) {
        PricingContext pricing = pricingContext(player, progress);
        InventorySellHelper.GroupedStacks grouped = InventorySellHelper.groupStacks(
                itemRegistry,
                stacks,
                InventorySellHelper.CatalogScope.ACTIVE
        );

        List<SellLine> lines = new ArrayList<>();
        double totalMoney = 0.0D;
        double totalPoints = 0.0D;
        double avgMarket = 0.0D;
        int marketSamples = 0;

        for (Map.Entry<String, Integer> entry : grouped.amounts().entrySet()) {
            SellableItemDefinition definition = grouped.definitions().get(entry.getKey());
            double market = marketService.coefficient(definition.id());
            SellLine line = lineQuote(definition, entry.getValue(), market, pricing);
            lines.add(line);
            totalMoney += line.totalMoney();
            totalPoints += line.totalPoints();
            avgMarket += market;
            marketSamples++;
        }

        double marketDisplay = marketSamples == 0 ? 1.0D : avgMarket / marketSamples;
        return new SellQuote(lines, totalMoney, totalPoints, marketDisplay, pricing.playerMultiplier());
    }

    public ItemUnitQuote unitQuote(Player player, SellableItemDefinition definition, PlayerProgress progress) {
        PricingContext pricing = pricingContext(player, progress);
        double market = marketService.coefficient(definition.id());
        SellLine line = lineQuote(definition, 1, market, pricing);
        int inventoryAmount = InventorySellHelper.countMatching(player, itemRegistry, definition.id());
        return new ItemUnitQuote(
                line.unitPrice(),
                line.unitPoints(),
                market,
                pricing.playerMultiplier(),
                inventoryAmount
        );
    }

    private SellLine lineQuote(
            SellableItemDefinition definition,
            int amount,
            double market,
            PricingContext pricing
    ) {
        double unitPrice = progressionService.moneyUnitPrice(
                definition.basePrice(),
                market,
                pricing.permissionMultiplier(),
                pricing.categoryBonus(),
                pricing.additiveMultiplier(),
                pricing.moneyBooster()
        );
        double pointsBasis = progressionService.pointsUnitPriceBasis(
                definition.basePrice(),
                market,
                pricing.permissionMultiplier()
        );
        double unitPoints = progressionService.pointsForSale(definition, 1, pointsBasis);
        return new SellLine(definition.id(), amount, unitPrice, unitPoints);
    }

    private PricingContext pricingContext(Player player, PlayerProgress progress) {
        double permissionMultiplier = progressionService.permissionMultiplier(player);
        double categoryBonus = progressionService.categoryBonus(progress);
        double additiveMultiplier = boosterService.additiveMultiplier(player);
        double moneyBooster = boosterService.moneyMultiplier(player);
        double playerMultiplier = permissionMultiplier * categoryBonus + additiveMultiplier;
        return new PricingContext(
                permissionMultiplier,
                categoryBonus,
                additiveMultiplier,
                moneyBooster,
                playerMultiplier
        );
    }

    private record PricingContext(
            double permissionMultiplier,
            double categoryBonus,
            double additiveMultiplier,
            double moneyBooster,
            double playerMultiplier
    ) {
    }
}
