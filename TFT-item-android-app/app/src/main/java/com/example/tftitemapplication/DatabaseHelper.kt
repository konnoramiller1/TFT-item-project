package com.example.tftitemapplication

import java.sql.Connection
import java.sql.DriverManager


class DatabaseHelper {
    companion object {
        private const val DB_URL = "jdbc:postgresql://10.0.2.2:5432/db"
        private const val USER = "postgres"
        private const val PASS = "pass123"
    }

    fun connect(): Connection? {
        return try {
            DriverManager.getConnection(DB_URL, USER, PASS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}