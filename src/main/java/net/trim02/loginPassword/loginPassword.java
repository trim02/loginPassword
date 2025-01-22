package net.trim02.loginPassword;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "loginpassword", name = "loginPassword", version = BuildConstants.VERSION, authors = {
        "trim02" }, dependencies = {
                @Dependency(id = "luckperms")
        })
public class loginPassword {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public loginPassword(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

    }

    public class configVar {
        public static String loginServer;
        public static String hubServer;
        public static String serverPassword;
        public static Boolean oneTimeLogin;
        public static String bypassNode;
        public static Boolean pluginGrantsBypass;
        public static String bypassMethod;
        public static String bypassGroup;
        public static Boolean disableLoginCommandOnBypass;
        public static String kickMessage;
        public static Long kickTimeout;
        public static String noPassword;
        public static String wrongPassword;
        public static Boolean loginCommandNegated;
        public static String loginCommandNode;

    }

    public void initConfig() {
        if (Files.notExists(dataDirectory)) {
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final Path config = dataDirectory.resolve("config.yml");
        if (Files.notExists(config)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(stream, config);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(config).build();
        final CommentedConfigurationNode node;
        try {
            node = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        configVar.loginServer = node.node("loginServer").getString();
        configVar.hubServer = node.node("hubServer").getString();
        configVar.serverPassword = node.node("serverPassword").getString();
        configVar.oneTimeLogin = node.node("oneTimeLogin").getBoolean();
        configVar.bypassMethod = node.node("bypassMethod").getString();
        configVar.bypassGroup = node.node("bypassGroup").getString();
        configVar.bypassNode = node.node("bypassNode").getString();
        configVar.pluginGrantsBypass = node.node("pluginGrantsBypass").getBoolean();
        configVar.disableLoginCommandOnBypass = node.node("disableLoginCommandOnBypass").getBoolean();
        configVar.kickMessage = node.node("kickMessage").getString();
        configVar.kickTimeout = node.node("kickTimeout").getLong();
        configVar.noPassword = node.node("noPassword").getString();
        configVar.wrongPassword = node.node("wrongPassword").getString();
        configVar.loginCommandNegated = node.node("loginCommandGrantedToEveryone").getBoolean();
        configVar.loginCommandNode = node.node("loginCommandNode").getString();

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initConfig();
        logger.info("Plugin ready!");

        server.getEventManager().register(this, new PlayerConnection(server, this));
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("login").plugin(this).build();
        SimpleCommand loginCommand = new LoginCommand(server, server.getPluginManager(), logger);
        commandManager.register(commandMeta, loginCommand);

    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        initConfig();
        logger.info("Config reloaded!");
    }
}
