package net.trim02.loginPassword;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.inject.Inject;
import com.technicjelle.UpdateChecker;
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
import java.util.concurrent.TimeUnit;


@Plugin(id = "loginpassword", name = "loginPassword", version = BuildConstants.VERSION, authors = {
        "trim02" }, dependencies = {
                @Dependency(id = "luckperms", optional = true)
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
        public static String configVersion;

    }

    public void initConfigOld() {
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

    public void getTomlConfig(Path configFile) {
        if (Files.notExists(dataDirectory)) {
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // final Path configFile = dataDirectory.resolve("config.toml");
        if (Files.notExists(configFile)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.toml")) {
                Files.copy(stream, configFile);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfig config = FileConfig.of(configFile);
        config.load();
        configVar.loginServer = config.get("core.loginServer");
        configVar.hubServer = config.get("core.hubServer");
        configVar.serverPassword = config.get("core.serverPassword");
        configVar.oneTimeLogin = config.get("core.oneTimeLogin");
        configVar.pluginGrantsBypass = config.get("core.bypass.pluginGrantsBypass");
        configVar.disableLoginCommandOnBypass = config.get("core.bypass.disableLoginCommandOnBypass");
        configVar.bypassNode = config.get("core.bypass.bypassNode");
        configVar.bypassMethod = config.get("core.bypass.methods.bypassMethod");
        configVar.bypassGroup = config.get("core.bypass.methods.bypassGroup");
        configVar.kickMessage = config.get("core.kick.kickMessage");
        configVar.kickTimeout = Long.getLong(config.get("core.kick.kickTimeout").toString());
        configVar.noPassword = config.get("core.messages.noPassword");
        configVar.wrongPassword = config.get("core.messages.wrongPassword");
        configVar.loginCommandNegated = config.get("misc.loginCommandGrantedToEveryone");
        configVar.loginCommandNode = config.get("misc.loginCommandNode");
        configVar.configVersion = config.get("misc.configVersion");
        

    }


    public void initConfig() throws IOException {
        if (Files.notExists(dataDirectory)) {
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final Path configFile = dataDirectory.resolve("config.toml");
        if (Files.notExists(configFile)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.toml")) {
                Files.copy(stream, configFile);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Check if the config file is empty
        if (Files.size(configFile) == 0) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.toml")) {
                Files.copy(stream, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        getTomlConfig(configFile);
        // Check if local config file version value is different from plugin version
        if(!configVar.configVersion.equals(BuildConstants.VERSION)){
            logger.warn("Config file version is different from plugin version. Migrating config file to new version.");
            migrateConfigVersion();
        }
        // Check if old yaml config file exists
        if (Files.exists(dataDirectory.resolve("config.yml"))) {
            logger.warn("Old config file found. Migrating to new config file format.");
            migrateYamlToToml();
            initConfig();
        }
    }

    public void migrateConfigVersion() {
        Path templateConfigFile = null;
        try {
            templateConfigFile = Files.createTempFile(dataDirectory, "configTemp", ".toml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream("config.toml");
       try {
           Files.copy(templateStream, templateConfigFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
       Path configFile = dataDirectory.resolve("config.toml");

       FileConfig templateConfig = FileConfig.of(templateConfigFile);
       FileConfig config = FileConfig.of(configFile);
       templateConfig.load();
    //    System.err.println("Template Config values:");
    //    System.err.println("core.loginServer: " + templateConfig.get("core.loginServer"));
    //    System.err.println("core.hubServer: " + templateConfig.get("core.hubServer"));
    //    System.err.println("core.serverPassword: " + templateConfig.get("core.serverPassword"));
    //    System.err.println("core.oneTimeLogin: " + templateConfig.get("core.oneTimeLogin"));
    //    System.err.println("core.bypass.pluginGrantsBypass: " + templateConfig.get("core.bypass.pluginGrantsBypass"));
    //    System.err.println("core.bypass.disableLoginCommandOnBypass: " + templateConfig.get("core.bypass.disableLoginCommandOnBypass"));
    //    System.err.println("core.bypass.bypassNode: " + templateConfig.get("core.bypass.bypassNode"));
    //    System.err.println("core.bypass.methods.bypassMethod: " + templateConfig.get("core.bypass.methods.bypassMethod"));
    //    System.err.println("core.bypass.methods.bypassGroup: " + templateConfig.get("core.bypass.methods.bypassGroup"));
    //    System.err.println("core.kick.kickMessage: " + templateConfig.get("core.kick.kickMessage"));
    //    System.err.println("core.kick.kickTimeout: " + templateConfig.get("core.kick.kickTimeout"));
    //    System.err.println("messages.noPassword: " + templateConfig.get("messages.noPassword"));
    //    System.err.println("messages.wrongPassword: " + templateConfig.get("messages.wrongPassword"));
    //    System.err.println("misc.loginCommandGrantedToEveryone: " + templateConfig.get("misc.loginCommandGrantedToEveryone"));
    //    System.err.println("misc.loginCommandNode: " + templateConfig.get("misc.loginCommandNode"));
    //    System.err.println("misc.configVersion: " + templateConfig.get("misc.configVersion"));
    //    System.err.println("Build Version: " + BuildConstants.VERSION);
       config.load();
    //    System.err.println("Local Config values:");
    //    System.err.println("core.loginServer: " + config.get("core.loginServer"));
    //    System.err.println("core.hubServer: " + config.get("core.hubServer"));
    //    System.err.println("core.serverPassword: " + config.get("core.serverPassword"));
    //    System.err.println("core.oneTimeLogin: " + config.get("core.oneTimeLogin"));
    //    System.err.println("core.bypass.pluginGrantsBypass: " + config.get("core.bypass.pluginGrantsBypass"));
    //    System.err.println("core.bypass.disableLoginCommandOnBypass: " + config.get("core.bypass.disableLoginCommandOnBypass"));
    //    System.err.println("core.bypass.bypassNode: " + config.get("core.bypass.bypassNode"));
    //    System.err.println("core.bypass.methods.bypassMethod: " + config.get("core.bypass.methods.bypassMethod"));
    //    System.err.println("core.bypass.methods.bypassGroup: " + config.get("core.bypass.methods.bypassGroup"));
    //    System.err.println("core.kick.kickMessage: " + config.get("core.kick.kickMessage"));
    //    System.err.println("core.kick.kickTimeout: " + config.get("core.kick.kickTimeout"));
    //    System.err.println("messages.noPassword: " + config.get("messages.noPassword"));
    //    System.err.println("messages.wrongPassword: " + config.get("messages.wrongPassword"));
    //    System.err.println("misc.loginCommandGrantedToEveryone: " + config.get("misc.loginCommandGrantedToEveryone"));
    //    System.err.println("misc.loginCommandNode: " + config.get("misc.loginCommandNode"));
    //    System.err.println("misc.configVersion: " + config.get("misc.configVersion"));
    //    System.err.println("Build Version: " + BuildConstants.VERSION);
       templateConfig.set("core.loginServer", config.get("core.loginServer"));
       templateConfig.set("core.hubServer", config.get("core.hubServer"));
       templateConfig.set("core.serverPassword", config.get("core.serverPassword"));
       templateConfig.set("core.oneTimeLogin", config.get("core.oneTimeLogin"));
       templateConfig.set("core.bypass.pluginGrantsBypass", config.get("core.bypass.pluginGrantsBypass"));
       templateConfig.set("core.bypass.disableLoginCommandOnBypass", config.get("core.bypass.disableLoginCommandOnBypass"));
       templateConfig.set("core.bypass.bypassNode", config.get("core.bypass.bypassNode"));
       templateConfig.set("core.bypass.methods.bypassMethod", config.get("core.bypass.methods.bypassMethod"));
       templateConfig.set("core.bypass.methods.bypassGroup", config.get("core.bypass.methods.bypassGroup"));
       templateConfig.set("core.kick.kickMessage", config.get("core.kick.kickMessage"));
       templateConfig.set("core.kick.kickTimeout", config.get("core.kick.kickTimeout"));
       templateConfig.set("messages.noPassword", config.get("messages.noPassword"));
       templateConfig.set("messages.wrongPassword", config.get("messages.wrongPassword"));
       templateConfig.set("misc.loginCommandGrantedToEveryone", config.get("misc.loginCommandGrantedToEveryone"));
       templateConfig.set("misc.loginCommandNode", config.get("misc.loginCommandNode"));

    //    System.err.println("New Config values:");
    //    System.err.println("core.loginServer: " + templateConfig.get("core.loginServer"));
    //      System.err.println("core.hubServer: " + templateConfig.get("core.hubServer"));
    //      System.err.println("core.serverPassword: " + templateConfig.get("core.serverPassword"));
    //         System.err.println("core.oneTimeLogin: " + templateConfig.get("core.oneTimeLogin"));
    //         System.err.println("core.bypass.pluginGrantsBypass: " + templateConfig.get("core.bypass.pluginGrantsBypass"));
    //         System.err.println("core.bypass.disableLoginCommandOnBypass: " + templateConfig.get("core.bypass.disableLoginCommandOnBypass"));
    //         System.err.println("core.bypass.bypassNode: " + templateConfig.get("core.bypass.bypassNode"));
    //         System.err.println("core.bypass.methods.bypassMethod: " + templateConfig.get("core.bypass.methods.bypassMethod"));
    //         System.err.println("core.bypass.methods.bypassGroup: " + templateConfig.get("core.bypass.methods.bypassGroup"));
    //         System.err.println("core.kick.kickMessage: " + templateConfig.get("core.kick.kickMessage"));
    //         System.err.println("core.kick.kickTimeout: " + templateConfig.get("core.kick.kickTimeout"));
    //         System.err.println("messages.noPassword: " + templateConfig.get("messages.noPassword"));
    //         System.err.println("messages.wrongPassword: " + templateConfig.get("messages.wrongPassword"));
    //         System.err.println("misc.loginCommandGrantedToEveryone: " + templateConfig.get("misc.loginCommandGrantedToEveryone"));
    //         System.err.println("misc.loginCommandNode: " + templateConfig.get("misc.loginCommandNode"));
    //         System.err.println("misc.configVersion: " + templateConfig.get("misc.configVersion"));
    //         System.err.println("Build Version: " + BuildConstants.VERSION);




       templateConfig.save();
       templateConfig.close();
       try {
            Files.copy(templateConfigFile, configFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.delete(templateConfigFile);
       }
         catch (IOException e) {
                throw new RuntimeException(e);
         }



    }
    public void migrateYamlToToml() {
        Path yamlFile = dataDirectory.resolve("config.yml");
        Path tomlFile = dataDirectory.resolve("config.toml");
        FileConfig config = FileConfig.of(tomlFile);
        config.load();
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(yamlFile).build();
        final CommentedConfigurationNode node;
        try {
            node = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        config.set("core.loginServer",node.node("loginServer").getString());
        config.set("core.hubServer",node.node("hubServer").getString());
        config.set("core.serverPassword",node.node("serverPassword").getString());
        config.set("core.oneTimeLogin",node.node("oneTimeLogin").getBoolean());
        config.set("core.bypass.pluginGrantsBypass",node.node("pluginGrantsBypass").getBoolean());
        config.set("core.bypass.disableLoginCommandOnBypass",node.node("disableLoginCommandOnBypass").getBoolean());
        config.set("core.bypass.bypassNode",node.node("bypassNode").getString());
        config.set("core.bypass.methods.bypassMethod",node.node("bypassMethod").getString());
        config.set("core.bypass.methods.bypassGroup",node.node("bypassGroup").getString());
        config.set("core.kick.kickMessage",node.node("kickMessage").getString());
        config.set("core.kick.kickTimeout",node.node("kickTimeout").getLong());
        config.set("messages.noPassword",node.node("noPassword").getString());
        config.set("messages.wrongPassword",node.node("wrongPassword").getString());
        config.set("misc.loginCommandGrantedToEveryone",node.node("loginCommandGrantedToEveryone").getBoolean());
        config.set("misc.loginCommandNode",node.node("loginCommandNode").getString());
        
        config.save();
        config.close();
        try {
            Files.delete(yamlFile);
            logger.info("Old config file deleted.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }









    public Boolean setConfigValue(String key, Object value) {
        FileConfig config = FileConfig.of(dataDirectory.resolve("config.toml"));
        config.load();
        config.set(key, value);
        config.save();
        config.close();
        return true;
    }
    

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        
        UpdateChecker updateChecker = new UpdateChecker("trim02", "loginPassword", BuildConstants.VERSION);
        server.getScheduler().buildTask(this, () -> {
            try {
                updateChecker.check();
                if (updateChecker.isUpdateAvailable()) {

                    var updateMessage = """
                            A new version is available: %s -> %s. Download the new version here:
                            modrinth: https://modrinth.com/plugin/loginpassword
                            Hangar: https://hangar.papermc.io/trim02/loginPassword
                            GitHub: %s
                            """.formatted(updateChecker.getCurrentVersion(), updateChecker.getLatestVersion(), updateChecker.getUpdateUrl());

                    logger.warn(updateMessage);
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }).repeat(7, TimeUnit.DAYS).schedule();

        
        try {
            initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        



        server.getEventManager().register(this, new PlayerConnection(server, this));
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("login").plugin(this).build();

        if (server.getPluginManager().isLoaded("luckperms")) {
            logger.debug("luckperms found!");
            SimpleCommand loginCommand = new LoginCommandLuckPerms(server, logger);
            commandManager.register(commandMeta, loginCommand);
        } else {
            if(configVar.pluginGrantsBypass.equals(true) && configVar.oneTimeLogin.equals(true)){
                logger.warn("pluginGrantsBypass is set to true but LuckPerms is not found. Please disable pluginGrantsBypass in the config file, as this setting will not work without LuckPerms. Bypass permissions must be granted manually.");
            }
            SimpleCommand loginCommand = new LoginCommand(server, logger);
            commandManager.register(commandMeta, loginCommand);

        }
        logger.info("Plugin ready!");
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        try {
            initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("Config reloaded!");
    }
}
