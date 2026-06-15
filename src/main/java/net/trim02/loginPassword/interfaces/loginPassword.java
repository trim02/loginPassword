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

package net.trim02.loginPassword.interfaces;

import com.technicjelle.UpdateChecker;
import net.trim02.loginPassword.BuildConstants;
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

    default void updateCheck() {
        UpdateChecker updateChecker = new UpdateChecker("trim02", "loginPassword", BuildConstants.VERSION);

        try {
            updateChecker.check();
            if(updateChecker.isUpdateAvailable()) {
                var updateMessage = """
                            A new version is available: %s -> %s. Download the new version here:
                            modrinth: https://modrinth.com/plugin/loginpassword
                            Hangar: https://hangar.papermc.io/trim02/loginPassword
                            GitHub: %s
                            """.formatted(updateChecker.getCurrentVersion(), updateChecker.getLatestVersion(), updateChecker.getUpdateUrl());
                getInterLogger().info(updateMessage);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }


    }

}
