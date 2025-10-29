package net.trim02.loginPassword.common;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class MojangApi {

    protected static String MOJANG_API_ENDPOINT = "https://api.mojang.com/";
    protected static HttpClient client = HttpClient.newHttpClient();

    public static String constructApiUrl(String endpoint, String query) {

        return MOJANG_API_ENDPOINT + endpoint  + query;
    }

    public static String getUUID(String username) {
        System.out.println("Fetching UUID for username: " + username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(constructApiUrl("minecraft/profile/lookup/name/", username)))
                .GET()
                .build();
        HttpResponse<String> response;

        try {

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {

            throw new RuntimeException(e);
        }

        return JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsString();
    }



}
