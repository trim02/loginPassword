package net.trim02.loginPassword;


import com.google.inject.Inject;
import com.technicjelle.UpdateChecker;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.trim02.loginPassword.Config.configVar;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.interfaces.loginPassword;
import net.trim02.loginPassword.velocity.AdminCommand;
import net.trim02.loginPassword.velocity.LoginCommand;
import net.trim02.loginPassword.velocity.LoginCommandLuckPerms;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "loginpassword", name = "loginPassword", version = BuildConstants.VERSION, authors = {
        "trim02" }, dependencies = {
                @Dependency(id = "luckperms", optional = true)
        })

public class loginPasswordVelocity implements loginPassword<ProxyServer> {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Config config;

    @Inject
    public loginPasswordVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = new Config(logger, dataDirectory);

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
            logger.info("Initializing loginPassword plugin...");
            config.initConfig();
            new BypassList(logger, dataDirectory);
            BypassList.loadBypassList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        



        server.getEventManager().register(this, new PlayerConnection(server, this, logger));
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMetaLogin = commandManager.metaBuilder("login").plugin(this).build();
        CommandMeta commandMetaAdmin = commandManager.metaBuilder("loginpasswordvelocity").plugin(this).build();

        if (server.getPluginManager().isLoaded("luckperms")) {
            debugMessage("luckperms found!");
            SimpleCommand loginCommand = new LoginCommandLuckPerms(server, logger);
            commandManager.register(commandMetaLogin, loginCommand);
        } else {
            if(configVar.pluginGrantsBypass.equals(true) && configVar.oneTimeLogin.equals(true)){
                logger.warn("pluginGrantsBypass is set to true but LuckPerms is not found. Bypass permissions must be granted manually.");
            }
            SimpleCommand loginCommand = new LoginCommand(server, logger);
            commandManager.register(commandMetaLogin, loginCommand);

        }
        SimpleCommand adminCommand = new AdminCommand(this, server, logger, config);
        commandManager.register(commandMetaAdmin, adminCommand);
        logger.info("Plugin ready!");
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        try {
           config.initConfig();
           BypassList.loadBypassList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("Config reloaded!");
    }



    @Override
    public String getPlatformName() {
        return "Velocity";
    }

    @Override
    public Logger getInterLogger() {
        return this.logger;
    }

    public Boolean isDebugModeEnabled() {
        return configVar.debugMode;
    }

    @Override
    public ProxyServer getInterServer() {
        return this.server;
    }

    @Override
    public Path getInterDataFolder() {
        return dataDirectory;
    }
}
