package com.team7.service.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @deprecated Legacy JDBC access. Prefer Spring DataSource/JdbcTemplate beans.
 */
// TODO(legacy-cleanup): remove in Wave 3 after all services stop using legacy JDBC paths.
@Deprecated(forRemoval = false, since = "1.1")
public class DatabaseConfig {
  // Параметры из docker-compose.yml
  private static final String URL = "jdbc:postgresql://localhost:5432/restaurant_db";
  private static final String USER = "restaurant_user";
  private static final String PASSWORD = "restaurant_password";

  static {
    try {
      Class.forName("org.postgresql.Driver");
      System.out.println("✅ PostgreSQL драйвер загружен");
    } catch (ClassNotFoundException e) {
      System.err.println("❌ PostgreSQL драйвер не найден");
      e.printStackTrace();
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  public static boolean testConnection() {
    try (Connection conn = getConnection()) {
      if (conn != null && !conn.isClosed()) {
        System.out.println("✅ Успешное подключение к PostgreSQL!");
        return true;
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка подключения к базе данных: " + e.getMessage());
    }
    return false;
  }
}