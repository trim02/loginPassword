package net.trim02.loginPassword;


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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import net.trim02.loginPassword.Config.configVar;

@Plugin(id = "loginpassword", name = "loginPassword", version = BuildConstants.VERSION, authors = {
        "trim02" }, dependencies = {
                @Dependency(id = "luckperms", optional = true)
        })
public class loginPassword {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Config config;

    @Inject
    public loginPassword(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = new Config(this, server, logger, dataDirectory);

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

                    logger.info(updateMessage);
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }).repeat(7, TimeUnit.DAYS).schedule();

        
        try {
            config.initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        



        server.getEventManager().register(this, new PlayerConnection(server, this));
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMetaLogin = commandManager.metaBuilder("login").plugin(this).build();
        CommandMeta commandMetaAdmin = commandManager.metaBuilder("loginpassword").plugin(this).build();

        if (server.getPluginManager().isLoaded("luckperms")) {
            logger.debug("luckperms found!");
            SimpleCommand loginCommand = new LoginCommandLuckPerms(server, logger);
            commandManager.register(commandMetaLogin, loginCommand);
        } else {
            if(configVar.pluginGrantsBypass.equals(true) && configVar.oneTimeLogin.equals(true)){
                logger.warn("pluginGrantsBypass is set to true but LuckPerms is not found. Please disable pluginGrantsBypass in the config file, as this setting will not work without LuckPerms. Bypass permissions must be granted manually.");
            }
            SimpleCommand loginCommand = new LoginCommand(server, logger);
            commandManager.register(commandMetaLogin, loginCommand);

        }
        SimpleCommand adminCommand = new AdminCommand(server, logger, config);
        commandManager.register(commandMetaAdmin, adminCommand);
        logger.info("Plugin ready!");
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        try {
           config.initConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("Config reloaded!");
    }
}
