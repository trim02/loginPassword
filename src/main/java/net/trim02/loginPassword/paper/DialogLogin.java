package net.trim02.loginPassword.paper;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trim02.loginPassword.Config.configVar;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.common.LuckPermsHook;
import net.trim02.loginPassword.common.ViaVersionHook;
import net.trim02.loginPassword.loginPasswordPaper;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

import java.util.List;
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
    private final Map<UUID, CompletableFuture<LoginResult>> connectingPlayers = new ConcurrentHashMap<>();
    private loginPasswordPaper plugin;
    private Server server;
    private Dialog loginDialog;

    public DialogLogin(loginPasswordPaper plugin, Server server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
        this.loginDialog = createDialog();
        isLuckPermsLoaded = server.getPluginManager().getPlugin("LuckPerms") != null;
        isViaLoaded = server.getPluginManager().getPlugin("ViaVersion") != null;
    }

    enum LoginResult {
        SUCCESS,
        FAILURE,
        TIMEOUT,
        ERROR,
        EXIT
    }

    Dialog createDialog() {
        Dialog dialog_login = Dialog.create(
                builder -> builder.empty().base(
                        DialogBase.builder(
                                        MiniMessage.miniMessage().deserialize(configVar.welcomeMessage)
                                ).
                                canCloseWithEscape(false).
                                inputs(
                                        List.of(
                                                DialogInput.text("password_input", Component.text("password")).build()
                                        )
                                ).build()
                ).type(DialogType.confirmation(
                                ActionButton.create(
                                        Component.text("Login"),
                                        Component.text("Click to Submit"),
                                        200,
                                        DialogAction.customClick(Key.key(DIALOG_NAMESPACE, "submit_login"), null)
                                ),
                                ActionButton.create(
                                        Component.text("Exit"),
                                        Component.text("Click to Exit"),
                                        200,
                                        DialogAction.customClick(Key.key(DIALOG_NAMESPACE, "exit_login"), null)
                                )
                        )
                )
        );
        return dialog_login;
    }


    @EventHandler
    void onPlayerConfiguration(AsyncPlayerConnectionConfigureEvent event) {
        if (!configVar.pluginEnabled) {
            return;
        }


//        Dialog dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key(DIALOG_NAMESPACE, "login_dialog"));
        Dialog dialog = this.loginDialog;
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
                if (LuckPermsHook.loadUser(playerUUID).getCachedData().getPermissionData().checkPermission(configVar.bypassNode).asBoolean()) {
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
        CompletableFuture<LoginResult> response = new CompletableFuture<>();
        response.completeOnTimeout(LoginResult.TIMEOUT, configVar.kickTimeout, TimeUnit.SECONDS);
        connectingPlayers.put(playerUUID, response);

        Audience audience = connection.getAudience();
        audience.showDialog(dialog);

        if (response.join().equals(LoginResult.TIMEOUT)) {
            audience.closeDialog();
            connection.disconnect(Component.text(configVar.kickMessage));
        }
        if (response.join().equals(LoginResult.ERROR)){
            connection.disconnect(Component.text("Internal server error"));
            logger.error("An error occurred while processing login for player {}", connection.getProfile().getName());
        }
        if (response.join().equals(LoginResult.FAILURE)) {
            connection.disconnect(Component.text("Login failure"));
            logger.info("Player {} failed to log in", connection.getProfile().getName());
        }
        if (response.join().equals(LoginResult.EXIT)) {
            connection.disconnect(Component.text("Disconnected from server"));
        }
        if (response.isDone() && response.join().equals(LoginResult.SUCCESS) && (configVar.oneTimeLogin && configVar.pluginGrantsBypass)) {
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
            logger.error("DialogResponseView is null for player {}", playerUUID);
            setConnectionResult(playerUUID, LoginResult.ERROR);
            return;
        }
        if (dialogKey.equals(Key.key(DIALOG_NAMESPACE, "exit_login"))) {
            setConnectionResult(playerUUID, LoginResult.EXIT);
            configurationConnection.getAudience().closeDialog();
            return;
        }
        if (!dialogKey.equals(Key.key(DIALOG_NAMESPACE, "submit_login"))) {
            setConnectionResult(playerUUID, LoginResult.ERROR);
            return;
        }

        String passwordInput = responseView.getText("password_input");

        if (passwordInput.isEmpty()) {
            configurationConnection.disconnect(Component.text(configVar.noPassword));
            setConnectionResult(playerUUID, LoginResult.FAILURE);
            return;
        } else {
            for (String password : configVar.serverPassword) {
                if (passwordInput.equals(password)) {
//                    configurationConnection.getAudience().closeDialog();
                    setConnectionResult(playerUUID, LoginResult.SUCCESS);
                    return;
                }
            }
        }

        configurationConnection.getAudience().closeDialog();
        configurationConnection.disconnect(Component.text(configVar.wrongPassword));
        setConnectionResult(playerUUID, LoginResult.FAILURE);


    }


    private void setConnectionResult(UUID playerUUID, LoginResult value) {
        CompletableFuture<LoginResult> response = connectingPlayers.get(playerUUID);
        if (response != null) {
            response.complete(value);
        }
    }

    @EventHandler
    void onPlayerConnectionClose(PlayerConnectionCloseEvent event) {
        connectingPlayers.remove(event.getPlayerUniqueId());
    }

}