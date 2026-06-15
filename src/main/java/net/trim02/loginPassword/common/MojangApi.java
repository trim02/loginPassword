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
//        System.out.println("Fetching UUID for username: " + username);
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
