package net.trim02.loginPassword;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.trim02.loginPassword.Config.configVar;
import org.slf4j.Logger;

import java.util.Optional;

public class LoginCommandLuckPerms extends LoginCommand {
    public static LuckPerms lpApi = LuckPermsProvider.get();

    public LoginCommandLuckPerms(ProxyServer server, Logger logger) {
        super(server, logger);
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();
        CommandSource source = invocation.source();


        if (args.length == 0) {
            source.sendMessage(Component.text(configVar.noPassword, NamedTextColor.RED));
            return;
        }
        if (source instanceof Player player && args[0].equals(configVar.serverPassword)) {
            Optional<RegisteredServer> connectToServer = server.getServer(configVar.hubServer);
            player.createConnectionRequest(connectToServer.get()).connectWithIndication();

            logger.info("Player {} has logged in", player.getUsername());

            if (!player.hasPermission(configVar.bypassNode) && (configVar.oneTimeLogin && configVar.pluginGrantsBypass)
                    && args[0].equals(configVar.serverPassword)) {

                // logger.info("Granting bypass permission to user {}", player.getUsername());

                if (configVar.bypassMethod.equalsIgnoreCase("user")) {
                    lpApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
                        user.data().add(Node.builder(configVar.bypassNode).build());
                    });

                    // logger.debug("Bypass permission granted to user {} using user method", player.getUsername());

                } else if (configVar.bypassMethod.equalsIgnoreCase("group")) {
                    lpApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
                        user.data().add(Node.builder("group." + configVar.bypassGroup).build());
                    });

                    // logger.debug("Bypass permission granted to user {} using group method", player.getUsername());

                } else {
                    logger.error(
                            "An error occurred while granting the user {} bypass permission.",
                            player.getUsername());
                    source.sendMessage(
                            Component.text("An error occurred. Please inform server staff.", NamedTextColor.RED));
                }
            }
        } else if (!args[0].equals(configVar.serverPassword)) {
            source.sendMessage(Component.text(configVar.wrongPassword, NamedTextColor.RED));
        } else if (source instanceof ConsoleCommandSource) {
            source.sendMessage(Component.text("This command can only be run by a player", NamedTextColor.RED));
        } else {
            source.sendMessage(
                    Component.text("An error occurred. This shouldn't happen. Bother trim", NamedTextColor.RED));
                    logger.error("An error occurred. This shouldn't happen. Bother trim");
        }

    }
}
