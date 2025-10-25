package net.trim02.loginPassword.paper;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.connection.PlayerCommonConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.trim02.loginPassword.loginPasswordPaper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;
import net.trim02.loginPassword.Config.configVar;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.trim02.loginPassword.BuildConstants.DIALOG_NAMESPACE;

public class DialogLogin implements Listener {
    private loginPasswordPaper plugin;
    private Server server;
    private final Logger logger;
    private final Map<UUID, CompletableFuture<Boolean>> connectingPlayers = new ConcurrentHashMap<>();

    public DialogLogin(loginPasswordPaper plugin, Server server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }




    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        logger.info("Sending player to hub2 via plugin message channel.");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF("hub2");
        player.sendPluginMessage(plugin, loginPasswordPaper.PluginChannelFull, out.toByteArray());
    }

    @EventHandler
    void onPlayerConfiguration(AsyncPlayerConnectionConfigureEvent event) {
        Dialog dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key(DIALOG_NAMESPACE, "login_dialog"));
        if (dialog == null){
            logger.error("Failed to load dialog");

            return;
        }

        PlayerConfigurationConnection connection = event.getConnection();
        UUID playerUUID = connection.getProfile().getId();
//        UUID testUUID = UUID.fromString("0c8a1f84-c036-447c-a946-fec2524dc9d6");
//        server.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
//            logger.info("Player name via sync: {}", connection.getProfile().getName());
//            logger.info("CraftPlayer: {}", server.getOfflinePlayer(playerUUID));
//        });
//        logger.info("Test profile via async: {}", server.getOfflinePlayer(testUUID));
//        logger.info("Player UUID: {}", playerUUID);
        if (playerUUID == null) {
            logger.error("Player UUID is null");
            return;
        }
//        logger.info("Player profile: {}", connection.getProfile());
//        if (server.getPlayer(playerUUID) == null) {
//            logger.info("Bypassing login for trim02");
//            return;
//        }
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        response.completeOnTimeout(false, configVar.kickTimeout, TimeUnit.SECONDS);
        connectingPlayers.put(playerUUID, response);

        Audience audience = connection.getAudience();
        logger.info("Response: {}", response);
        audience.showDialog(dialog);
        logger.info("Displayed login dialog to player {}", connection.getProfile().getName());

        if (!response.join()) {
            audience.closeDialog();

            connection.disconnect(Component.text("Login failure"));
            logger.info("Player {} failed to log in", connection.getProfile().getName());
        }
        logger.info("Player {} has logged in or timed out", connection.getProfile().getName());
        connectingPlayers.remove(playerUUID);



    }
    @EventHandler
    void onHandleDialog(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerConfigurationConnection configurationConnection)) {
            return;
        }

        UUID playerUUID = configurationConnection.getProfile().getId();
        if (playerUUID == null) {
            logger.error("Player UUID is null");
            return;
        }

        Key dialogKey = event.getIdentifier();
        logger.info("Player {} clicked dialog with key {}", configurationConnection.getProfile().getName(), dialogKey);

        DialogResponseView responseView = event.getDialogResponseView();
        logger.info("Processing login dialog for player {}", configurationConnection.getProfile().getName());
        logger.info("DialogResponseView: {}", responseView);
        if(responseView == null) {
            logger.error("DialogResponseView is null for player {}", playerUUID);
            logger.warn("They will be disconnected");
            setConnectionResult(playerUUID, false);
            return;
        }
        if (dialogKey.equals(Key.key(DIALOG_NAMESPACE, "exit_login"))) {
            configurationConnection.getAudience().closeDialog();
            configurationConnection.disconnect(Component.text("Disconnected from server"));
            setConnectionResult(playerUUID, false);
            logger.info("Player {} has exited login dialog", configurationConnection.getProfile().getName());
            return;
        }
        if(!dialogKey.equals(Key.key(DIALOG_NAMESPACE, "submit_login"))) {
            setConnectionResult(playerUUID, false);
            return;
        }
        String passwordInput = responseView.getText("password_input");
        if (passwordInput.isEmpty()) {
            logger.error("Password input is null for player {}", playerUUID);
            logger.warn("They will be disconnected");
            configurationConnection.disconnect(Component.text(configVar.noPassword));
            setConnectionResult(playerUUID, false);
            return;
        }
        if (passwordInput.equals(configVar.serverPassword)) {
            setConnectionResult(playerUUID, true);
            logger.info("Player {} has logged in", configurationConnection.getProfile().getName());
        } else {
            configurationConnection.getAudience().closeDialog();
            configurationConnection.disconnect(Component.text(configVar.wrongPassword));
            setConnectionResult(playerUUID, false);
            logger.info("Player {} provided wrong password", configurationConnection.getProfile().getName());
        }






    }

    private void setConnectionResult(UUID playerUUID, boolean value) {
        CompletableFuture<Boolean> response = connectingPlayers.get(playerUUID);
        if (response != null) {
            response.complete(value);
        }
    }


}
