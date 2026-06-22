package bm.b0b0b0.soulBuyer.config;

import bm.b0b0b0.soulBuyer.config.settings.*;
import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;

import java.util.List;
import java.util.Map;

public final class PluginConfig {

    private final SoulBuyerSettings main;
    private final SoulBuyerItemsSettings items;
    private final GuiGeneralSettings generalGui;
    private final GuiBuyerSettings buyerGui;
    private final GuiQuantitySettings quantityGui;
    private final GuiAutosellSettings autosellGui;
    private final GuiBoostersSettings boostersGui;

    public PluginConfig(
            SoulBuyerSettings main,
            SoulBuyerItemsSettings items,
            GuiGeneralSettings generalGui,
            GuiBuyerSettings buyerGui,
            GuiQuantitySettings quantityGui,
            GuiAutosellSettings autosellGui,
            GuiBoostersSettings boostersGui
    ) {
        this.main = main;
        this.items = items;
        this.generalGui = generalGui;
        this.buyerGui = buyerGui;
        this.quantityGui = quantityGui;
        this.autosellGui = autosellGui;
        this.boostersGui = boostersGui;
    }

    public SoulBuyerSettings main() {
        return main;
    }

    public SoulBuyerItemsSettings itemsSettings() {
        return items;
    }

    public GuiGeneralSettings generalGui() {
        return generalGui;
    }

    public GuiBuyerSettings buyerGui() {
        return buyerGui;
    }

    public GuiQuantitySettings quantityGui() {
        return quantityGui;
    }

    public GuiAutosellSettings autosellGui() {
        return autosellGui;
    }

    public GuiBoostersSettings boostersGui() {
        return boostersGui;
    }

    public SoulBuyerSettings.BoostersSettings boosters() {
        return main.boosters;
    }

    public SoulBuyerSettings.SellLimitsSettings sellLimits() {
        return main.sellLimits;
    }

    public boolean boostersFeatureEnabled() {
        return main.boosters.enabled;
    }

    public String permissionBoosters() {
        return main.permissions.boosters;
    }

    public String storageType() {
        return main.storageType;
    }

    public boolean isFlatStorage() {
        return "flat".equalsIgnoreCase(main.storageType);
    }

    public boolean isSqliteStorage() {
        return "sqlite".equalsIgnoreCase(main.storageType);
    }

    public boolean isMysqlStorage() {
        return "mysql".equalsIgnoreCase(main.storageType);
    }

    public SoulBuyerSettings.StorageSettings storage() {
        return main.storage;
    }

    public String serverId() {
        return main.network.serverId;
    }

    public boolean singleServer() {
        return main.network.singleServer;
    }

    public SoulBuyerSettings.MysqlSettings mysql() {
        return main.mysql;
    }

    public SoulBuyerSettings.RedisSettings redis() {
        return main.redis;
    }

    public String defaultLocale() {
        return main.locale.defaultLocale;
    }

    public String fallbackLocale() {
        return main.locale.fallbackLocale;
    }

    public String permissionUse() {
        return main.permissions.use;
    }

    public String permissionAdmin() {
        return main.permissions.admin;
    }

    public String mainCommand() {
        return main.commands.main;
    }

    public List<String> commandOpenAliases() {
        return main.commands.openAliases;
    }

    public SoulBuyerSettings.MarketSettings market() {
        return main.market;
    }

    public SoulBuyerSettings.CatalogRotationSettings catalogRotation() {
        return main.catalogRotation;
    }

    public SoulBuyerSettings.CategoryIconAnimationSettings categoryIconAnimation() {
        return main.categoryIconAnimation;
    }

    public SoulBuyerSettings.ProgressionSettings progression() {
        return main.progression;
    }

    public SoulBuyerSettings.AutosellSettings autosell() {
        return main.autosell;
    }

    public boolean autosellFeatureEnabled() {
        return main.autosell.enabled;
    }

    public String permissionAutosell() {
        return main.permissions.autosell;
    }

    public String permissionDonate() {
        return main.permissions.donate;
    }

    public SoulBuyerSettings.EconomySettings economy() {
        return main.economy;
    }

    public boolean playerPointsEnabled() {
        return main.economy.playerPointsEnabled;
    }

    public boolean donateBuyerEnabled() {
        return main.economy.donateBuyerEnabled;
    }

    public boolean donateBuyerActive() {
        return main.economy.playerPointsEnabled && main.economy.donateBuyerEnabled;
    }

    public boolean requiresVaultEconomy() {
        return !main.economy.playerPointsEnabled || main.economy.donateBuyerEnabled;
    }

    public boolean requiresPlayerPoints() {
        return main.economy.playerPointsEnabled;
    }

    public BuyerPayoutMode defaultOpenPayoutMode() {
        if (!main.economy.playerPointsEnabled) {
            return BuyerPayoutMode.VAULT;
        }
        if (main.economy.donateBuyerEnabled) {
            return BuyerPayoutMode.VAULT;
        }
        return BuyerPayoutMode.PLAYER_POINTS;
    }

    public BuyerPayoutMode autosellPayoutMode() {
        return defaultOpenPayoutMode();
    }

    public String buyerTitleKey(BuyerPayoutMode mode) {
        if (mode == BuyerPayoutMode.PLAYER_POINTS && donateBuyerActive()) {
            return "gui.donate-buyer.title";
        }
        if (mode == BuyerPayoutMode.PLAYER_POINTS) {
            return "gui.buyer.title-playerpoints";
        }
        String configured = buyerGui.titleKey;
        if (configured == null || configured.isBlank()) {
            return "gui.buyer.title";
        }
        return configured;
    }

    public String donateMainCommand() {
        return main.commands.donateBuyer.main;
    }

    public List<String> donateCommandOpenAliases() {
        return main.commands.donateBuyer.openAliases;
    }

    public Map<String, SoulBuyerSettings.CategorySettings> categories() {
        return main.categories;
    }

    public Map<String, SoulBuyerSettings.SellableItemSettings> items() {
        return items.items;
    }

    public List<Integer> buyerContentSlots() {
        return buyerGui.contentSlots;
    }

    public List<Integer> buyerSeparatorSlots() {
        return buyerGui.separatorSlots;
    }

    public int buyerSize() {
        return buyerGui.size;
    }

    public double minMarketCoefficient() {
        return main.market.minCoefficient;
    }

    public boolean debug() {
        return main.debug;
    }
}
