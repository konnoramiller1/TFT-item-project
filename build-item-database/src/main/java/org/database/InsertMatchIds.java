package org.database;

import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;

public class InsertMatchIds {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConfig.getConnection();
            String selectSQL = "SELECT * from PUUIDS ORDER BY id ASC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL);

            HttpClient client = HttpClient.newHttpClient();

            while (resultSet.next()) {
                String puuid = resultSet.getString("id");

                String API_url = "https://americas.api.riotgames.com/tft/match/v1/matches/by-puuid/" + puuid + "/ids";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(API_url))
                        .header("X-Riot-Token", System.getenv("API_KEY"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    String insertSQL = "INSERT INTO MATCHES (matchid) values (?) ON CONFLICT (matchid) do nothing";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

                    for (int i = 0; i < arr.length(); i++) {
                        preparedStatement.setString(1, arr.getString(i));
                        preparedStatement.execute();
                    }
                } else {
                    System.out.println("Request failed with status code: " + response.statusCode());
                    System.out.println(response.body());
                    break;
                }
            }

            connection.close();
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

