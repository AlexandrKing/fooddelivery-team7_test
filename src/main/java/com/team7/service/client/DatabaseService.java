package com.team7.service.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
  private static final String URL = "jdbc:postgresql://localhost:5432/restaurant_db";
  private static final String USER = "restaurant_user";
  private static final String PASSWORD = "restaurant_password";

  public Connection connect() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}