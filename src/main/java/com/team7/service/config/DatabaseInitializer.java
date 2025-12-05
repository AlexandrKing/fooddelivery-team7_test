package com.team7.service.config;

import java.sql.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseInitializer {

  public static void initialize() {
    System.out.println("=".repeat(60));
    System.out.println("        🗄️  ИНИЦИАЛИЗАЦИЯ БАЗЫ ДАННЫХ");
    System.out.println("=".repeat(60));

    if (!DatabaseConfig.testConnection()) {
      System.err.println("❌ Не удалось подключиться к базе данных!");
      throw new RuntimeException("Ошибка подключения к базе данных");
    }

    System.out.println("✅ Подключение к базе данных установлено");

    try (Connection conn = DatabaseConfig.getConnection()) {
      // 1. Сначала удаляем старые таблицы
      dropTables(conn);

      // 2. Создаем новые таблицы
      createTables(conn);

      // 3. Создаем индексы
      createIndexes(conn);

      // 4. Проверяем структуру
      checkDatabaseStructure(conn);

    } catch (SQLException e) {
      System.err.println("❌ Ошибка при инициализации БД: " + e.getMessage());
      throw new RuntimeException("Ошибка инициализации БД", e);
    }
  }

  private static void dropTables(Connection conn) throws SQLException {
    System.out.println("\n🗑️  УДАЛЕНИЕ СТАРЫХ ТАБЛИЦ...");
    String dropScript = readSqlFile("01_drop/001_drop_tables.sql");
    if (dropScript != null) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(dropScript);
        System.out.println("✅ Таблицы удалены");
      }
    }
  }

  private static void createTables(Connection conn) throws SQLException {
    System.out.println("\n🏗️  СОЗДАНИЕ ТАБЛИЦ...");

    // Список скриптов создания таблиц (в правильном порядке)
    String[] createScripts = {
        "001_create_users.sql",
        "002_create_restaurants.sql",
        "003_create_menu_categories.sql",
        "004_create_dishes.sql",
        "005_create_addresses.sql",
        "006_create_carts.sql",
        "007_create_cart_items.sql",
        "08_create_orders.sql",
        "09_create_order_items.sql",
        // Переносим создание courier_users ПЕРЕД reviews
        "012_create_admin_users.sql",
        "013_create_courier_users.sql",  // Теперь создается ДО reviews
        "014_create_courier_assigned_orders.sql",
        "010_create_reviews.sql",        // Теперь будет работать
        "011_create_order_status_history.sql"
    };

    for (String script : createScripts) {
      String sql = readSqlFile("02_create/" + script);
      if (sql != null && !sql.trim().isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(sql);
          System.out.println("   ✅ " + script);
        } catch (SQLException e) {
          System.err.println("   ❌ Ошибка в " + script + ": " + e.getMessage());
        }
      }
    }
  }

  private static void createIndexes(Connection conn) throws SQLException {
    System.out.println("\n📊 СОЗДАНИЕ ИНДЕКСОВ...");

    // Список скриптов индексов
    String[] indexScripts = {
        "001_create_users_indexes.sql",
        "002_create_restaurant_indexes.sql",
        "003_create_menu_categories_indexes.sql",
        "004_create_dishes_indexes.sql",
        "005_create_addresses_indexes.sql",
        "006_create_carts_indexes.sql",
        "007_create_cart_items_indexes.sql",
        "008_create_orders_indexes.sql",
        "009_create_order_items_indexes.sql",
        "010_create_reviews_indexes.sql",
        "011_create_order_status_history_indexes.sql",
        "012_create_admin_users_indexes.sql",
        "013_create_couriers_users_indexes.sql",
        "014_create_courier_assigned_orders_indexes.sql"
    };

    for (String script : indexScripts) {
      String sql = readSqlFile("03_indexes/" + script);
      if (sql != null && !sql.trim().isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(sql);
          System.out.println("   ✅ " + script);
        } catch (SQLException e) {
          System.err.println("   ⚠️  Индекс в " + script + ": " + e.getMessage());
        }
      }
    }
  }

  private static String readSqlFile(String relativePath) {
    try {
      // Пробуем разные пути
      Path[] possiblePaths = {
          Paths.get("src/main/resources", relativePath),
          Paths.get("resources", relativePath),
          Paths.get(relativePath),
          Paths.get(System.getProperty("user.dir"), "src/main/resources", relativePath)
      };

      for (Path path : possiblePaths) {
        if (Files.exists(path)) {
          return Files.readString(path);
        }
      }

      // Также пробуем загрузить из classpath
      InputStream is = DatabaseInitializer.class.getClassLoader()
          .getResourceAsStream(relativePath);
      if (is != null) {
        return new String(is.readAllBytes());
      }

      System.err.println("❌ Файл не найден: " + relativePath);
      return null;

    } catch (IOException e) {
      System.err.println("❌ Ошибка чтения файла " + relativePath + ": " + e.getMessage());
      return null;
    }
  }

  private static void checkDatabaseStructure(Connection conn) throws SQLException {
    System.out.println("\n🔍 ПРОВЕРКА СТРУКТУРЫ БАЗЫ ДАННЫХ:");

    String[] tables = {
        "users", "restaurants", "menu_categories", "dishes",
        "addresses", "carts", "cart_items", "orders",
        "order_items", "reviews", "order_status_history",
        "admin_users", "courier_users", "courier_assigned_orders"
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

    if (foundTables == tables.length) {
      System.out.println("✅ Все таблицы созданы успешно!");
    } else {
      System.out.println("⚠️  Некоторые таблицы отсутствуют");
    }

    System.out.println("=".repeat(60));
    System.out.println("✅ База данных успешно инициализирована");
  }
}