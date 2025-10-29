package net.trim02.loginPassword.interfaces;

import org.slf4j.Logger;

import java.nio.file.Path;

public interface loginPassword<T> {


    String getPlatformName();
    Logger getInterLogger();

    Path getInterDataFolder();

    T getInterServer();

}
