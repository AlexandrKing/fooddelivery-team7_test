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

      // ПРАВИЛЬНЫЙ ПОРЯДОК - ТАК ЖЕ КАК У ВАС
      List<String> sqlFiles = Arrays.asList(
          "src/main/resources/01_drop/001_drop_tables.sql",
          "src/main/resources/02_create/001_create_restaurant.sql",
          "src/main/resources/02_create/002_create_menu_category.sql",
          "src/main/resources/02_create/003_create_dish.sql",
          "src/main/resources/03_indexes/001_create_indexes.sql",
          "src/main/resources/04_data/001_insert_restaurants.sql",
          "src/main/resources/04_data/002_insert_menu_categories.sql",
          "src/main/resources/04_data/003_insert_dishes.sql"
      );

      System.out.println("Executing SQL files in correct order...");

      for (String sqlFile : sqlFiles) {
        try {
          System.out.print("  📄 " + sqlFile + "... ");

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
          // (особенно для CREATE TABLE с FOREIGN KEY)
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

      // Проверяем таблицы
      System.out.println("\nChecking tables...");
      var rs = stmt.executeQuery(
          "SELECT table_name FROM information_schema.tables " +
              "WHERE table_schema = 'public' ORDER BY table_name"
      );

      int tableCount = 0;
      while (rs.next()) {
        System.out.println("  • " + rs.getString("table_name"));
        tableCount++;
      }

      if (tableCount == 0) {
        System.out.println("  ❌ No tables created!");
      } else {
        System.out.println("  ✅ " + tableCount + " tables created");
      }

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
}
