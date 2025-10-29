package net.trim02.loginPassword;

import com.technicjelle.UpdateChecker;
import io.papermc.paper.command.brigadier.BasicCommand;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.interfaces.loginPassword;
import net.trim02.loginPassword.paper.AdminCommand;
import net.trim02.loginPassword.paper.DialogLogin;
import net.trim02.loginPassword.paper.PaperComms;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public class loginPasswordPaper extends JavaPlugin implements loginPassword<Server> {


    public Logger logger;
    public Server server;
    private final Path dataDirectory;
    private final Config config;



    public loginPasswordPaper() {

        this.logger = this.getInterLogger();
        this.server = this.getInterServer();
        this.dataDirectory = this.getDataFolder().toPath();
        this.config = new Config(logger, dataDirectory);


    }

    @Override
    public void onEnable() {

        UpdateChecker updateChecker = new UpdateChecker("trim02", "LoginPassword", BuildConstants.VERSION);
        server.getScheduler().runTaskTimerAsynchronously(this, task -> {
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
        }, 1200, 12096000);





        try {

            config.initConfig();
        } catch (Exception e) {
            logger.error("Failed to initialize config: ", e);
            throw new RuntimeException(e);
        }
        try {
            this.getServer().getPluginManager().registerEvents(new DialogLogin(this, server, logger), this);
            new BypassList(logger, dataDirectory);
            BypassList.loadBypassList();

            BasicCommand adminCommand = new AdminCommand(this, server, logger, config);
            registerCommand("loginpassword", adminCommand);


//            this.getCommand("login").setExecutor(new LoginCommand(this.server, this.logger));
        } catch (Exception e) {
            logger.error("Failed to register events or commands: ", e);
            throw new RuntimeException(e);
        }



    }




    @NotNull
    public String getPlatformName() {
        return "Paper";
    }

    @Override
    public Logger getInterLogger() {
        return this.getComponentLogger();
    }

    @Override
    public Path getInterDataFolder() {
        return dataDirectory;
    }

    @Override
    public Server getInterServer() {
        return this.getServer();
    }

}
