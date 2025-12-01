package com.team7;

import com.team7.service.config.DatabaseInitializer;
import com.team7.userstory.client.ClientUserStories;
import com.team7.userstory.restaurant.RestaurantUserStories;
import com.team7.userstory.courieadmin.CourierUserStories;
import com.team7.userstory.courieadmin.AdminUserStories;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    // ДОБАВЬТЕ ЭТОТ БЛОК В НАЧАЛО ↓↓↓
    System.out.println("=== FOOD DELIVERY SYSTEM ===");
    System.out.println("Initializing database...");

    try {
      // Используем ваш DatabaseInitializer
      DatabaseInitializer.initialize();
      System.out.println("✅ Database initialized successfully!");
    } catch (Exception e) {
      System.err.println("⚠️ Database initialization warning: " + e.getMessage());
      System.err.println("Trying alternative method...");

      try {
        // Если DatabaseInitializer не сработал, создаём таблицы напрямую
        createBasicTables();
        System.out.println("✅ Basic tables created!");
      } catch (Exception e2) {
        System.err.println("❌ Could not create tables: " + e2.getMessage());
        System.err.println("Continuing anyway...");
      }
    }
    // КОНЕЦ ДОБАВЛЕННОГО БЛОКА ↑↑↑

    while (true) {
      System.out.println("\n=== MAIN MENU ===");
      System.out.println("1. Запуск: Ресторан");
      System.out.println("2. Запуск: Курьер");
      System.out.println("3. Запуск: Клиент");
      System.out.println("4. Запуск: Админ");
      System.out.println("5. Выход");
      System.out.print("Выберите: ");

      String input = scanner.nextLine();

      switch (input) {
        case "1":
          System.out.println("\n--- Запуск RestaurantUserStories ---");
          RestaurantUserStories.main(new String[]{});
          break;

        case "2":
          System.out.println("\n--- Запуск CourierUserStories ---");
          CourierUserStories.main(new String[]{});
          break;

        case "3":
          System.out.println("\n--- Запуск ClientsUserStories ---");
          ClientUserStories.main(new String[]{});
          break;

        case "4":
          System.out.println("\n--- Запуск AdminUserStories ---");
          AdminUserStories.main(new String[]{});
          break;

        case "5":
          System.out.println("Выход.");
          scanner.close();
          return;

        default:
          System.out.println("Неверный выбор. Введите 1-5.");
      }
    }
  }

  // ДОБАВЬТЕ ЭТОТ МЕТОД В КОНЕЦ КЛАССА ↓↓↓
  private static void createBasicTables() {
    try (Connection conn = com.team7.service.config.DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement()) {

      System.out.println("Creating basic tables...");

      // Таблица restaurant
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS restaurant (" +
              "  id BIGSERIAL PRIMARY KEY," +
              "  name VARCHAR(255) NOT NULL," +
              "  email VARCHAR(255) UNIQUE NOT NULL," +
              "  password_hash VARCHAR(255) NOT NULL," +
              "  phone VARCHAR(20)," +
              "  address TEXT," +
              "  cuisine_type VARCHAR(100)," +
              "  description TEXT," +
              "  status VARCHAR(50) DEFAULT 'pending'," +
              "  registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
              "  email_verified BOOLEAN DEFAULT false" +
              ")"
      );
      System.out.println("  ✅ Table 'restaurant' created");

      // Таблица menu_category
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS menu_category (" +
              "  id BIGSERIAL PRIMARY KEY," +
              "  restaurant_id BIGINT REFERENCES restaurant(id) ON DELETE CASCADE," +
              "  name VARCHAR(255) NOT NULL," +
              "  description TEXT" +
              ")"
      );
      System.out.println("  ✅ Table 'menu_category' created");

      // Таблица dish
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS dish (" +
              "  id BIGSERIAL PRIMARY KEY," +
              "  category_id BIGINT REFERENCES menu_category(id) ON DELETE CASCADE," +
              "  name VARCHAR(255) NOT NULL," +
              "  description TEXT," +
              "  price DECIMAL(10,2) NOT NULL," +
              "  available BOOLEAN DEFAULT true," +
              "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
              ")"
      );
      System.out.println("  ✅ Table 'dish' created");

    } catch (Exception e) {
      throw new RuntimeException("Failed to create basic tables: " + e.getMessage(), e);
    }
  }
}