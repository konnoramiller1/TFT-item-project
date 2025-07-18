package org.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InsertItemData {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConfig.getConnection();
            String selectSQL = "SELECT * from MATCHES ORDER BY matchid DESC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL);

            HttpClient client = HttpClient.newHttpClient();

            int count = 1;

            while (resultSet.next()) {

                // RiotAPI can only be polled 100 times every 2 mins
                // so must wait at least 2 mins before continuing
                if (count % 100 == 0) {
                    TimeUnit.MINUTES.sleep(2);
                    TimeUnit.SECONDS.sleep(2);
                }

                String match = resultSet.getString("matchid");

                String API_url = "https://americas.api.riotgames.com/tft/match/v1/matches/" + match;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(API_url))
                        .header("X-Riot-Token", System.getenv("API_KEY"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject object = new JSONObject(response.body());
                    JSONObject infoObject = object.getJSONObject("info");

                    // Only want games that are ranked games and games that are of the current set
                    // so skip the ones that aren't either of those
                    if (infoObject.getInt("queueId") != 1100 && !Objects.equals(infoObject.getString("tft_set_core_name"), "TFTSet14")) {
                        System.out.println("Game " + count + " skipped!!");
                        count++;
                        continue;
                    }

                    JSONArray array = infoObject.getJSONArray("participants");

                    String insertSQL = "INSERT INTO ITEMS (name, placement) values (?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

                    for (int i = 0; i < array.length(); i++) {
                        int placement = array.getJSONObject(i).getInt("placement");
                        JSONArray unitArray = array.getJSONObject(i).getJSONArray("units");

                        Set<String> itemSet = new HashSet<>();

                        for (int j = 0; j < unitArray.length(); j++) {
                            JSONArray itemArray = unitArray.getJSONObject(j).getJSONArray("itemNames");

                            if (itemArray != null && !itemArray.isEmpty()) {

                                for (int k = 0; k < itemArray.length(); k++) {

                                    if (!itemSet.contains(itemArray.getString(k))) {

                                        itemSet.add(itemArray.getString(k));
                                        preparedStatement.setString(1, itemArray.getString(k));
                                        preparedStatement.setInt(2, placement);
                                        preparedStatement.execute();
                                    }
                                }
                            }
                        }
                    }

                    System.out.println("Game " + count + " finished being added");
                    count++;

                } else {
                    System.out.println("Request failed with status code: " + response.statusCode());
                    System.out.println(response.body());
                    break;
                }
            }

            connection.close();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

