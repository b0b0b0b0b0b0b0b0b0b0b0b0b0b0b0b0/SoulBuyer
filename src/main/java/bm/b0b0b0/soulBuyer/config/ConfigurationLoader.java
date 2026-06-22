package bm.b0b0b0.soulBuyer.config;

import bm.b0b0b0.soulBuyer.config.settings.*;
import bm.b0b0b0.soulBuyer.debug.SoulBuyerDebugLog;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class ConfigurationLoader {

    private final SoulBuyerSettings mainSettings = new SoulBuyerSettings();
    private final SoulBuyerItemsSettings itemsSettings = new SoulBuyerItemsSettings();
    private final GuiGeneralSettings generalGuiSettings = new GuiGeneralSettings();
    private final GuiBuyerSettings buyerGuiSettings = new GuiBuyerSettings();
    private final GuiQuantitySettings quantityGuiSettings = new GuiQuantitySettings();
    private final GuiAutosellSettings autosellGuiSettings = new GuiAutosellSettings();
    private final GuiBoostersSettings boostersGuiSettings = new GuiBoostersSettings();
    private PluginConfig pluginConfig;

    public PluginConfig load(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        SerializedConfigReloader.reload(plugin, mainSettings, Path.of("config.yml"), debug);
        SerializedConfigReloader.reload(plugin, itemsSettings, Path.of("items.yml"), debug);
        SerializedConfigReloader.reload(plugin, generalGuiSettings, Path.of("gui", "general.yml"), debug);
        SerializedConfigReloader.reload(plugin, buyerGuiSettings, Path.of("gui", "buyer.yml"), debug);
        SerializedConfigReloader.reload(plugin, quantityGuiSettings, Path.of("gui", "quantity.yml"), debug);
        SerializedConfigReloader.reload(plugin, autosellGuiSettings, Path.of("gui", "autosell.yml"), debug);
        SerializedConfigReloader.reload(plugin, boostersGuiSettings, Path.of("gui", "boosters.yml"), debug);
        pluginConfig = new PluginConfig(
                mainSettings,
                itemsSettings,
                generalGuiSettings,
                buyerGuiSettings,
                quantityGuiSettings,
                autosellGuiSettings,
                boostersGuiSettings
        );
        return pluginConfig;
    }

    public PluginConfig reload(JavaPlugin plugin, SoulBuyerDebugLog debug) {
        return load(plugin, debug);
    }

    public SoulBuyerSettings mainSettings() {
        return mainSettings;
    }

    public SoulBuyerItemsSettings itemsSettings() {
        return itemsSettings;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }
}
