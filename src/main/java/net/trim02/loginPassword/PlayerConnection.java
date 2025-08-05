package net.trim02.loginPassword;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trim02.loginPassword.Config.configVar;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayerConnection {
    private final ProxyServer server;
    private final loginPassword plugin;
    private final Logger logger;
    static HashMap<Integer, String> hashScheduledPlayerTask = new HashMap<>();

    public PlayerConnection(ProxyServer server, loginPassword plugin, Logger logger) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;

    }


    // Check if the player bypasses login requirement
    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();

        if ((configVar.oneTimeLogin && player.hasPermission(configVar.bypassNode)) || !configVar.pluginEnabled) {
            return;
        } else {
            Optional<RegisteredServer> connectToServer = server.getServer(configVar.loginServer);
            try {
                connectToServer.get().ping().get();
                event.setInitialServer(connectToServer.get());
                player.sendMessage(Component.text(configVar.welcomeMessage, NamedTextColor.GREEN));
            } catch (InterruptedException | ExecutionException e) {
                event.setInitialServer(null);
                logger.error("Error pinging login server: " + e.getMessage());
                logger.error("Make sure the login server is online");

//                player.disconnect(Component.text("Failed to connect to server", NamedTextColor.RED));

            }


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

    // Check if the player was kicked from the login server due to an unsupported client version
    @Subscribe
    public void onPlayerKick(KickedFromServerEvent event) {

        String serverPlayerKicked = event.getServer().getServerInfo().getName();
        String kickReason = PlainTextComponentSerializer.plainText().serialize(event.getServerKickReason().orElse(Component.text("No reason provided")));


        if (serverPlayerKicked.equals(configVar.loginServer) && kickReason.equals("Unsupported client version")) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Unsupported client version. Please contact an admin.", NamedTextColor.RED)));
            logger.error("A player attempted to connect to the login server with version {} and failed. Please check if the login server is updated.", event.getPlayer().getProtocolVersion());

        }
    }
    // Check if the player was redirected from the login server to another server (the hub server). If so, the player would bypass logging in, so we have to disconnect them
    @Subscribe
    public void onPlayerRedirect(KickedFromServerEvent event) {
        String serverPlayerKicked = event.getServer().getServerInfo().getName();

        if (serverPlayerKicked.equals(configVar.loginServer) && event.getResult().toString().contains("RedirectPlayer")) {

            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("There was a problem connecting to the server. Please try again later or contact an admin.", NamedTextColor.RED)));
            logger.warn("Player {} was redirected to another server, this usually means they were unable to connect to the login server for some reason. Check if the login server is updated or online.", event.getPlayer().getUsername());
        }

    }

}
