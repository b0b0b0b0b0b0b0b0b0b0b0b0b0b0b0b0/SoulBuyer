package bm.b0b0b0.soulBuyer.command;

import bm.b0b0b0.soulBuyer.SoulBuyer;
import bm.b0b0b0.soulBuyer.SoulBuyerRuntime;
import bm.b0b0b0.soulBuyer.config.ConfigurationLoader;
import bm.b0b0b0.soulBuyer.config.PluginConfig;
import bm.b0b0b0.soulBuyer.config.settings.SoulBuyerSettings;
import bm.b0b0b0.soulBuyer.message.MessageService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class SoulBuyerCommandRegistrar {

    private static final SoulBuyerSettings DEFAULT_SETTINGS = new SoulBuyerSettings();

    private final SoulBuyer plugin;
    private final SoulBuyerRuntime runtime;
    private volatile PluginConfig config;
    private volatile MessageService messageService;
    private volatile ConfigurationLoader configurationLoader;

    public SoulBuyerCommandRegistrar(SoulBuyer plugin, SoulBuyerRuntime runtime) {
        this.plugin = plugin;
        this.runtime = runtime;
    }

    public void registerLifecycle() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            String mainName = defaultMainCommand();
            plugin.debug().boot("registering commands: " + mainName + " + aliases " + defaultAliases(mainName));
            event.registrar().register(buildCommandTree(mainName, false).build(), "Open SoulBuyer menu");
            for (String alias : defaultAliases(mainName)) {
                event.registrar().register(buildCommandTree(alias, false).build(), "Open SoulBuyer menu");
            }
            String donateMain = defaultDonateMainCommand();
            plugin.debug().boot("registering donate commands: " + donateMain + " + aliases " + defaultDonateAliases(donateMain));
            event.registrar().register(buildCommandTree(donateMain, true).build(), "Open donate SoulBuyer menu");
            for (String alias : defaultDonateAliases(donateMain)) {
                event.registrar().register(buildCommandTree(alias, true).build(), "Open donate SoulBuyer menu");
            }
        });
    }

    public void bind(PluginConfig pluginConfig, MessageService boundMessages, ConfigurationLoader loader) {
        this.config = pluginConfig;
        this.messageService = boundMessages;
        this.configurationLoader = loader;
    }

    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildCommandTree(
            String mainName,
            boolean donateMenu
    ) {
        return Commands.literal(mainName)
                .requires(source -> source.getSender().hasPermission(permissionUse())
                        || source.getSender().hasPermission(permissionAdmin())
                        || (donateMenu && source.getSender().hasPermission(permissionDonate())))
                .executes(context -> openMenu(context, donateMenu))
                .then(Commands.literal("admin")
                        .requires(source -> source.getSender().hasPermission(permissionAdmin()))
                        .then(Commands.literal("reload").executes(this::reload)));
    }

    private int openMenu(CommandContext<CommandSourceStack> context, boolean donateMenu) {
        CommandSender sender = context.getSource().getSender();
        plugin.debug().log("command openMenu from " + sender.getName()
                + " donate=" + donateMenu
                + " ready=" + runtime.isReady()
                + " msgBound=" + (messageService != null));
        if (!requireReady(sender)) {
            return 0;
        }
        if (!(sender instanceof Player player)) {
            send(sender, "command.player-only");
            return 0;
        }
        if (donateMenu) {
            if (!donateBuyerActive()) {
                send(player, "command.donate-disabled");
                return 0;
            }
            if (!player.hasPermission(permissionDonate()) && !player.hasPermission(permissionAdmin())) {
                plugin.debug().log("command denied: no permission " + permissionDonate());
                send(player, "command.no-permission");
                return 0;
            }
            plugin.debug().log("opening donate buyer GUI for " + player.getName());
            runtime.buyerGuiService().openDonateBuyer(player);
            return Command.SINGLE_SUCCESS;
        }
        if (!player.hasPermission(permissionUse())) {
            plugin.debug().log("command denied: no permission " + permissionUse());
            send(player, "command.no-permission");
            return 0;
        }
        plugin.debug().log("opening buyer GUI for " + player.getName());
        runtime.buyerGuiService().open(player);
        return Command.SINGLE_SUCCESS;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        ConfigurationLoader loader = configurationLoader;
        if (loader == null) {
            send(sender, "error.database");
            return 0;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PluginConfig reloaded = loader.reload(plugin, plugin.debug());
                messageService.reloadAsync(plugin, () -> {
                    runtime.updatePluginConfig(reloaded);
                    runtime.itemRegistry().reload(reloaded);
                    if (runtime.catalogRotationService() != null) {
                        runtime.catalogRotationService().reload(reloaded);
                    }
                    plugin.debug().setEnabled(reloaded.debug());
                    plugin.debug().log("admin reload OK");
                    send(sender, "command.reload-success");
                });
            } catch (Exception exception) {
                plugin.debug().error("admin reload failed", exception);
                Bukkit.getScheduler().runTask(plugin, () -> send(sender, "command.reload-failed"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private boolean requireReady(CommandSender sender) {
        if (runtime.isReady()) {
            return true;
        }
        plugin.debug().warn("command blocked: runtime not ready for " + sender.getName());
        send(sender, "error.database");
        return false;
    }

    private void send(CommandSender sender, String key) {
        MessageService messages = messageService;
        if (messages != null) {
            messages.send(sender, key);
            return;
        }
        sender.sendMessage(fallbackMessage(key));
    }

    private Component fallbackMessage(String key) {
        if ("error.database".equals(key)) {
            return Component.text("SoulBuyer is still loading, please wait...");
        }
        if ("command.player-only".equals(key)) {
            return Component.text("Players only.");
        }
        if ("command.no-permission".equals(key)) {
            return Component.text("Insufficient permissions.");
        }
        if ("command.donate-disabled".equals(key)) {
            return Component.text("Donate buyer is disabled.");
        }
        return Component.text(key);
    }

    private boolean donateBuyerActive() {
        if (config != null) {
            return config.donateBuyerActive();
        }
        return DEFAULT_SETTINGS.economy.playerPointsEnabled && DEFAULT_SETTINGS.economy.donateBuyerEnabled;
    }

    private String permissionUse() {
        return config != null ? config.permissionUse() : DEFAULT_SETTINGS.permissions.use;
    }

    private String permissionDonate() {
        return config != null ? config.permissionDonate() : DEFAULT_SETTINGS.permissions.donate;
    }

    private String permissionAdmin() {
        return config != null ? config.permissionAdmin() : DEFAULT_SETTINGS.permissions.admin;
    }

    private String defaultMainCommand() {
        return normalizeCommand(DEFAULT_SETTINGS.commands.main);
    }

    private String defaultDonateMainCommand() {
        return normalizeCommand(DEFAULT_SETTINGS.commands.donateBuyer.main);
    }

    private List<String> defaultAliases(String mainName) {
        return normalizeAliases(DEFAULT_SETTINGS.commands.openAliases, mainName);
    }

    private List<String> defaultDonateAliases(String mainName) {
        return normalizeAliases(DEFAULT_SETTINGS.commands.donateBuyer.openAliases, mainName);
    }

    private String normalizeCommand(String name) {
        if (name == null || name.isBlank()) {
            return "soulbuyer";
        }
        return name.toLowerCase(Locale.ROOT).trim();
    }

    private List<String> normalizeAliases(List<String> aliases, String mainName) {
        Set<String> unique = new LinkedHashSet<>();
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias == null || alias.isBlank()) {
                    continue;
                }
                String normalized = alias.toLowerCase(Locale.ROOT).trim();
                if (normalized.equals(mainName)) {
                    continue;
                }
                unique.add(normalized);
            }
        }
        return new ArrayList<>(unique);
    }
}
