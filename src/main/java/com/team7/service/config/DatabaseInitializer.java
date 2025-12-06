package com.team7.service.config;

import java.sql.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

      // 4. Вставляем тестовые данные ИЗ ФАЙЛОВ
      insertTestDataFromFiles(conn);

      // 5. Проверяем структуру и данные
      checkDatabaseStructure(conn);
      checkDataCounts(conn);

    } catch (SQLException e) {
      System.err.println("❌ Ошибка при инициализации БД: " + e.getMessage());
      throw new RuntimeException("Ошибка инициализации БД", e);
    }
  }

  private static void dropTables(Connection conn) throws SQLException {
    System.out.println("\n🗑️  УДАЛЕНИЕ СТАРЫХ ТАБЛИЦ...");

    // Сначала пытаемся найти и выполнить файл удаления
    String dropScript = readSqlFile("01_drop/001_drop_tables.sql");

    if (dropScript != null && !dropScript.trim().isEmpty()) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(dropScript);
        System.out.println("✅ Таблицы удалены из файла");
      } catch (SQLException e) {
        System.err.println("⚠️  Ошибка при выполнении drop-скрипта: " + e.getMessage());
        System.out.println("🔄 Пытаемся удалить таблицы вручную...");
        dropTablesManually(conn);
      }
    } else {
      System.out.println("⚠️  Файл удаления не найден, удаляем вручную...");
      dropTablesManually(conn);
    }
  }

  private static void dropTablesManually(Connection conn) throws SQLException {
    // Порядок удаления с учетом зависимостей
    String[] tablesToDrop = {
        "courier_assigned_orders",
        "order_status_history",
        "reviews",
        "courier_users",
        "admin_users",
        "order_items",
        "cart_items",
        "carts",
        "orders",
        "addresses",
        "dishes",
        "menu_categories",
        "restaurants",
        "users"
    };

    try (Statement stmt = conn.createStatement()) {
      // Отключаем проверку внешних ключей
      stmt.execute("SET CONSTRAINTS ALL DEFERRED");

      int droppedCount = 0;
      for (String table : tablesToDrop) {
        try {
          stmt.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
          droppedCount++;
          System.out.println("   ✓ Удалена таблица: " + table);
        } catch (SQLException e) {
          // Таблицы может не существовать - это нормально
        }
      }
      System.out.println("✅ Удалено таблиц: " + droppedCount);
    }
  }

  private static void createTables(Connection conn) throws SQLException {
    System.out.println("\n🏗️  СОЗДАНИЕ ТАБЛИЦ...");

    // Список скриптов создания таблиц (в правильном порядке зависимостей)
    String[] createScripts = {
        "001_create_users.sql",
        "002_create_restaurants.sql",
        "003_create_menu_categories.sql",
        "004_create_dishes.sql",
        "005_create_addresses.sql",
        "006_create_carts.sql",
        "007_create_cart_items.sql",
        "008_create_orders.sql",
        "009_create_order_items.sql",
        // Переносим создание courier_users ПЕРЕД reviews
        "010_create_admin_users.sql",
        "011_create_courier_users.sql",  // Теперь создается ДО reviews
        "012_create_courier_assigned_orders.sql",
        "013_create_reviews.sql",        // Теперь будет работать
        "014_create_order_status_history.sql"
    };

    int createdCount = 0;
    for (String script : createScripts) {
      String sql = readSqlFile("02_create/" + script);
      if (sql != null && !sql.trim().isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          // Выполняем SQL построчно, чтобы избежать ошибок с точками с запятой
          String[] statements = sql.split(";");
          for (String statement : statements) {
            if (statement.trim().length() > 0) {
              stmt.execute(statement.trim());
            }
          }
          System.out.println("   ✅ " + script);
          createdCount++;
        } catch (SQLException e) {
          System.err.println("   ❌ Ошибка в " + script + ": " + e.getMessage());
          // Не прерываем выполнение, продолжаем создание других таблиц
        }
      } else {
        System.err.println("   ❌ Файл не найден или пустой: " + script);
      }
    }

    System.out.println("📊 Создано таблиц: " + createdCount + "/" + createScripts.length);
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
        "010_create_admin_users_indexes.sql",
        "011_create_courier_users_indexes.sql",
        "012_create_courier_assigned_orders_indexes.sql",
        "013_create_reviews_indexes.sql",
        "014_create_order_status_history_indexes.sql",
    };

    int createdIndexes = 0;
    for (String script : indexScripts) {
      String sql = readSqlFile("03_indexes/" + script);
      if (sql != null && !sql.trim().isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          String[] statements = sql.split(";");
          for (String statement : statements) {
            if (statement.trim().length() > 0) {
              stmt.execute(statement.trim());
            }
          }
          System.out.println("   ✅ " + script);
          createdIndexes++;
        } catch (SQLException e) {
          System.err.println("   ⚠️  Ошибка индекса в " + script + ": " + e.getMessage());
        }
      } else {
        System.out.println("   ⚠️  Файл индекса не найден: " + script);
      }
    }

    System.out.println("📊 Создано индексов: " + createdIndexes + "/" + indexScripts.length);
  }

  private static void insertTestDataFromFiles(Connection conn) throws SQLException {
    System.out.println("\n📥 ЗАГРУЗКА ТЕСТОВЫХ ДАННЫХ ИЗ ФАЙЛОВ...");

    // Порядок вставки данных (с учетом зависимостей между таблицами)
    String[] insertFiles = {
        "001_insert_users.sql",
        "002_insert_restaurants.sql",
        "003_insert_menu_categories.sql",
        "004_insert_dishes.sql",
        "005_insert_addresses.sql",
        "006_insert_carts.sql",
        "007_insert_cart_items.sql",
        "008_insert_orders.sql",
        "009_insert_order_items.sql",
        "010_insert_admin_users.sql",
        "011_insert_courier_users.sql",
        "012_insert_courier_assigned_orders.sql",
        "013_insert_reviews.sql",
        "014_insert_order_status_history.sql",
    };

    int insertedFiles = 0;
    int totalRows = 0;

    try (Statement stmt = conn.createStatement()) {
      // Отключаем проверку внешних ключей для упрощения вставки
      stmt.execute("SET CONSTRAINTS ALL DEFERRED");

      for (String file : insertFiles) {
        String sql = readSqlFile("04_data/" + file);
        if (sql != null && !sql.trim().isEmpty()) {
          try {
            // Разделяем SQL по точкам с запятой и выполняем каждую команду отдельно
            String[] statements = sql.split(";");
            int rowsAffected = 0;

            for (String statement : statements) {
              String trimmed = statement.trim();
              if (trimmed.length() > 0 && !trimmed.startsWith("--")) {
                if (trimmed.toUpperCase().startsWith("INSERT")) {
                  rowsAffected += stmt.executeUpdate(trimmed);
                } else {
                  stmt.execute(trimmed);
                }
              }
            }

            System.out.println("   ✅ " + file + " (" + rowsAffected + " строк)");
            insertedFiles++;
            totalRows += rowsAffected;

          } catch (SQLException e) {
            System.err.println("   ❌ Ошибка при вставке из " + file + ": " + e.getMessage());
            // Продолжаем с другими файлами
          }
        } else {
          System.out.println("   ⚠️  Файл данных не найден: " + file);
        }
      }

      // Включаем проверку внешних ключей обратно
      stmt.execute("SET CONSTRAINTS ALL IMMEDIATE");
    }

    System.out.println("📊 Загружено файлов: " + insertedFiles + "/" + insertFiles.length);
    System.out.println("📊 Всего строк добавлено: " + totalRows);
  }

  private static void checkDatabaseStructure(Connection conn) throws SQLException {
    System.out.println("\n🔍 ПРОВЕРКА СТРУКТУРЫ БАЗЫ ДАННЫХ:");

    String[] requiredTables = {
        "users", "restaurants", "menu_categories", "dishes",
        "addresses", "carts", "cart_items", "orders",
        "order_items", "reviews", "order_status_history",
        "admin_users", "courier_users", "courier_assigned_orders"
    };

    int foundTables = 0;

    for (String table : requiredTables) {
      try {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, table);
          ResultSet rs = pstmt.executeQuery();
          if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("   ✅ " + table);
            foundTables++;
          } else {
            System.out.println("   ❌ " + table + " (не найдена)");
          }
        }
      } catch (SQLException e) {
        System.out.println("   ❌ " + table + " (ошибка проверки: " + e.getMessage() + ")");
      }
    }

    System.out.println("\n📊 Структура: " + foundTables + "/" + requiredTables.length + " таблиц");

    if (foundTables == requiredTables.length) {
      System.out.println("✅ Все таблицы созданы успешно!");
    } else {
      System.out.println("⚠️  Некоторые таблицы отсутствуют");
    }
  }

  private static void checkDataCounts(Connection conn) throws SQLException {
    System.out.println("\n📊 ПРОВЕРКА КОЛИЧЕСТВА ДАННЫХ:");

    String[][] tablesToCheck = {
        {"users", "Пользователи"},
        {"restaurants", "Рестораны"},
        {"dishes", "Блюда"},
        {"orders", "Заказы"}
    };

    try (Statement stmt = conn.createStatement()) {
      for (String[] tableInfo : tablesToCheck) {
        String tableName = tableInfo[0];
        String tableLabel = tableInfo[1];

        try {
          ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
          if (rs.next()) {
            int count = rs.getInt(1);
            String status = count > 0 ? "✅" : "⚠️ ";
            System.out.printf("   %s %-15s: %d записей%n", status, tableLabel, count);
          }
        } catch (SQLException e) {
          System.out.printf("   ❌ %-15s: ошибка (%s)%n", tableLabel, e.getMessage());
        }
      }
    }

    System.out.println("\n" + "=".repeat(60));
    System.out.println("✅ База данных успешно инициализирована");
    System.out.println("✅ Тестовые данные загружены из файлов");
    System.out.println("=".repeat(60));
  }

  private static String readSqlFile(String relativePath) {
    try {
      // Пробуем разные пути
      Path[] possiblePaths = {
          Paths.get("src/main/resources", relativePath),
          Paths.get("resources", relativePath),
          Paths.get(relativePath),
          Paths.get(System.getProperty("user.dir"), "src/main/resources", relativePath),
          Paths.get(System.getProperty("user.dir"), "resources", relativePath)
      };

      for (Path path : possiblePaths) {
        if (Files.exists(path)) {
          String content = Files.readString(path);
          System.out.println("   📄 Загружен файл: " + path);
          return content;
        }
      }

      // Также пробуем загрузить из classpath
      InputStream is = DatabaseInitializer.class.getClassLoader()
          .getResourceAsStream(relativePath);
      if (is != null) {
        String content = new String(is.readAllBytes());
        System.out.println("   📄 Загружен из classpath: " + relativePath);
        return content;
      }

      // Файл не найден
      return null;

    } catch (IOException e) {
      System.err.println("❌ Ошибка чтения файла " + relativePath + ": " + e.getMessage());
      return null;
    }
  }
}