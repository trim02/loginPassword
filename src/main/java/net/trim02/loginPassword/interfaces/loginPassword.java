package net.trim02.loginPassword.interfaces;

import net.trim02.loginPassword.Config;
import org.slf4j.Logger;

import java.nio.file.Path;

public interface loginPassword<T> {


    String getPlatformName();
    Logger getInterLogger();
    Path getInterDataFolder();
    T getInterServer();

    default Boolean isDebugModeEnabled(){
        return Config.configVar.debugMode;
    }
    default void debugMessage(String message) {
        if (isDebugModeEnabled()) {
            getInterLogger().info("[Debug] {}", message);
        }
    }

}
