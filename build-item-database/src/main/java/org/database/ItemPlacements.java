package org.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPlacements {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConfig.getConnection();
            String selectSQL = "SELECT * from ITEMS ORDER BY name ASC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL);

            Map<String, Double> itemMap = new HashMap<>();

            while (resultSet.next()) {
                String itemName = resultSet.getString("name");
                double itemPlacement = resultSet.getDouble("placement");

                if (itemMap.containsKey(itemName)) {
                    itemMap.put(itemName, itemMap.get(itemName) + itemPlacement);
                } else {
                    itemMap.put(itemName, itemPlacement);
                }
            }

            for (Map.Entry<String, Double> entry : itemMap.entrySet()) {
                selectSQL = "SELECT COUNT(name) FROM ITEMS WHERE name = '" + entry.getKey() + "'";
                resultSet = statement.executeQuery(selectSQL);
                resultSet.next();

                itemMap.put(entry.getKey(), entry.getValue() / resultSet.getDouble("count"));
            }

            connection.close();

            System.out.println();

            List<Map.Entry<String, Double>> list = new ArrayList<>(itemMap.entrySet());
            list.sort(Map.Entry.comparingByValue());

            for (Map.Entry<String, Double> entry : list) {
                System.out.println(String.format("%.3f", entry.getValue()) + "  " + entry.getKey());
            }


        } catch(Exception e) {
            System.out.println("Catch: " + e.getMessage());
        }
    }
}
