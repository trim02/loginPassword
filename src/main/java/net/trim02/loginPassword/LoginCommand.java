package net.trim02.loginPassword;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.trim02.loginPassword.loginPassword.configVar;
import org.slf4j.Logger;

import java.util.Optional;

public class LoginCommand implements SimpleCommand {
    private final ProxyServer server;
    public static LuckPerms lpApi = LuckPermsProvider.get();
//    public static boolean luckPermsIsPresent;

    public LoginCommand(ProxyServer server, PluginManager pluginManager, Logger logger) {
        this.server = server;
//        if(pluginManager.getPlugin("LuckPerms").isPresent()) {
//            lpApi = LuckPermsProvider.get();
//            luckPermsIsPresent = true;
//        }
    }
//    public static boolean luckPermsPresent(){
//        return luckPermsIsPresent;
//    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(Component.text(configVar.noPassword, NamedTextColor.RED));
            return;
        }
//        source.sendMessage(Component.text("Hello World from a command!", NamedTextColor.AQUA));
        if (source instanceof Player player && args[0].equals(configVar.serverPassword)) {
            Optional<RegisteredServer> connectToServer = server.getServer(configVar.hubServer);
            player.createConnectionRequest(connectToServer.get()).connectWithIndication();

            if (!player.hasPermission("group." + configVar.bypassGroup) && (configVar.oneTimeLogin && configVar.pluginGrantsBypass) && configVar.bypassMethod.equals("group")) {
//                System.out.println("---- Adding group ----");
                lpApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().add(Node.builder("group." + configVar.bypassGroup).build());
                });
            } else if (!player.hasPermission(configVar.bypassNode) && (configVar.oneTimeLogin && configVar.pluginGrantsBypass) && configVar.bypassMethod.equals("user")) {
//                System.out.println("---- Adding node ----");
                lpApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().add(Node.builder(configVar.bypassNode).build());
                });
            }

        } else if (!args[0].equals(configVar.serverPassword)) {
            source.sendMessage(Component.text(configVar.wrongPassword, NamedTextColor.RED));
        } else if (source instanceof ConsoleCommandSource) {
            source.sendMessage(Component.text("This command can only be run by a player", NamedTextColor.RED));
        } else {
            source.sendMessage(Component.text("An error occurred. This shouldn't happen. Bother trim", NamedTextColor.RED));
        }

    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        boolean sourcePermissionNode = invocation.source().hasPermission(configVar.bypassNode);
        boolean sourcePermissionGroup = invocation.source().hasPermission("group." + configVar.bypassGroup);
//        System.out.println("User has group: " + invocation.source().hasPermission("group." + configVar.bypassGroup));
//        System.out.println("User has node: " + invocation.source().hasPermission(configVar.bypassNode));
//        System.out.println("Bypass method: " + configVar.bypassMethod);
//        System.out.println("One Time Login: " + configVar.oneTimeLogin);
//        System.out.println("disable command: " + configVar.disableLoginCommandOnBypass);
        if (configVar.loginCommandNegated) {
            if ((configVar.oneTimeLogin && configVar.disableLoginCommandOnBypass) && ((configVar.bypassMethod.equals("group") && sourcePermissionGroup) || (configVar.bypassMethod.equals("user") && sourcePermissionNode))) {
//                System.out.println("This command should be disabled");
                return false;
            } else {
                return true;
            }

        } else {
            return invocation.source().hasPermission("loginpassword.login");
        }
//        return (configVar.oneTimeLogin && configVar.disableLoginCommandOnBypass) && !(configVar.bypassMethod.equals("group") && sourcePermissionGroup) || !(configVar.bypassMethod.equals("user") && sourcePermissionNode);


    }
}
