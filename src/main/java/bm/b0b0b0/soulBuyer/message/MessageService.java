package bm.b0b0b0.soulBuyer.message;

import bm.b0b0b0.soulBuyer.integration.PlaceholderApiBridge;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageService {

    private final MessageLoader loader;
    private final Map<UUID, String> playerLocales = new HashMap<>();
    private PlaceholderApiBridge placeholderApiBridge;

    public MessageService(MessageLoader loader) {
        this.loader = loader;
    }

    public void bindPlaceholderApi(PlaceholderApiBridge placeholderApiBridge) {
        this.placeholderApiBridge = placeholderApiBridge;
    }

    public String locale(Player player) {
        return playerLocales.getOrDefault(player.getUniqueId(), loader.defaultLocale());
    }

    public Component component(Player player, String key, String... pairs) {
        return component(locale(player), key, pairs);
    }

    public Component component(String locale, String key, String... pairs) {
        String prefix = loader.raw(locale, "prefix");
        String body = HexColorParser.replacePlaceholders(loader.raw(locale, key), pairs);
        return HexColorParser.parse(prefix + body);
    }

    public Component guiText(Player player, String key, String... pairs) {
        return HexColorParser.parse(expand(player, loader.raw(locale(player), key), pairs));
    }

    public Component guiTextWithComponent(Player player, String key, String placeholder, Component value) {
        String template = expand(player, loader.raw(locale(player), key));
        String token = "{" + placeholder + "}";
        int index = template.indexOf(token);
        if (index < 0) {
            return HexColorParser.parse(template);
        }
        String prefix = template.substring(0, index);
        String suffix = template.substring(index + token.length());
        Style itemStyle = HexColorParser.parse(prefix + "x").style();
        Component styledValue = value
                .style(value.style().merge(itemStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET))
                .decoration(TextDecoration.ITALIC, false);
        return HexColorParser.parse(prefix).append(styledValue).append(HexColorParser.parse(suffix));
    }

    public Component guiItemName(Player player, String key, Component itemName) {
        String template = expand(player, loader.raw(locale(player), key), "name", "");
        Style style = HexColorParser.parse(template).style();
        return itemName
                .style(itemName.style().merge(style, Style.Merge.Strategy.IF_ABSENT_ON_TARGET))
                .decoration(TextDecoration.ITALIC, false);
    }

    public List<Component> guiLore(Player player, String key, String... pairs) {
        return loader.lore(locale(player), key).stream()
                .map(line -> HexColorParser.parse(expand(player, line, pairs)))
                .toList();
    }

    public String guiRaw(Player player, String key, String... pairs) {
        return expand(player, loader.raw(locale(player), key), pairs);
    }

    private String expand(Player player, String template, String... pairs) {
        String expanded = HexColorParser.replacePlaceholders(template, pairs);
        if (player != null && placeholderApiBridge != null) {
            expanded = placeholderApiBridge.apply(player, expanded);
        }
        return expanded;
    }

    public void send(Player player, String key, String... pairs) {
        player.sendMessage(component(player, key, pairs));
    }

    public void sendActionBar(Player player, String key, String... pairs) {
        player.sendActionBar(guiText(player, key, pairs));
    }

    public void send(CommandSender sender, String key, String... pairs) {
        if (sender instanceof Player player) {
            send(player, key, pairs);
            return;
        }
        sender.sendMessage(HexColorParser.parse(
                HexColorParser.replacePlaceholders(loader.raw(loader.defaultLocale(), key), pairs)
        ));
    }

    public String raw(Player player, String key) {
        return loader.raw(locale(player), key);
    }

    public void reload() {
        loader.load();
    }

    public void reloadAsync(JavaPlugin plugin, Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            loader.load();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }
}
