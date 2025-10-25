package net.trim02.loginPassword.interfaces;

import org.slf4j.Logger;

public interface loginPassword<T> {


    String getPlatformName();
    Logger getInterLogger();
    T getInterServer();

}
