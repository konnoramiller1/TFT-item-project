package org.database;

import java.sql.*;

public class PostgresCreateTables {
    public static void main(String[] args) {
        createMatchidTable();
        createPuuidTable();
        createItemTable();

        System.out.println("Finished creating tables");
    }

    private static void createPuuidTable() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            Statement statement = connection.createStatement();
            String createTableStatement = "CREATE TABLE PUUIDS (id VARCHAR(200) PRIMARY KEY)";
            statement.execute(createTableStatement);
            connection.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createMatchidTable() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            Statement statement = connection.createStatement();
            String createTableStatement = "CREATE TABLE MATCHES (matchid VARCHAR(200) PRIMARY KEY)";
            statement.execute(createTableStatement);
            connection.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createItemTable() {
        try {
            Connection connection = DatabaseConfig.getConnection();
            Statement statement = connection.createStatement();
            String createTableStatement = "CREATE TABLE ITEMS (id BIGSERIAL PRIMARY KEY, name VARCHAR(200), placement INT)";
            statement.execute(createTableStatement);
            connection.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

