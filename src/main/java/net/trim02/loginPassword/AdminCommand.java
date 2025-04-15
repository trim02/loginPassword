package net.trim02.loginPassword;

import net.trim02.loginPassword.Config.configVar;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;


public class AdminCommand implements SimpleCommand {
    protected final ProxyServer server;
    protected final Logger logger;
    protected final Config config;

    public AdminCommand(ProxyServer server, Logger logger, Config config) {
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
            default -> source.sendMessage(Component.text("Unknown command", NamedTextColor.RED));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource source = invocation.source();
        return (source instanceof Player player && player.hasPermission("loginpassword.admin")) || (source instanceof ConsoleCommandSource);
    }
}
