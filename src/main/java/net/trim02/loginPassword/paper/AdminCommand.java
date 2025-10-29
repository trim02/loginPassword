package net.trim02.loginPassword.paper;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trim02.loginPassword.BuildConstants;
import net.trim02.loginPassword.Config;
import net.trim02.loginPassword.Config.configVar;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.loginPasswordPaper;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;

import java.util.UUID;

public class AdminCommand implements BasicCommand {

    protected final loginPasswordPaper plugin;
    protected final Server server;
    protected final Logger logger;
    protected final Config config;

    public AdminCommand(loginPasswordPaper plugin, Server server, Logger logger, Config config) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {

        CommandSender source = commandSourceStack.getSender();


        if (args.length == 0) {
            // Future Reference:
            // /loginpassword set <key> <value> - Sets the config value
            // /loginpassword get <key> - Gets the config value
            source.sendMessage(Component.text("""
                    loginPassword v%s by trim02
                    /loginpassword reload - Reloads the config
                    /loginpassword add <uuid|player> - Adds a player to the bypass list
                    /loginpassword remove <uuid|player> - Removes a player from the bypass list
                    /loginpassword list - Lists all players in the bypass list
                    /loginpassword toggle - Enables/disables the plugin
                    """.formatted(BuildConstants.VERSION)));
            return;
        }
        switch (args[0]) {
            case "reload" -> {
                config.reloadConfig();
                source.sendMessage(Component.text("Config reloaded", NamedTextColor.GREEN));
            }
            case "config" -> source.sendMessage(Component.text(
                    """
                            loginPassword v%s by trim02
                            Config:
                            pluginEnabled: %s
                            [core]
                            loginServer: %s
                            hubServer: %s
                            serverPassword: %s
                            oneTimeLogin: %s
                            [core.bypass]
                            bypassNode: %s
                            disableLoginCommandOnBypass: %s
                            pluginGrantsBypass: %s
                            [core.bypass.methods]
                            bypassGroup: %s
                            bypassMethod: %s
                            [core.kick]
                            kickTimeout: %s
                            kickMessage: %s
                            [messages]
                            wrongPassword: %s
                            noPassword: %s
                            """.formatted(BuildConstants.VERSION,
                            configVar.pluginEnabled, configVar.loginServer,
                            configVar.hubServer, configVar.serverPassword, configVar.oneTimeLogin,
                            configVar.bypassNode, configVar.disableLoginCommandOnBypass, configVar.pluginGrantsBypass,
                            configVar.bypassGroup, configVar.bypassMethod, configVar.kickTimeout,
                            configVar.kickMessage, configVar.wrongPassword, configVar.noPassword), NamedTextColor.GREEN));

//        else if (args[0].equals("set")) {
//            if (args.length < 3) {
//                source.sendMessage(Component.text("Usage: /loginpassword set <key> <value>", NamedTextColor.RED));
//                return;
//            }
//            config.setConfigValue(args[1], args[2]);
//            source.sendMessage(Component.text("Config set", NamedTextColor.GREEN));
//        }
//        else if (args[0].equals("get")) {
//            if (args.length < 2) {
//                source.sendMessage(Component.text("Usage: /loginpassword get <key>", NamedTextColor.RED));
//                return;
//            }
//            String value = configVar.getConfig(args[1]);
//            source.sendMessage(Component.text(value, NamedTextColor.GREEN));
//        }
            case "toggle" -> {
                config.togglePlugin();
                source.sendMessage(Component.text("Plugin has been " + (configVar.pluginEnabled ? "enabled" : "disabled"),
                        NamedTextColor.GREEN));
//                source.sendMessage(Component.text("Plugin toggled", NamedTextColor.GREEN));
            }
            case "add" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /loginpassword add <uuid|player>", NamedTextColor.RED));
                    return;
                }

                String player = args[1];
                String playerName;
                logger.info("args[1]: " + player);
                if (!BypassList.validUUID(player)) {

                    playerName = player;
                    UUID uuid = server.getPlayerUniqueId(player);

                    if (uuid != null) {
                        player = uuid.toString();

                    } else {
                        source.sendMessage(Component.text("Player " + playerName + " not found.", NamedTextColor.RED));
                        return;
                    }

                } else {

                    playerName = server.getOfflinePlayer((UUID.fromString(player))).getName();

                    if (playerName == null) {
                        playerName = "Unknown";
                    }
                }
                BypassList.loadBypassList();
                if (!BypassList.inBypassList(UUID.fromString(player))) {
                    BypassList.addBypassEntry(player);
                    source.sendMessage(Component.text("Player " + playerName + " added to bypass list", NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Player " + playerName + " is already in the bypass list", NamedTextColor.RED));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /loginpassword remove <player>", NamedTextColor.RED));
                    return;
                }
                String player = args[1];
                String playerName;
                if (!BypassList.validUUID(player)) {

                    playerName = player;
                    UUID uuid = server.getPlayerUniqueId(player);

                    if (uuid != null) {
                        player = uuid.toString();

                    } else {
                        source.sendMessage(Component.text("Player " + playerName + " not found.", NamedTextColor.RED));
                        return;
                    }

                } else {

                    playerName = server.getOfflinePlayer((UUID.fromString(player))).getName();

                    if (playerName == null) {
                        playerName = "Unknown";
                    }
                }


                BypassList.loadBypassList();
                if (BypassList.inBypassList(UUID.fromString(player))) {
                    BypassList.removeBypassEntry(player);
                    source.sendMessage(Component.text("Player " + playerName + " removed from bypass list", NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Player " + playerName + " is not in the bypass list", NamedTextColor.RED));
                }
            }
            case "list" -> {
                // TODO: convert from UUIDs to player names

                StringBuilder listMessage = new StringBuilder("Bypass List:\n");
                for (int i = 0; i < BypassList.bypassList.size(); i++) {
                    listMessage.append("- ").append(BypassList.bypassList.get(i).getAsString()).append("\n");
                }
                source.sendMessage(Component.text(listMessage.toString(), NamedTextColor.GREEN));
            }
            default -> source.sendMessage(Component.text("Unknown command", NamedTextColor.RED));
        }


    }

    @Override
    public boolean canUse(CommandSender sender) {

        return sender.hasPermission("loginpassword.admin");
    }
}
