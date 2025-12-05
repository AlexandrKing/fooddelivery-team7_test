package com.team7.service.config;

import java.sql.*;

public class DatabaseInitializer {

  public static void initialize() {
    System.out.println("=".repeat(60));
    System.out.println("        🗄️  ИНИЦИАЛИЗАЦИЯ БАЗЫ ДАННЫХ");
    System.out.println("=".repeat(60));

    // Проверяем подключение
    if (!DatabaseConfig.testConnection()) {
      System.err.println("❌ Не удалось подключиться к базе данных!");
      throw new RuntimeException("Ошибка подключения к базе данных");
    }

    System.out.println("✅ Подключение к базе данных установлено");
    System.out.println("✅ База данных уже инициализирована (пропускаем DROP/CREATE)");

    // Проверяем существование основных таблиц
    try (Connection conn = DatabaseConfig.getConnection()) {
      checkDatabaseStructure(conn);
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при проверке структуры БД: " + e.getMessage());
      throw new RuntimeException("Ошибка проверки структуры БД", e);
    }
  }

  private static void checkDatabaseStructure(Connection conn) throws SQLException {
    System.out.println("\n🔍 ПРОВЕРКА СТРУКТУРЫ БАЗЫ ДАННЫХ:");

    // ИСПРАВЛЕННЫЙ список таблиц (совпадает с SQL файлами)
    String[] tables = {
        "users",
        "restaurants",  // ИСПРАВЛЕНО: было client_restaurants
        "menu_categories",
        "dishes",       // ИСПРАВЛЕНО: было client_menu
        "addresses",
        "carts",
        "cart_items",
        "orders",
        "order_items",
        "reviews",
        "order_status_history",
        "admin_users",
        "courier_users",
        "courier_assigned_orders"
    };

    int foundTables = 0;

    for (String table : tables) {
      try {
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, table);
          ResultSet rs = pstmt.executeQuery();
          if (rs.next() && rs.getBoolean(1)) {
            System.out.println("   ✅ " + table);
            foundTables++;
          } else {
            System.out.println("   ❌ " + table + " (не найдена)");
          }
        }
      } catch (SQLException e) {
        System.out.println("   ❌ " + table + " (ошибка: " + e.getMessage() + ")");
      }
    }

    System.out.println("\n📊 Статус: " + foundTables + "/" + tables.length + " таблиц");

    if (foundTables >= 8) {
      System.out.println("✅ Основные таблицы существуют");
    } else {
      System.out.println("⚠️  Некоторые таблицы отсутствуют");
      System.out.println("ℹ️  Если таблиц нет, они будут созданы при первом использовании");
    }

    System.out.println("=".repeat(60));
  }
}