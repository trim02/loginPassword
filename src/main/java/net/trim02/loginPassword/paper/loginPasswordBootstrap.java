package net.trim02.loginPassword.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trim02.loginPassword.Config;
import net.trim02.loginPassword.Config.configVar;
import org.slf4j.Logger;

import java.util.List;

import static net.trim02.loginPassword.BuildConstants.DIALOG_NAMESPACE;

public class loginPasswordBootstrap implements PluginBootstrap {

//    private static final String DIALOG_NAMESPACE = "loginpassword";
//    private final Config config;
//
//
//    public loginPasswordBootstrap(Config config) {
//        this.config = new Config(log);
//
//    }

    @Override
    public void bootstrap(BootstrapContext context) {
        Logger logger = context.getLogger();
        Config config = new Config(logger, context.getDataDirectory());
        logger.info("Bootstrapping plugin...");
        try {
            logger.info("Initializing config...");
            config.initConfig();
        } catch (Exception e) {

            logger.error("Failed to initialize config: ", e);
            throw new RuntimeException(e);
        }


        context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose(),
                event -> event.registry().register(
                        DialogKeys.create(Key.key(DIALOG_NAMESPACE, "login_dialog")),
                        builder -> builder.base(
                                DialogBase.builder(
//                                        Component.text(configVar.welcomeMessage)
                                        MiniMessage.miniMessage().deserialize(configVar.welcomeMessage)
                                ).canCloseWithEscape(false).inputs(
                                        List.of(
                                                DialogInput.text("password_input", Component.text("password")).build()
                                        )

                                ).build()
                        ).type(DialogType.confirmation(
                                        ActionButton.create(
                                                Component.text("Login"),
                                                Component.text("Click to submit"),
                                                100,
                                                DialogAction.customClick(Key.key(DIALOG_NAMESPACE, "submit_login"), null)
                                        ),
                                        ActionButton.create(
                                                Component.text("Exit"),
                                                Component.text("Click to exit"),
                                                100,
                                                DialogAction.customClick(Key.key(DIALOG_NAMESPACE, "exit_login"), null)
                                        )
                                )
                        )
                )

        );

    }
}
