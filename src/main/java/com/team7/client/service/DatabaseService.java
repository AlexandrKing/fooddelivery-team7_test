package com.team7.client.service;

import java.sql.*;

public class DatabaseService {
    private static final String URL = "jdbc:postgresql://localhost:5432/food_delivery";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}