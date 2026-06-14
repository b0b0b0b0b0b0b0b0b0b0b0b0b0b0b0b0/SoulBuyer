package bm.b0b0b0.soulBuyer.integration;

import bm.b0b0b0.soulBuyer.service.BuyerStatsService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoulBuyerPlaceholderExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final BuyerStatsService buyerStatsService;

    public SoulBuyerPlaceholderExpansion(JavaPlugin plugin, BuyerStatsService buyerStatsService) {
        this.plugin = plugin;
        this.buyerStatsService = buyerStatsService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "soulbuyer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "b0b0b0";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        return buyerStatsService.resolvePlaceholder(player, params);
    }
}
