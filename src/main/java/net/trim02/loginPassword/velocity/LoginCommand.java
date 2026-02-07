package net.trim02.loginPassword.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trim02.loginPassword.Config.configVar;
import org.slf4j.Logger;

import java.util.Optional;

public class LoginCommand implements SimpleCommand {
    protected final ProxyServer server;
    protected final Logger logger;


    public LoginCommand(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
 
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(Component.text(configVar.noPassword, NamedTextColor.RED));
            return;
        }
        if (source instanceof Player player && !args[0].isEmpty()) {
            int loopCount = 0;
            boolean loggedIn = false;
            for (String password : configVar.serverPassword) {
                if (args[0].equals(password)) {
                    Optional<RegisteredServer> connectToServer = server.getServer(configVar.hubServer);
                    player.createConnectionRequest(connectToServer.get()).connectWithIndication();
                    logger.info("Player {} has logged in", player.getUsername());
                    loggedIn = true;
                } else {
                    loopCount++;
                    if ((loopCount >= configVar.serverPassword.size())) {
                        source.sendMessage(Component.text(configVar.wrongPassword, NamedTextColor.RED));
                    }
                }
//            Optional<RegisteredServer> connectToServer = server.getServer(configVar.hubServer);
//            player.createConnectionRequest(connectToServer.get()).connectWithIndication();

                // logger.info("Player {} has logged in", player.getUsername());

            }
        }
//        else if (!args[0].equals(configVar.serverPassword)) {
//            source.sendMessage(Component.text(configVar.wrongPassword, NamedTextColor.RED));
//        }
        else if (source instanceof ConsoleCommandSource) {
            source.sendMessage(Component.text("This command can only be run by a player", NamedTextColor.RED));
        } else {
            source.sendMessage(
                    Component.text("An error occurred. This shouldn't happen. Bother trim", NamedTextColor.RED));
                    logger.error("An error occurred. This shouldn't happen. Bother trim");
        }

    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        boolean sourcePermissionNode = invocation.source().hasPermission(configVar.bypassNode);

        if (configVar.loginCommandNegated) {
            return !(configVar.oneTimeLogin && configVar.disableLoginCommandOnBypass && sourcePermissionNode);
        } else {
            return invocation.source().hasPermission(configVar.loginCommandNode);
        }
    }
}
