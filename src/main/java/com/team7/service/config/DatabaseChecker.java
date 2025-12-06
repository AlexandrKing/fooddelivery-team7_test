package com.team7.service.config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseChecker {

  public static void checkAllTables() {
    System.out.println("\n" + "=".repeat(70));
    System.out.println("           🔍 ПОЛНАЯ ПРОВЕРКА БАЗЫ ДАННЫХ");
    System.out.println("=".repeat(70));

    if (!DatabaseConfig.testConnection()) {
      System.err.println("❌ Нет подключения к базе данных!");
      return;
    }

    try (Connection conn = DatabaseConfig.getConnection()) {
      System.out.println("\n📊 СТАТУС ПОДКЛЮЧЕНИЯ:");
      System.out.println("   ✅ Подключено к: " + conn.getMetaData().getURL());
      System.out.println("   ✅ Пользователь: " + conn.getMetaData().getUserName());
      System.out.println("   ✅ Версия PostgreSQL: " + conn.getMetaData().getDatabaseProductVersion());

      // 1. Получаем список всех таблиц
      System.out.println("\n📋 СПИСОК ВСЕХ ТАБЛИЦ В БАЗЕ ДАННЫХ:");
      List<String> allTables = getAllTables(conn);
      System.out.println("   Всего таблиц: " + allTables.size());

      // 2. Проверяем основные таблицы (ожидаемые)
      System.out.println("\n✅ ПРОВЕРКА ОСНОВНЫХ ТАБЛИЦ:");
      String[] expectedTables = {
          "users", "client_restaurants", "client_addresses",
          "client_menu", "client_carts", "client_cart_items",
          "orders", "client_order_items", "reviews",
          "client_order_status_history", "admin_users", "courier_users",
          "courier_assigned_orders"
      };

      int foundTables = 0;
      for (String table : expectedTables) {
        if (allTables.contains(table)) {
          System.out.println("   ✅ " + table + " - существует");
          foundTables++;
          printTableInfo(conn, table);
        } else {
          System.out.println("   ❌ " + table + " - НЕ НАЙДЕНА!");
        }
      }

      System.out.println("\n📊 СВОДКА:");
      System.out.println("   Найдено таблиц: " + foundTables + "/" + expectedTables.length);

      if (foundTables == expectedTables.length) {
        System.out.println("   🎉 Все основные таблицы созданы!");
      } else if (foundTables >= 8) {
        System.out.println("   ⚠️  Большинство таблиц создано (" + foundTables + "/" + expectedTables.length + ")");
      } else {
        System.out.println("   ⚠️  Критически мало таблиц! Запустите DatabaseInitializer");
      }

      // 3. Показываем примеры данных
      System.out.println("\n📊 ОБЗОР ДАННЫХ В ТАБЛИЦАХ:");
      showSampleData(conn);

      // 4. Статистика
      System.out.println("\n📈 СТАТИСТИКА БАЗЫ ДАННЫХ:");
      showStatistics(conn);

    } catch (SQLException e) {
      System.err.println("❌ Ошибка при проверке базы данных: " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("\n" + "=".repeat(70));
  }

  private static List<String> getAllTables(Connection conn) throws SQLException {
    List<String> tables = new ArrayList<>();
    DatabaseMetaData metaData = conn.getMetaData();

    try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
      while (rs.next()) {
        String tableName = rs.getString("TABLE_NAME");
        tables.add(tableName);
        System.out.println("   • " + tableName);
      }
    }
    return tables;
  }

  private static void printTableInfo(Connection conn, String tableName) throws SQLException {
    // Получаем количество записей
    String countSql = "SELECT COUNT(*) as count FROM " + tableName;
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(countSql)) {
      if (rs.next()) {
        int count = rs.getInt("count");
        System.out.println("      📊 Записей: " + count);

        // Если есть данные, показываем структуру
        if (count > 0) {
          showTableStructure(conn, tableName);
          if (count <= 5) {
            showAllRecords(conn, tableName);
          } else {
            showFirstRecords(conn, tableName, 3);
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("      ⚠️  Не удалось посчитать записи: " + e.getMessage());
    }
  }

  private static void showTableStructure(Connection conn, String tableName) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    System.out.print("      📝 Столбцы: ");

    try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
      boolean first = true;
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String columnType = columns.getString("TYPE_NAME");
        int columnSize = columns.getInt("COLUMN_SIZE");

        if (!first) {
          System.out.print(", ");
        }
        System.out.print(columnName + " (" + columnType + ")");
        first = false;
      }
      System.out.println();
    }
  }

  private static void showFirstRecords(Connection conn, String tableName, int limit) throws SQLException {
    String sql = "SELECT * FROM " + tableName + " LIMIT " + limit;
    System.out.println("      📄 Первые " + limit + " записи:");

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();

      while (rs.next()) {
        System.out.print("        • ");
        for (int i = 1; i <= columnCount; i++) {
          if (i > 1) System.out.print(" | ");
          String columnName = metaData.getColumnName(i);
          Object value = rs.getObject(i);
          System.out.print(columnName + ": " + value);
        }
        System.out.println();
      }
    }
  }

  private static void showAllRecords(Connection conn, String tableName) throws SQLException {
    String sql = "SELECT * FROM " + tableName;
    System.out.println("      📄 Все записи:");

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();

      while (rs.next()) {
        System.out.print("        • ");
        for (int i = 1; i <= columnCount; i++) {
          if (i > 1) System.out.print(" | ");
          String columnName = metaData.getColumnName(i);
          Object value = rs.getObject(i);
          System.out.print(columnName + ": " + value);
        }
        System.out.println();
      }
    }
  }

  private static void showSampleData(Connection conn) throws SQLException {
    String[][] sampleTables = {
        {"users", "Пользователи"},
        {"client_restaurants", "Рестораны"},
        {"client_menu", "Меню"},
        {"orders", "Заказы"}
    };

    for (String[] tableInfo : sampleTables) {
      String tableName = tableInfo[0];
      String description = tableInfo[1];

      if (tableExists(conn, tableName)) {
        System.out.println("\n   🏷️  " + description + " (" + tableName + "):");

        String countSql = "SELECT COUNT(*) as count FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
          if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("      Всего записей: " + count);

            if (count > 0) {
              String sampleSql = "SELECT * FROM " + tableName + " LIMIT 2";
              try (Statement sampleStmt = conn.createStatement();
                   ResultSet sampleRs = sampleStmt.executeQuery(sampleSql)) {

                ResultSetMetaData metaData = sampleRs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Выводим названия столбцов
                System.out.print("      Пример: ");
                while (sampleRs.next()) {
                  System.out.print("{ ");
                  for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) System.out.print(", ");
                    String columnName = metaData.getColumnName(i);
                    Object value = sampleRs.getObject(i);
                    System.out.print(columnName + ": " + value);
                  }
                  System.out.print(" } ");
                }
                System.out.println();
              }
            }
          }
        } catch (SQLException e) {
          System.out.println("      ⚠️  Ошибка при выборке: " + e.getMessage());
        }
      } else {
        System.out.println("\n   ❌ " + description + " (" + tableName + ") - таблица не найдена");
      }
    }
  }

  private static void showStatistics(Connection conn) throws SQLException {
    String[] tablesToCheck = {"users", "client_restaurants", "orders"};

    for (String table : tablesToCheck) {
      if (tableExists(conn, table)) {
        String sql = "SELECT COUNT(*) as count FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
          if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("   • " + table + ": " + count + " записей");
          }
        }
      }
    }

    // Проверяем последние заказы
    if (tableExists(conn, "orders")) {
      System.out.println("\n   📦 ПОСЛЕДНИЕ ЗАКАЗЫ:");
      String sql = "SELECT id, user_id, status, total_amount, created_at " +
          "FROM orders ORDER BY created_at DESC LIMIT 3";

      try (Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
          System.out.println("      • Заказ #" + rs.getLong("id") +
              ", пользователь: " + rs.getLong("user_id") +
              ", статус: " + rs.getString("status") +
              ", сумма: " + rs.getDouble("total_amount"));
        }
      }
    }
  }

  private static boolean tableExists(Connection conn, String tableName) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
      return rs.next();
    }
  }

  public static void testDatabaseConnection() {
    System.out.println("\n🔍 ТЕСТИРОВАНИЕ ПОДКЛЮЧЕНИЯ К БАЗЕ ДАННЫХ");
    System.out.println("══════════════════════════════════════════════════");

    try {
      Class.forName("org.postgresql.Driver");
      System.out.println("✅ Драйвер PostgreSQL загружен");
    } catch (ClassNotFoundException e) {
      System.err.println("❌ Драйвер PostgreSQL не найден");
      return;
    }

    try (Connection conn = DatabaseConfig.getConnection()) {
      System.out.println("✅ Подключение успешно!");
      System.out.println("   URL: " + conn.getMetaData().getURL());
      System.out.println("   Database: " + conn.getCatalog());
      System.out.println("   User: " + conn.getMetaData().getUserName());
      System.out.println("   PostgreSQL Version: " + conn.getMetaData().getDatabaseProductVersion());

      // Проверяем доступные схемы
      System.out.println("\n📁 СХЕМЫ БАЗЫ ДАННЫХ:");
      try (ResultSet rs = conn.getMetaData().getSchemas()) {
        while (rs.next()) {
          String schema = rs.getString("TABLE_SCHEM");
          System.out.println("   • " + schema);
        }
      }

    } catch (SQLException e) {
      System.err.println("❌ Ошибка подключения: " + e.getMessage());
      System.err.println("\n   Возможные причины:");
      System.err.println("   1. Контейнер не запущен: docker start restaurant_db");
      System.err.println("   2. Неправильные параметры в DatabaseConfig.java");
      System.err.println("   3. Порт 5432 занят другим приложением");
    }
    System.out.println("══════════════════════════════════════════════════");
  }
}