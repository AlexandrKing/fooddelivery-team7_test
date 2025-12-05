package com.team7;

import com.team7.service.config.DatabaseConfig;
import com.team7.service.config.DatabaseInitializer;
import com.team7.userstory.client.ClientUserStories;
import com.team7.userstory.restaurant.RestaurantUserStories;
import com.team7.userstory.courieradmin.CourierUserStories;
import com.team7.userstory.courieradmin.AdminUserStories;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              🍽️ СИСТЕМА ДОСТАВКИ ЕДЫ 🚚                  ║");
        System.out.println("║                     Версия 1.0.0                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🏃 ПОСЛЕДОВАТЕЛЬНОСТЬ ЗАПУСКА");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Шаг 1: Проверка подключения к PostgreSQL
        System.out.println("\n[1/3] 🔍 Проверка подключения к базе данных...");
        if (!checkDatabaseConnection()) {
            System.err.println("\n❌ НЕ УДАЛОСЬ ПОДКЛЮЧИТЬСЯ К БАЗЕ ДАННЫХ!");
            System.err.println("\nПроверьте:");
            System.err.println("   1. Docker контейнер запущен: docker ps | grep restaurant_db");
            System.err.println("   2. Если нет, запустите: docker start restaurant_db");
            System.err.println("   3. Параметры в DatabaseConfig.java совпадают с docker-compose.yml");
            scanner.close();
            System.exit(1);
        }
        System.out.println("✅ Подключение к PostgreSQL установлено");

        // Шаг 2: Инициализация БД (drop → create → insert → проверка)
        System.out.println("\n[2/3] 🗄️  Инициализация базы данных...");
        try {
            DatabaseInitializer.initialize();
            System.out.println("✅ База данных успешно инициализирована");
            System.out.println("   ✓ Таблицы созданы");
            System.out.println("   ✓ Тестовые данные загружены");
            System.out.println("   ✓ Структура проверена");
        } catch (Exception e) {
            System.err.println("\n❌ ОШИБКА ИНИЦИАЛИЗАЦИИ БАЗЫ ДАННЫХ!");
            System.err.println("Причина: " + e.getMessage());

            // Проверяем, если ошибка связана с уже существующими таблицами
            if (e.getMessage().contains("уже существует") || e.getMessage().contains("already exists")) {
                System.out.println("\n⚠️  Таблицы уже существуют. Пропускаем создание...");
                System.out.println("✅ Продолжаем работу с существующей БД");
            } else {
                scanner.close();
                System.exit(1);
            }
        }

        // Шаг 3: Меню выбора User Stories
        System.out.println("\n[3/3] 🎮 Запуск панели выбора User Stories...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        boolean running = true;
        while (running) {
            displayUserStoriesMenu();

            System.out.print("\n📋 Выберите User Story для запуска (1-5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n" + "👤".repeat(35));
                    System.out.println("        ЗАПУСК КЛИЕНТСКИХ USER STORIES");
                    System.out.println("👤".repeat(35));
                    ClientUserStories.main(new String[]{});
                    break;

                case "2":
                    System.out.println("\n" + "🍽️".repeat(35));
                    System.out.println("        ЗАПУСК РЕСТОРАННЫХ USER STORIES");
                    System.out.println("🍽️".repeat(35));
                    RestaurantUserStories.main(new String[]{});
                    break;

                case "3":
                    System.out.println("\n" + "🚴".repeat(35));
                    System.out.println("        ЗАПУСК КУРЬЕРСКИХ USER STORIES");
                    System.out.println("🚴".repeat(35));
                    CourierUserStories.main(new String[]{});
                    break;

                case "4":
                    System.out.println("\n" + "👨‍💼".repeat(35));
                    System.out.println("        ЗАПУСК АДМИНИСТРАТИВНЫХ USER STORIES");
                    System.out.println("👨‍💼".repeat(35));
                    AdminUserStories.main(new String[]{});
                    break;

                case "5":
                    System.out.println("\n" + "=".repeat(70));
                    System.out.println("                  ЗАВЕРШЕНИЕ РАБОТЫ");
                    System.out.println("=".repeat(70));
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Введите число от 1 до 5.");
            }

            if (running && !choice.equals("5")) {
                System.out.println("\n" + "─".repeat(70));
                System.out.print("↩️  Вернуться в главное меню? (да/нет): ");
                String answer = scanner.nextLine().trim().toLowerCase();

                if (answer.equals("нет") || answer.equals("н") || answer.equals("no") || answer.equals("n")) {
                    running = false;
                    System.out.println("\n👋 Завершение работы...");
                }
            }
        }

        scanner.close();
        System.out.println("\n💫 Спасибо за использование системы доставки еды!");
        System.exit(0);
    }

    private static boolean checkDatabaseConnection() {
        System.out.println("\n🔍 ПРОВЕРКА ПОДКЛЮЧЕНИЯ К POSTGRESQL:");
        System.out.println("   Хост: localhost:5432");
        System.out.println("   База данных: restaurant_db");
        System.out.println("   Пользователь: restaurant_user");

        try {
            // Проверяем подключение через DatabaseConfig
            if (DatabaseConfig.testConnection()) {
                System.out.println("✅ Подключение успешно!");

                // Дополнительная проверка контейнера
                try {
                    Process process = Runtime.getRuntime().exec("docker ps --filter name=restaurant_db --format \"{{.Names}}\\t{{.Status}}\"");
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream())
                    );
                    String line = reader.readLine();
                    process.waitFor();

                    if (line != null && line.contains("restaurant_db")) {
                        System.out.println("✅ Контейнер PostgreSQL: " + line);
                    }
                } catch (Exception e) {
                    // Игнорируем, если не удалось проверить через docker
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при проверке подключения: " + e.getMessage());
            return false;
        }
    }

    private static void displayUserStoriesMenu() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("              ПАНЕЛЬ ВЫБОРА USER STORIES");
        System.out.println("═".repeat(70));
        System.out.println("\nВыберите User Story для запуска:");
        System.out.println();
        System.out.println("  1. 👤 КЛИЕНТСКИЕ USER STORIES");
        System.out.println("     - Просмотр ресторанов и меню");
        System.out.println("     - Работа с корзиной и заказами");
        System.out.println("     - Регистрация и аутентификация");
        System.out.println();
        System.out.println("  2. 🍽️  РЕСТОРАННЫЕ USER STORIES");
        System.out.println("     - Управление меню ресторана");
        System.out.println("     - Просмотр заказов и статистики");
        System.out.println("     - Управление профилем ресторана");
        System.out.println();
        System.out.println("  3. 🚴 КУРЬЕРСКИЕ USER STORIES");
        System.out.println("     - Принятие и доставка заказов");
        System.out.println("     - Просмотр доступных заказов");
        System.out.println("     - Обновление статусов доставки");
        System.out.println();
        System.out.println("  4. 👨‍💼 АДМИНИСТРАТИВНЫЕ USER STORIES");
        System.out.println("     - Управление пользователями");
        System.out.println("     - Мониторинг системы");
        System.out.println("     - Аналитика и отчеты");
        System.out.println();
        System.out.println("  5. ⏹️  ВЫХОД");
        System.out.println("═".repeat(70));
    }
}