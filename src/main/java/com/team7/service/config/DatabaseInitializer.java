package com.team7.service.config;

import java.sql.Connection;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class DatabaseInitializer {

  public static void initialize() {
    System.out.println("=== DATABASE INITIALIZATION ===");

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement()) {

      System.out.println("✅ Connected to PostgreSQL");

      // ПРАВИЛЬНЫЙ ПОРЯДОК ВЫПОЛНЕНИЯ SQL ФАЙЛОВ
      List<String> sqlFiles = Arrays.asList(
          // 1. УДАЛЕНИЕ СТАРЫХ ТАБЛИЦ
          "src/main/resources/01_drop/001_drop_tables.sql",
          "src/main/resources/01_drop/002_drop_client_tables.sql",

          // 2. СОЗДАНИЕ РЕСТОРАННЫХ ТАБЛИЦ
          "src/main/resources/02_create/001_create_restaurant.sql",
          "src/main/resources/02_create/002_create_menu_category.sql",
          "src/main/resources/02_create/003_create_dish.sql",

          // 3. СОЗДАНИЕ КЛИЕНТСКИХ ТАБЛИЦ
          "src/main/resources/02_create/004_create_client_users.sql",
          "src/main/resources/02_create/005_create_client_restaurants.sql",
          "src/main/resources/02_create/006_create_client_addresses.sql",
          "src/main/resources/02_create/007_create_client_menu.sql",
          "src/main/resources/02_create/008_create_client_carts.sql",
          "src/main/resources/02_create/009_create_client_cart_items.sql",
          "src/main/resources/02_create/010_create_client_orders.sql",
          "src/main/resources/02_create/011_create_client_order_items.sql",
          "src/main/resources/02_create/012_create_client_reviews.sql",
          "src/main/resources/02_create/013_create_client_order_status_history.sql",

          // 4. СОЗДАНИЕ ИНДЕКСОВ
          "src/main/resources/03_indexes/001_create_indexes.sql",
          "src/main/resources/03_indexes/002_create_client_indexes.sql",

          // 5. ВСТАВКА ДАННЫХ РЕСТОРАНОВ
          "src/main/resources/04_data/001_insert_restaurants.sql",
          "src/main/resources/04_data/002_insert_menu_categories.sql",
          "src/main/resources/04_data/003_insert_dishes.sql",

          // 6. ВСТАВКА ДАННЫХ КЛИЕНТОВ
          "src/main/resources/04_data/004_insert_client_users.sql",
          "src/main/resources/04_data/005_insert_client_restaurants.sql",
          "src/main/resources/04_data/006_insert_client_addresses.sql",
          "src/main/resources/04_data/007_insert_client_menu.sql",
          "src/main/resources/04_data/008_insert_client_carts.sql",
          "src/main/resources/04_data/009_insert_client_cart_items.sql",
          "src/main/resources/04_data/010_insert_client_orders.sql",
          "src/main/resources/04_data/011_insert_client_order_items.sql",
          "src/main/resources/04_data/012_insert_client_reviews.sql",
          "src/main/resources/04_data/013_insert_client_order_status_history.sql"
      );

      System.out.println("Executing SQL files in correct order...");

      for (String sqlFile : sqlFiles) {
        try {
          System.out.print("  📄 " + sqlFile.substring(sqlFile.lastIndexOf('/') + 1) + "... ");

          java.nio.file.Path path = Paths.get(sqlFile);

          if (!Files.exists(path)) {
            System.out.println("❌ NOT FOUND");
            continue;
          }

          // Читаем файл
          String sql = new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);

          // УБИРАЕМ ВСЕ КОММЕНТАРИИ
          sql = sql.replaceAll("--.*\n", "");
          sql = sql.trim();

          if (sql.isEmpty()) {
            System.out.println("⚠️ EMPTY");
            continue;
          }

          // ВЫПОЛНЯЕМ ВЕСЬ SQL КАК ОДНУ КОМАНДУ
          stmt.execute(sql);
          System.out.println("✅ DONE");

        } catch (Exception e) {
          System.out.println("❌ ERROR: " + e.getMessage());

          // Если не удалось выполнить весь файл, пробуем построчно
          try {
            System.out.println("  Trying line by line execution...");
            executeSqlLineByLine(stmt, sqlFile);
          } catch (Exception e2) {
            System.err.println("  Line by line also failed: " + e2.getMessage());
          }
        }
      }

      System.out.println("\n✅ Database initialization complete!");
      checkAllTables(stmt);

    } catch (Exception e) {
      System.err.println("❌ Database initialization failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void executeSqlLineByLine(Statement stmt, String sqlFile) throws Exception {
    java.nio.file.Path path = Paths.get(sqlFile);
    List<String> lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);

    StringBuilder currentCommand = new StringBuilder();

    for (String line : lines) {
      String trimmed = line.trim();

      // Пропускаем комментарии и пустые строки
      if (trimmed.isEmpty() || trimmed.startsWith("--")) {
        continue;
      }

      currentCommand.append(line).append("\n");

      // Если строка заканчивается точкой с запятой, выполняем команду
      if (trimmed.endsWith(";")) {
        String sql = currentCommand.toString().trim();
        if (!sql.isEmpty()) {
          try {
            stmt.execute(sql);
            System.out.println("    ✓ Executed command");
          } catch (Exception e) {
            System.err.println("    ✗ SQL Error: " + e.getMessage());
          }
        }
        currentCommand.setLength(0); // Очищаем для следующей команды
      }
    }

    // Если осталась невыполненная команда (без ; в конце)
    String remaining = currentCommand.toString().trim();
    if (!remaining.isEmpty()) {
      try {
        stmt.execute(remaining);
        System.out.println("    ✓ Executed final command");
      } catch (Exception e) {
        System.err.println("    ✗ Final SQL Error: " + e.getMessage());
      }
    }
  }

  private static void checkAllTables(Statement stmt) throws Exception {
    System.out.println("\nChecking tables...");

    // Все таблицы: ресторанные + клиентские
    String[] allTables = {
        // Ресторанные таблицы
        "restaurant", "menu_category", "dish",
        // Клиентские таблицы
        "client_users", "client_restaurants", "client_addresses", "client_menu",
        "client_carts", "client_cart_items", "client_orders", "client_order_items",
        "client_reviews", "client_order_status_history"
    };

    int createdCount = 0;
    for (String table : allTables) {
      try {
        stmt.execute("SELECT 1 FROM " + table + " LIMIT 1");
        System.out.println("  • " + table);
        createdCount++;
      } catch (Exception e) {
        System.out.println("  • " + table + " ❌ NOT FOUND");
      }
    }

    System.out.println("\n✅ " + createdCount + " tables created out of " + allTables.length);
  }
}