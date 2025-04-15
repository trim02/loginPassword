package net.trim02.loginPassword;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.trim02.loginPassword.Config.configVar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PlayerConnection {
    private final ProxyServer server;
    private final loginPassword plugin;
    static HashMap<Integer, String> hashScheduledPlayerTask = new HashMap<>();

    public PlayerConnection(ProxyServer server, loginPassword plugin) {
        this.server = server;
        this.plugin = plugin;

    }


    // Check if the player bypasses login requirement
    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();

        if (configVar.oneTimeLogin && player.hasPermission(configVar.bypassNode)) {
            return;
        } else {
            Optional<RegisteredServer> connectToServer = server.getServer(configVar.loginServer);
            event.setInitialServer(connectToServer.get());
        }
    }

    // Creates a scheduled task to kick the player after 30 seconds if they do not enter the password
    @Subscribe
    public void onPlayerJoinLoginServer(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer connectedServer = event.getServer();

        if (connectedServer.getServerInfo().getName().equals(configVar.loginServer) && configVar.loginCommandNegated.equals(true)) {
            ScheduledTask task = server.getScheduler().buildTask(plugin, () -> player.disconnect(Component.text(configVar.kickMessage))).delay(configVar.kickTimeout, TimeUnit.SECONDS).schedule();
            hashScheduledPlayerTask.put(player.getUniqueId().hashCode(), String.valueOf(task.toString().hashCode()));

        }

    }

    // Creates a scheduled task to cancel the above task if the player has transferred from the login server to the hub server, otherwise the player will be kicked from the hub server
    @Subscribe
    public void onPlayerLeaveLoginServer(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> transferServer = event.getPreviousServer();
        RegisteredServer connectedServer = event.getServer();
        if (transferServer.isEmpty()) {
            return;
        }
        if (connectedServer.getServerInfo().getName().equals(configVar.hubServer) && Objects.equals(transferServer.get().getServerInfo().getName(), configVar.loginServer)) {
            server.getScheduler().buildTask(plugin, () -> {
                Collection<ScheduledTask> tasks = server.getScheduler().tasksByPlugin(plugin);
                for (ScheduledTask cancelTask : tasks) {
                    if (hashScheduledPlayerTask.containsKey(player.getUniqueId().hashCode())) {

                        if (!cancelTask.status().toString().equals("FINISHED")) {
                            cancelTask.cancel();
                        }

                    }
                }
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }

}
