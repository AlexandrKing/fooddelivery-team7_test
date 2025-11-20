package com.team7.restaurant.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
  private static final String URL = "jdbc:postgresql://localhost:5432/restaurant_db";
  private static final String USER = "restaurant_user";
  private static final String PASSWORD = "restaurant_password";

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}
