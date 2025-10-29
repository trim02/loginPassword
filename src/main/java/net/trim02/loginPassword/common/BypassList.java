package net.trim02.loginPassword.common;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.UUID;

public class BypassList {

    protected static Logger logger;
    protected static Path dataDirectory;
    public static final String BypassListFileName = "bypasslist.json";
    public static JsonArray bypassList;
    public static Gson gson;

    public BypassList(Logger logger, Path dataDirectory) {
        BypassList.logger = logger;
        BypassList.dataDirectory = dataDirectory;
        gson = new Gson();

    }

    public static void loadBypassList() {
        File bypassFile = dataDirectory.resolve(BypassListFileName).toFile();
        if (!bypassFile.exists()) {

            bypassList = new JsonArray();
            saveBypassList();

            return;
        }

        try {
            bypassList = JsonParser.parseReader(new java.io.FileReader(bypassFile)).getAsJsonArray();


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
    public static void saveBypassList() {

        File bypassFile = dataDirectory.resolve(BypassListFileName).toFile();

        try {
            java.io.FileWriter writer = new java.io.FileWriter(bypassFile);
            gson.toJson(bypassList, writer);
            writer.flush();
            writer.close();
            loadBypassList();

        } catch (Exception e) {
            logger.error("Error saving bypass list: {}", e.getMessage());
        }
    }
    public static void addBypassEntry(String playerUUID) {
        if (!bypassList.contains(gson.toJsonTree(playerUUID))) {
            bypassList.add(playerUUID);
            saveBypassList();

        }
    }
    public static void removeBypassEntry(String playerUUID) {
        if (bypassList.contains(gson.toJsonTree(playerUUID))) {
            bypassList.remove(gson.toJsonTree(playerUUID));
            saveBypassList();

        }
    }
    public static boolean inBypassList(UUID playerUUID) {
        return bypassList.contains(gson.toJsonTree(playerUUID));
    }
    public static boolean validUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}