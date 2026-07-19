/*
 *     loginPassword
 *     Copyright (c) 2025. trim02
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package net.trim02.loginPassword;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.trim02.loginPassword.common.BypassList;
import net.trim02.loginPassword.interfaces.loginPassword;
import net.trim02.loginPassword.paper.AdminCommand;
import net.trim02.loginPassword.paper.DialogLogin;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onEnable() {


        if (loginPasswordPaper.isFolia()) {
            AsyncScheduler asyncScheduler = server.getAsyncScheduler();
            asyncScheduler.runAtFixedRate(this, task -> updateCheck(), 1200, 12096000, TimeUnit.SECONDS);
        } else {
            server.getScheduler().runTaskTimerAsynchronously(this, task -> updateCheck(), 1200, 12096000);
        }

        try {
            config.initConfig();
        } catch (Exception e) {
            logger.error("Failed to initialize config: ", e);
            throw new RuntimeException(e);
        }
        try {
            debugMessage("Registering events and commands...");
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

    public void reenableEvents() {
        try {
            debugMessage("Re-registering events...");
            AsyncPlayerConnectionConfigureEvent.getHandlerList().unregister(this);
            PlayerCustomClickEvent.getHandlerList().unregister(this);
            PlayerConnectionCloseEvent.getHandlerList().unregister(this);
            this.getServer().getPluginManager().registerEvents(new DialogLogin(this, server, logger), this);
        } catch (Exception e) {
            logger.error("Failed to re-register events: ", e);
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
