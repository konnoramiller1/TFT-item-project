package com.example.tftitemapplication

import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DatabaseRepository {
    private val databaseHelper = DatabaseHelper()

    suspend fun fetchData(): MutableMap<String, Double> = withContext(Dispatchers.IO) {
        val itemMap: MutableMap<String, Double> = HashMap()
        var connection: Connection? = null

        try {
            connection = databaseHelper.connect()!!
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * from ITEMS ORDER BY name ASC")

            while (resultSet.next()) {
                val itemName = resultSet.getString("name")
                val itemPlacement: Double = resultSet.getDouble("placement")

                itemMap[itemName] = itemMap.getOrDefault(itemName, 0.0) + itemPlacement
            }

            val preparedStatement = connection.prepareStatement("SELECT COUNT(*) as item_count FROM ITEMS WHERE name = ?")

            for ((key, value) in itemMap) {
                preparedStatement.setString(1, key)
                val countResult = preparedStatement.executeQuery()

                if (countResult.next()) {
                    itemMap[key] = value / countResult.getInt("item_count")
                }

                countResult.close()
            }

            resultSet.close()
            statement.close()
            preparedStatement.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }

        itemMap
    }
}