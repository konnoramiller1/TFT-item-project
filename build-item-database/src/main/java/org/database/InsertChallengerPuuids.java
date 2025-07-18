package org.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class InsertChallengerPuuids {
    public static void main(String[] args) {
        String API_url = "https://na1.api.riotgames.com/tft/league/v1/challenger";

        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_url))
                    .header("X-Riot-Token", System.getenv("API_KEY"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject obj = new JSONObject(response.body());
                JSONArray arr = obj.getJSONArray("entries");

                Connection connection = DatabaseConfig.getConnection();
                String insertSQL = "INSERT INTO PUUIDS (id) values (?) ON CONFLICT (id) do nothing";
                PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

                for (int i = 0; i < arr.length(); i++) {
                    String puuid = arr.getJSONObject(i).getString("puuid");
                    preparedStatement.setString(1,  puuid);
                    preparedStatement.execute();
                }

                connection.close();
            } else {
                System.out.println("Request failed with status code: " + response.statusCode());
                System.out.println(response.body());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
