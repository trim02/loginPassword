package net.trim02.loginPassword;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    protected final ProxyServer server;
    protected final loginPassword plugin;
    protected final Logger logger;
    protected final Path dataDirectory;
    protected final ConfigSpec defaultSpec;

    public Config(loginPassword plugin, ProxyServer server, Logger logger, Path dataDirectory) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.defaultSpec = defaultConfig();
        com.electronwill.nightconfig.core.Config.setInsertionOrderPreserved(true);


    }

    static class configVar {
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
        public static Integer kickTimeout;
        public static String noPassword;
        public static String wrongPassword;
        public static String welcomeMessage;
        public static Boolean loginCommandNegated;
        public static String loginCommandNode;
        public static String configVersion;
        public static Boolean pluginEnabled;
    }
    public ConfigSpec defaultConfig() {
        ConfigSpec spec = new ConfigSpec();

        spec.define("core.loginServer", "login");
        spec.define("core.hubServer", "hub");
        spec.define("core.serverPassword", "1234");
        spec.define("core.oneTimeLogin", false);
        spec.define("core.bypass.pluginGrantsBypass", true);
        spec.define("core.bypass.disableLoginCommandOnBypass", true);
        spec.define("core.bypass.bypassNode", "loginpassword.bypass");
        spec.define("core.bypass.methods.bypassMethod", "user");
        spec.define("core.bypass.methods.bypassGroup", "default");
        spec.define("core.kick.kickMessage", "You were kicked for failing to provide the password after 30 seconds");
        spec.define("core.kick.kickTimeout", 30);
        spec.define("messages.noPassword", "Please provide a password. It can be found on Discord");
        spec.define("messages.wrongPassword", "Wrong Password.");
        spec.define("messages.welcomeMessage", "Please type /login <password> to log in.");
        spec.define("misc.loginCommandGrantedToEveryone", true);
        spec.define("misc.loginCommandNode", "loginpassword.login");
        spec.define("misc.configVersion", BuildConstants.VERSION);
        spec.define("misc.pluginEnabled", true);

        return spec;

    }

    public Object isConfigCorrect(String key, Object value) {
       return defaultSpec.correct(key, value);


    }
    public void validateConfig(Path configFile) {
        FileConfig config = FileConfig.of(configFile);
        config.load();
        defaultSpec.correct(config);
        config.save();
        config.close();

    }

    public void initConfig() throws IOException {
        if (Files.notExists(dataDirectory)) {
            logger.info("Data directory does not exist. Creating it.");
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
        validateConfig(configFile);
        getTomlConfig(configFile);
        // Check if local config file version value is different from plugin version
        if(!configVar.configVersion.equals(BuildConstants.VERSION)){
            // logger.warn("Config file version is different from plugin version. Migrating config file to new version.");
            migrateConfigVersion();
            initConfig();
        }
        // Check if old yaml config file exists
        if (Files.exists(dataDirectory.resolve("config.yml"))) {
            logger.warn("Old config file found. Migrating to new config file format.");
            migrateYamlToToml();
            initConfig();
        }
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
        configVar.kickTimeout = config.get("core.kick.kickTimeout");
        configVar.noPassword = config.get("messages.noPassword");
        configVar.wrongPassword = config.get("messages.wrongPassword");
        configVar.welcomeMessage = config.get("messages.welcomeMessage");
        configVar.loginCommandNegated = config.get("misc.loginCommandGrantedToEveryone");
        configVar.loginCommandNode = config.get("misc.loginCommandNode");
        configVar.configVersion = config.get("misc.configVersion");
        configVar.pluginEnabled = config.get("misc.pluginEnabled");
        config.close();

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
        templateConfig.set("messages.welcomeMessage", config.get("messages.welcomeMessage"));
        templateConfig.set("misc.loginCommandGrantedToEveryone", config.get("misc.loginCommandGrantedToEveryone"));
        templateConfig.set("misc.loginCommandNode", config.get("misc.loginCommandNode"));
        templateConfig.set("misc.pluginEnabled", config.get("misc.pluginEnabled"));
        templateConfig.set("core.loginServer", isConfigCorrect("core.loginServer", config.get("core.loginServer")));
        templateConfig.set("core.hubServer", isConfigCorrect("core.hubServer", config.get("core.hubServer")));
        templateConfig.set("core.serverPassword", isConfigCorrect("core.serverPassword", config.get("core.serverPassword")));
        templateConfig.set("core.oneTimeLogin", isConfigCorrect("core.oneTimeLogin", config.get("core.oneTimeLogin")));
        templateConfig.set("core.bypass.pluginGrantsBypass", isConfigCorrect("core.bypass.pluginGrantsBypass", config.get("core.bypass.pluginGrantsBypass")));
        templateConfig.set("core.bypass.disableLoginCommandOnBypass", isConfigCorrect("core.bypass.disableLoginCommandOnBypass", config.get("core.bypass.disableLoginCommandOnBypass")));
        templateConfig.set("core.bypass.bypassNode", isConfigCorrect("core.bypass.bypassNode", config.get("core.bypass.bypassNode")));
        templateConfig.set("core.bypass.methods.bypassMethod", isConfigCorrect("core.bypass.methods.bypassMethod", config.get("core.bypass.methods.bypassMethod")));
        templateConfig.set("core.bypass.methods.bypassGroup", isConfigCorrect("core.bypass.methods.bypassGroup", config.get("core.bypass.methods.bypassGroup")));
        templateConfig.set("core.kick.kickMessage", isConfigCorrect("core.kick.kickMessage", config.get("core.kick.kickMessage")));
        templateConfig.set("core.kick.kickTimeout", isConfigCorrect("core.kick.kickTimeout", config.get("core.kick.kickTimeout")));
        templateConfig.set("messages.noPassword", isConfigCorrect("messages.noPassword", config.get("messages.noPassword")));
        templateConfig.set("messages.wrongPassword", isConfigCorrect("messages.wrongPassword", config.get("messages.wrongPassword")));
        templateConfig.set("messages.welcomeMessage", isConfigCorrect("messages.welcomeMessage", config.get("messages.welcomeMessage")));
        templateConfig.set("misc.loginCommandGrantedToEveryone", isConfigCorrect("misc.loginCommandGrantedToEveryone", config.get("misc.loginCommandGrantedToEveryone")));
        templateConfig.set("misc.loginCommandNode", isConfigCorrect("misc.loginCommandNode", config.get("misc.loginCommandNode")));
        templateConfig.set("misc.pluginEnabled", isConfigCorrect("misc.pluginEnabled", config.get("misc.pluginEnabled")));

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
        config.close();
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
    public void setConfigValue(String key, Object value) {
        FileConfig config = FileConfig.of(dataDirectory.resolve("config.toml"));
        config.load();
        config.set(key, isConfigCorrect(key, value));
        config.save();
        config.close();
        try {
            initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadConfig() {
        try {
            initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void togglePlugin() {
        if(configVar.pluginEnabled) {
            setConfigValue("misc.pluginEnabled", false);
        } else {
            setConfigValue("misc.pluginEnabled", true);
        }

    }





}
