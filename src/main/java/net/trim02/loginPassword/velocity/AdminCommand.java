package net.trim02.loginPassword.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.UuidUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trim02.loginPassword.BuildConstants;
import net.trim02.loginPassword.Config;
import net.trim02.loginPassword.Config.configVar;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.common.MojangApi;
import net.trim02.loginPassword.interfaces.loginPassword;
import net.trim02.loginPassword.loginPasswordVelocity;
import org.slf4j.Logger;

import java.util.UUID;


public class AdminCommand implements SimpleCommand {
    protected final loginPassword<?, ?> plugin;
    protected final ProxyServer server;
    protected final Logger logger;
    protected final Config config;

    public AdminCommand(loginPasswordVelocity plugin, ProxyServer server, Logger logger, Config config) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.config = config;
    }


    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            // Future Reference:
            // /loginpassword set <key> <value> - Sets the config value
            // /loginpassword get <key> - Gets the config value
            source.sendMessage(Component.text(
                    """
                            loginPassword v%s by trim02
                            /loginpassword reload - Reloads the config
                            /loginpassword add <uuid|player> - Adds a player to the bypass list
                            /loginpassword remove <uuid|player> - Removes a player from the bypass list
                            /loginpassword list - Lists all players in the bypass list
                            /loginpassword config - Shows the current config
                            /loginpassword toggle - Enables/disables the plugin
                            """.formatted(BuildConstants.VERSION), NamedTextColor.GREEN));
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

            case "toggle" -> {
                config.togglePlugin();
                source.sendMessage(Component.text("Plugin has been " + (configVar.pluginEnabled ? "enabled" : "disabled"),
                        NamedTextColor.GREEN));

            }
            case "add" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /loginpassword add <uuid|player>", NamedTextColor.RED));
                    return;
                }

                String player = args[1];
                String playerName;

                if (!BypassList.validUUID(player)) {

                    playerName = player;
                    UUID uuid;
                    try {
                        uuid = UuidUtils.fromUndashed(MojangApi.getUUID(playerName));
                    } catch (Exception e) {
                        source.sendMessage(Component.text("Player " + playerName + " not found.", NamedTextColor.RED));
                        return;
                    }

                    player = uuid.toString();

                } else {

                    playerName = server.getPlayer(player).get().getUsername();

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
                    UUID uuid;
                    try {
                        uuid = UuidUtils.fromUndashed(MojangApi.getUUID(playerName));
                    } catch (Exception e) {
                        source.sendMessage(Component.text("Player " + playerName + " not found.", NamedTextColor.RED));
                        return;
                    }

                    player = uuid.toString();

                } else {

                    playerName = server.getPlayer(player).get().getUsername();

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
    public boolean hasPermission(Invocation invocation) {
        CommandSource source = invocation.source();
        return (source instanceof Player player && player.hasPermission("loginpassword.admin")) || (source instanceof ConsoleCommandSource);
    }
}
