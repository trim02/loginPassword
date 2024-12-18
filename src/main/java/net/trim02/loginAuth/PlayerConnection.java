package net.trim02.loginAuth;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.trim02.loginAuth.LoginAuth.configVar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PlayerConnection {
    private final ProxyServer server;
    private final LoginAuth plugin;
    static HashMap<Integer, String> hashScheduledPlayerTask = new HashMap<>();

    public PlayerConnection(ProxyServer server, LoginAuth plugin) {
        this.server = server;
        this.plugin = plugin;

    }

    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    // Check if the player bypasses login requirement
    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();

        if ((configVar.oneTimeLogin) && ((configVar.bypassMethod.equals("group") && isPlayerInGroup(player, configVar.bypassGroup)) || ((configVar.bypassMethod.equals("user") && player.hasPermission(configVar.bypassNode))))) {
            return;
//            System.out.println("Player is in group");
////            player.transferToHost(new InetSocketAddress("localhost",25566));
//            Optional<RegisteredServer> connectToServer = server.getServer(configVar.hubServer);
//            System.out.println("RegisteredServer Info: " + connectToServer.get().getServerInfo().getName());
////            player.createConnectionRequest(connectToServer.get()).connectWithIndication();
//            event.setInitialServer(connectToServer.get());


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

        if (connectedServer.getServerInfo().getName().equals(configVar.loginServer)) {
//            System.out.println("--- First SCE event ---");
//            System.out.println("Config server: " + configVar.loginServer);
//            System.out.println("Player " + player.getUsername() + " has logged in to server: " + connectedServer.getServerInfo().getName());
//            server.getScheduler().buildTask(plugin,() -> {
//                System.out.println("This message is delayed by 10 seconds");
//            }).delay(5L, TimeUnit.SECONDS).schedule();
            ScheduledTask task = server.getScheduler().buildTask(plugin, () -> {

//                System.out.println("This message is delayed by 10 seconds: " + player);
                Component kickMessage = ((configVar.kickMessage).equals("default")) ? Component.text("You were kicked for failing to provide the password within " + configVar.kickTimeout + " seconds") : Component.text(configVar.kickMessage);
                player.disconnect(kickMessage);
            }).delay(configVar.kickTimeout, TimeUnit.SECONDS).schedule();
            hashScheduledPlayerTask.put(player.getUniqueId().hashCode(), String.valueOf(task.toString().hashCode()));
//            System.out.println("Scheduled task: " + task.toString().hashCode());
//            System.out.println("Hash Map everything: " + hashScheduledPlayerTask.size());
//            System.out.println("--- First SCE END ---");

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
//            System.out.println("------ Second SCE event ------");
//            System.out.println("Player transfered from: " + transferServer.orElse(null).getServerInfo().getName());
//            System.out.println("Player " + player.getUsername() + " has logged in to server: " + connectedServer.getServerInfo().getName());
            ScheduledTask task = server.getScheduler().buildTask(plugin, () -> {
                Collection<ScheduledTask> tasks = server.getScheduler().tasksByPlugin(plugin);
//            System.out.println(tasks.stream().distinct(hashScheduledPlayerTask.get(player.getUniqueId().toString())));
//            ScheduledTask quitTask = hashScheduledPlayerTask.get(player.getUniqueId().toString());
                for (ScheduledTask cancelTask : tasks) {
//                    System.out.println("Canceling tasks...");
//                    int cancelTaskHashCode = cancelTask.toString().hashCode();
                    if (hashScheduledPlayerTask.containsKey(player.getUniqueId().hashCode())) {
//                        System.out.println("Task: " + cancelTask.toString().hashCode());

//                        System.out.println("1 Task status: " + cancelTask.status());
                        if (!cancelTask.status().toString().equals("FINISHED")) {
//                            System.out.println("Player has quit, canceling task");
                            cancelTask.cancel();
                        }
//                        System.out.println("2 Task status: " + cancelTask.status());
                    }
                }
//                System.out.println("All tasks: " + tasks);
//                System.out.println("Hash map : " + hashScheduledPlayerTask.get(player.getUniqueId().hashCode()));
//                System.out.println("--- Second SCE END ---");
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }

}
