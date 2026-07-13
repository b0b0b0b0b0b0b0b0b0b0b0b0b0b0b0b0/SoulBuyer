package bm.b0b0b0.soulBuyer.message;

import bm.b0b0b0.soulBuyer.integration.PlaceholderApiBridge;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private boolean disableGuiItemItalic = true;

    public MessageService(MessageLoader loader) {
        this.loader = loader;
    }

    public void bindPlaceholderApi(PlaceholderApiBridge placeholderApiBridge) {
        this.placeholderApiBridge = placeholderApiBridge;
    }

    public void setDisableGuiItemItalic(boolean disableGuiItemItalic) {
        this.disableGuiItemItalic = disableGuiItemItalic;
    }

    public String locale(Player player) {
        String override = playerLocales.get(player.getUniqueId());
        if (override != null) {
            return override;
        }
        String clientLanguage = player.locale().getLanguage().toLowerCase(Locale.ROOT);
        if (loader.containsLocale(clientLanguage)) {
            return clientLanguage;
        }
        return loader.defaultLocale();
    }

    public Component component(Player player, String key, String... pairs) {
        return component(locale(player), key, pairs);
    }

    public Component component(String locale, String key, String... pairs) {
        String prefix = loader.raw(locale, "prefix");
        String body = HexColorParser.replacePlaceholders(loader.raw(locale, key), pairs);
        return HexColorParser.parse(prefix).append(HexColorParser.parse(body));
    }

    public Component guiText(Player player, String key, String... pairs) {
        return withoutItemItalic(HexColorParser.parse(expand(player, loader.raw(locale(player), key), pairs)));
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
        return withoutItemItalic(HexColorParser.parse(prefix))
                .append(styledValue)
                .append(withoutItemItalic(HexColorParser.parse(suffix)));
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
                .map(line -> formatGuiLoreLine(player, line, pairs))
                .toList();
    }

    public boolean hasKey(Player player, String key) {
        return hasKey(locale(player), key);
    }

    public boolean hasKey(String locale, String key) {
        return loader.containsKey(locale, key);
    }

    private Component formatGuiLoreLine(Player player, String line, String... pairs) {
        if (line == null) {
            return withoutItemItalic(Component.text(" "));
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty() || "<empty>".equalsIgnoreCase(trimmed) || "<blank>".equalsIgnoreCase(trimmed)) {
            return withoutItemItalic(Component.text(" "));
        }
        return withoutItemItalic(HexColorParser.parse(expand(player, line, pairs)));
    }

    private Component withoutItemItalic(Component component) {
        if (!disableGuiItemItalic || component == null) {
            return component;
        }
        return component.decoration(TextDecoration.ITALIC, false);
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
