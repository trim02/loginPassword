package net.trim02.loginPassword.paper;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.trim02.loginPassword.Config.configVar;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.common.LuckPermsHook;
import net.trim02.loginPassword.common.ViaVersionHook;
import net.trim02.loginPassword.loginPasswordPaper;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.trim02.loginPassword.BuildConstants.DIALOG_NAMESPACE;

public class DialogLogin implements Listener {
    private static boolean isViaLoaded;
    private static boolean isLuckPermsLoaded;
    private final Logger logger;
    private final Map<UUID, CompletableFuture<Boolean>> connectingPlayers = new ConcurrentHashMap<>();
    private loginPasswordPaper plugin;
    private Server server;

    public DialogLogin(loginPasswordPaper plugin, Server server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;

        isLuckPermsLoaded = server.getPluginManager().getPlugin("LuckPerms") != null;
        isViaLoaded = server.getPluginManager().getPlugin("ViaVersion") != null;
    }


    @EventHandler
    void onPlayerConfiguration(AsyncPlayerConnectionConfigureEvent event) {
        if (!configVar.pluginEnabled) {
            return;
        }


        Dialog dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key(DIALOG_NAMESPACE, "login_dialog"));

        if (dialog == null) {

            event.getConnection().disconnect(Component.text("Internal server error"));
            logger.error("Failed to load dialog for player {}", event.getConnection().getProfile().getName());
            return;
        }

        PlayerConfigurationConnection connection = event.getConnection();
        UUID playerUUID = connection.getProfile().getId();



        if (playerUUID == null) {
            event.getConnection().disconnect(Component.text("Internal server error"));
            logger.error("Player {} UUID is null", connection.getProfile().getName());
            return;
        }
        if (configVar.oneTimeLogin) {
            if (isLuckPermsLoaded) {
                var user = LuckPermsHook.api.getUserManager().getUser(playerUUID);
                assert user != null;
                if (user.getCachedData().getPermissionData().checkPermission(configVar.bypassNode).asBoolean()) {

                    return;
                }
            }
            if (BypassList.inBypassList(playerUUID)) {

                return;
            }
        }

        if (isViaLoaded) {
            if (ViaVersionHook.api.getPlayerVersion(playerUUID) < 771) {
                logger.info("Player {} attempted to join using version: {}, which does not support the dialog api required to show the login prompt. They have been disconnected. They must update or be granted bypass permissions using LuckPerms or added to the bypass list using /loginpassword add {}", connection.getProfile().getName(), ViaVersionHook.api.getPlayerProtocolVersion(playerUUID), playerUUID);
                connection.disconnect(Component.text("Unsupported Minecraft version. Please use 1.21.6 or higher."));
                return;
            }
        }
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        response.completeOnTimeout(false, configVar.kickTimeout, TimeUnit.SECONDS);
        connectingPlayers.put(playerUUID, response);

        Audience audience = connection.getAudience();
        audience.showDialog(dialog);


        if (!response.join()) {
            audience.closeDialog();
            connection.disconnect(Component.text("Login failure"));
            logger.info("Player {} failed to log in", connection.getProfile().getName());
        }
        if (response.isDone() && response.join() && (configVar.oneTimeLogin && configVar.pluginGrantsBypass)) {
            if (isLuckPermsLoaded) {
                if (configVar.bypassMethod.equalsIgnoreCase("user")) {
                    LuckPermsHook.addNode(playerUUID, configVar.bypassNode);

                } else if (configVar.bypassMethod.equalsIgnoreCase("group")) {
                    LuckPermsHook.addNode(playerUUID, "group." + configVar.bypassGroup);
                } else {
                    logger.error("An error occurred while granting the user {} bypass permission.", connection.getProfile().getName());
                }
            } else {
                BypassList.addBypassEntry(String.valueOf(playerUUID));
            }
            logger.info("Player {} successfully logged in", connection.getProfile().getName());
        }
        connectingPlayers.remove(playerUUID);


    }


    @EventHandler
    void onHandleDialog(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerConfigurationConnection configurationConnection)) {
            return;
        }

        UUID playerUUID = configurationConnection.getProfile().getId();
        if (playerUUID == null) {

            configurationConnection.disconnect(Component.text("Internal server error"));
            logger.error("Player {} UUID is null", configurationConnection.getProfile().getName());
            return;
        }

        Key dialogKey = event.getIdentifier();
        DialogResponseView responseView = event.getDialogResponseView();

        if (responseView == null) {
            configurationConnection.disconnect(Component.text("Internal server error"));
            logger.error("DialogResponseView is null for player {}", playerUUID);
            setConnectionResult(playerUUID, false);
            return;
        }
        if (dialogKey.equals(Key.key(DIALOG_NAMESPACE, "exit_login"))) {
            configurationConnection.getAudience().closeDialog();
            configurationConnection.disconnect(Component.text("Disconnected from server"));
            setConnectionResult(playerUUID, false);

            return;
        }
        if (!dialogKey.equals(Key.key(DIALOG_NAMESPACE, "submit_login"))) {
            configurationConnection.disconnect(Component.text("Internal server error"));
            setConnectionResult(playerUUID, false);
            return;
        }
        String passwordInput = responseView.getText("password_input");
        if (passwordInput.isEmpty()) {
            configurationConnection.disconnect(Component.text(configVar.noPassword));
            setConnectionResult(playerUUID, false);
            return;
        }
        if (passwordInput.equals(configVar.serverPassword)) {
            setConnectionResult(playerUUID, true);
        } else {
            configurationConnection.getAudience().closeDialog();
            configurationConnection.disconnect(Component.text(configVar.wrongPassword));
            setConnectionResult(playerUUID, false);
        }

    }


    private void setConnectionResult(UUID playerUUID, boolean value) {
        CompletableFuture<Boolean> response = connectingPlayers.get(playerUUID);
        if (response != null) {
            response.complete(value);
        }
    }

    @EventHandler
    void onPlayerConnectionClose(PlayerConnectionCloseEvent event){
        connectingPlayers.remove(event.getPlayerUniqueId());
    }

}