package com.team7.userstory.courieradmin;

import com.team7.service.courieradmin.AdminService;
import com.team7.service.courieradmin.CourierService;
import com.team7.service.courieradmin.ReviewService;
import com.team7.service.courieradmin.ClientService;
import com.team7.service.courieradmin.RestaurantService;
import com.team7.service.courieradmin.OrderService;
import com.team7.service.courieradmin.CourierOrderService;
import com.team7.model.admin.Admin;
import com.team7.model.courier.Courier;
import com.team7.model.review.Review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class AdminUserStories {
    private static Admin currentAdmin = null;
    private static AdminService adminService = new AdminService();
    private static CourierService courierService = new CourierService();
    private static ReviewService reviewService = new ReviewService();
    private static ClientService clientService = new ClientService();
    private static RestaurantService restaurantService = new RestaurantService();
    private static OrderService orderService = new OrderService();
    private static CourierOrderService courierOrderService = new CourierOrderService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=".repeat(50));
        System.out.println("        👨‍💼 АДМИНИСТРАТИВНАЯ ПАНЕЛЬ        ");
        System.out.println("=".repeat(50));

        while (running) {
            System.out.println("\n" + "━".repeat(40));

            if (currentAdmin != null) {
                System.out.println("👤 Текущий администратор: " + currentAdmin.getFullName());
                System.out.println("Роль: " + currentAdmin.getRole());
                System.out.println("=== ОСНОВНОЕ МЕНЮ ===");
                System.out.println("1. 👥 Управление курьерами");
                System.out.println("2. 👥 Управление клиентами");
                System.out.println("3. 🍽️  Управление ресторанами");
                System.out.println("4. ⭐ Управление отзывами");
                System.out.println("5. 📦 Управление заказами");
                System.out.println("6. 💰 Управление финансами");
                System.out.println("7. 📊 Статистика системы");
                System.out.println("=== СИСТЕМА ===");
                System.out.println("8. 🔐 Сменить пароль");
                System.out.println("9. 🚪 Выйти из системы");
                System.out.println("0. ❌ Выйти из программы");
            } else {
                System.out.println("1. 🔑 Войти в систему");
                System.out.println("2. ❌ Выйти из программы");
            }

            System.out.println("━".repeat(40));

            int choice = readInt("Выберите действие: ");

            try {
                if (currentAdmin != null) {
                    running = handleAdminMenu(choice);
                } else {
                    running = handleLoginMenu(choice);
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("\n👋 Программа завершена.");
    }

    private static boolean handleAdminMenu(int choice) {
        switch (choice) {
            case 1:
                manageCouriers();
                break;
            case 2:
                manageClients();
                break;
            case 3:
                manageRestaurants();
                break;
            case 4:
                manageReviews();
                break;
            case 5:
                manageOrders();
                break;
            case 6:
                manageFinance();
                break;
            case 7:
                showStatistics();
                break;
            case 8:
                changePassword();
                break;
            case 9:
                logout();
                return true;
            case 0:
                System.out.println("👋 Выход из программы...");
                return false;
            default:
                System.out.println("❌ Неверный выбор!");
        }
        return true;
    }

    private static boolean handleLoginMenu(int choice) {
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                System.out.println("👋 Выход из программы...");
                return false;
            default:
                System.out.println("❌ Неверный выбор!");
        }
        return true;
    }

    private static void login() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🔑 ВХОД АДМИНИСТРАТОРА        ");
        System.out.println("=".repeat(40));

        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        Admin admin = adminService.login(username, password);
        if (admin != null) {
            currentAdmin = admin;
            System.out.println("\n" + "✅".repeat(20));
            System.out.println("        🎉 ВХОД ВЫПОЛНЕН УСПЕШНО!        ");
            System.out.println("✅".repeat(20));
            System.out.println("👋 Добро пожаловать, " + admin.getFullName() + "!");
            System.out.println("👑 Роль: " + admin.getRole());
        } else {
            System.out.println("❌ ОШИБКА ВХОДА: Неверное имя пользователя или пароль");
        }
    }

    // === УПРАВЛЕНИЕ КУРЬЕРАМИ ===
    private static void manageCouriers() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        👥 УПРАВЛЕНИЕ КУРЬЕРАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 📋 Показать всех курьеров");
            System.out.println("2. ✅ Показать доступных курьеров");
            System.out.println("3. ⛔ Заблокировать курьера");
            System.out.println("4. ✅ Разблокировать курьера");
            System.out.println("5. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    showAllCouriers();
                    break;
                case 2:
                    showAvailableCouriers();
                    break;
                case 3:
                    blockCourier();
                    break;
                case 4:
                    unblockCourier();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showAllCouriers() {
        List<Courier> couriers = courierService.getAllCouriers();
        System.out.println("\n" + "━".repeat(100));
        System.out.println("        👥 ВСЕ КУРЬЕРЫ (" + couriers.size() + ")        ");
        System.out.println("━".repeat(100));

        if (couriers.isEmpty()) {
            System.out.println("😔 Нет зарегистрированных курьеров");
        } else {
            System.out.printf("%-5s %-15s %-20s %-15s %-10s %-10s %-15s\n",
                    "ID", "Логин", "ФИО", "Статус", "Баланс", "Заказов", "Рейтинг");
            System.out.println("━".repeat(100));

            for (Courier courier : couriers) {
                String statusEmoji = "🟢";
                if ("busy".equals(courier.getStatus())) statusEmoji = "🟡";
                if ("offline".equals(courier.getStatus())) statusEmoji = "⚫";

                System.out.printf("%-5d %-15s %-20s %-15s %-10.2f %-10d %-15s\n",
                        courier.getId(),
                        courier.getUsername(),
                        courier.getFullName().length() > 20 ?
                                courier.getFullName().substring(0, 17) + "..." : courier.getFullName(),
                        statusEmoji + " " + courier.getStatus(),
                        courier.getBalance().doubleValue(),
                        courier.getCompletedOrders(),
                        "⭐ " + courier.getRating());
            }
        }
        System.out.println("━".repeat(100));
    }

    private static void showAvailableCouriers() {
        List<Courier> couriers = courierService.getAvailableCouriers();
        System.out.println("\n" + "━".repeat(90));
        System.out.println("        ✅ ДОСТУПНЫЕ КУРЬЕРЫ (" + couriers.size() + ")        ");
        System.out.println("━".repeat(90));

        if (couriers.isEmpty()) {
            System.out.println("😔 Нет доступных курьеров");
        } else {
            System.out.printf("%-5s %-15s %-20s %-15s %-10s %-15s\n",
                    "ID", "Логин", "ФИО", "Транспорт", "Заказов", "Рейтинг");
            System.out.println("━".repeat(90));

            for (Courier courier : couriers) {
                System.out.printf("%-5d %-15s %-20s %-15s %-10d %-15s\n",
                        courier.getId(),
                        courier.getUsername(),
                        courier.getFullName().length() > 20 ?
                                courier.getFullName().substring(0, 17) + "..." : courier.getFullName(),
                        getVehicleEmoji(courier.getVehicleType()) + " " +
                                (courier.getVehicleType() != null ? courier.getVehicleType() : "не указан"),
                        courier.getCompletedOrders(),
                        "⭐ " + courier.getRating());
            }
        }
        System.out.println("━".repeat(90));
    }

    private static void blockCourier() {
        System.out.print("\nВведите ID курьера для блокировки: ");
        Long courierId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (courierService.blockCourier(courierId)) {
            System.out.println("✅ Курьер #" + courierId + " заблокирован");
        } else {
            System.out.println("❌ Ошибка блокировки курьера");
        }
    }

    private static void unblockCourier() {
        System.out.print("\nВведите ID курьера для разблокировки: ");
        Long courierId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (courierService.unblockCourier(courierId)) {
            System.out.println("✅ Курьер #" + courierId + " разблокирован");
        } else {
            System.out.println("❌ Ошибка разблокировки курьера");
        }
    }

    // === УПРАВЛЕНИЕ КЛИЕНТАМИ ===
    private static void manageClients() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        👥 УПРАВЛЕНИЕ КЛИЕНТАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 📋 Показать всех клиентов");
            System.out.println("2. ⛔ Деактивировать клиента");
            System.out.println("3. ✅ Активировать клиента");
            System.out.println("4. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    showAllClients();
                    break;
                case 2:
                    deactivateClient();
                    break;
                case 3:
                    activateClient();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showAllClients() {
        List<ClientService.Client> clients = clientService.getAllClients();
        System.out.println("\n" + "━".repeat(120));
        System.out.println("        👥 ВСЕ КЛИЕНТЫ (" + clients.size() + ")        ");
        System.out.println("━".repeat(120));

        if (clients.isEmpty()) {
            System.out.println("😔 Нет зарегистрированных клиентов");
        } else {
            System.out.printf("%-5s %-15s %-20s %-25s %-15s %-25s %-10s\n",
                    "ID", "Логин", "ФИО", "Email", "Телефон", "Адрес", "Статус");
            System.out.println("━".repeat(120));

            for (ClientService.Client client : clients) {
                String status = client.getIsActive() ? "✅ Активен" : "⛔ Неактивен";
                System.out.printf("%-5d %-15s %-20s %-25s %-15s %-25s %-10s\n",
                        client.getId(),
                        client.getUsername(),
                        client.getFullName(),
                        client.getEmail(),
                        client.getPhone(),
                        client.getAddress() != null && client.getAddress().length() > 25 ?
                                client.getAddress().substring(0, 22) + "..." : (client.getAddress() != null ? client.getAddress() : "не указан"),
                        status);
            }
        }
        System.out.println("━".repeat(120));
    }

    private static void deactivateClient() {
        System.out.print("\nВведите ID клиента для деактивации: ");
        Long clientId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (clientService.deactivateClient(clientId)) {
            System.out.println("✅ Клиент #" + clientId + " деактивирован");
        } else {
            System.out.println("❌ Ошибка деактивации клиента");
        }
    }

    private static void activateClient() {
        System.out.print("\nВведите ID клиента для активации: ");
        Long clientId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (clientService.activateClient(clientId)) {
            System.out.println("✅ Клиент #" + clientId + " активирован");
        } else {
            System.out.println("❌ Ошибка активации клиента");
        }
    }

    // === УПРАВЛЕНИЕ РЕСТОРАНАМИ ===
    private static void manageRestaurants() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        🍽️  УПРАВЛЕНИЕ РЕСТОРАНАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 📋 Показать все рестораны");
            System.out.println("2. ⛔ Деактивировать ресторан");
            System.out.println("3. ✅ Активировать ресторан");
            System.out.println("4. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    showAllRestaurants();
                    break;
                case 2:
                    deactivateRestaurant();
                    break;
                case 3:
                    activateRestaurant();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showAllRestaurants() {
        List<RestaurantService.Restaurant> restaurants = restaurantService.getAllRestaurants();
        System.out.println("\n" + "━".repeat(120));
        System.out.println("        🍽️  ВСЕ РЕСТОРАНЫ (" + restaurants.size() + ")        ");
        System.out.println("━".repeat(120));

        if (restaurants.isEmpty()) {
            System.out.println("😔 Нет зарегистрированных ресторанов");
        } else {
            System.out.printf("%-5s %-25s %-20s %-15s %-25s %-10s %-10s\n",
                    "ID", "Название", "Кухня", "Телефон", "Адрес", "Рейтинг", "Статус");
            System.out.println("━".repeat(120));

            for (RestaurantService.Restaurant restaurant : restaurants) {
                String status = restaurant.getIsActive() ? "✅ Активен" : "⛔ Неактивен";
                System.out.printf("%-5d %-25s %-20s %-15s %-25s %-10.1f %-10s\n",
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getCuisineType(),
                        restaurant.getPhone(),
                        restaurant.getAddress().length() > 25 ?
                                restaurant.getAddress().substring(0, 22) + "..." : restaurant.getAddress(),
                        restaurant.getRating() != null ? restaurant.getRating() : 0.0,
                        status);
            }
        }
        System.out.println("━".repeat(120));
    }

    private static void deactivateRestaurant() {
        System.out.print("\nВведите ID ресторана для деактивации: ");
        Long restaurantId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (restaurantService.deactivateRestaurant(restaurantId)) {
            System.out.println("✅ Ресторан #" + restaurantId + " деактивирован");
        } else {
            System.out.println("❌ Ошибка деактивации ресторана");
        }
    }

    private static void activateRestaurant() {
        System.out.print("\nВведите ID ресторана для активации: ");
        Long restaurantId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (restaurantService.activateRestaurant(restaurantId)) {
            System.out.println("✅ Ресторан #" + restaurantId + " активирован");
        } else {
            System.out.println("❌ Ошибка активации ресторана");
        }
    }

    // === УПРАВЛЕНИЕ ОТЗЫВАМИ ===
    private static void manageReviews() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        ⭐ УПРАВЛЕНИЕ ОТЗЫВАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 📋 Показать все отзывы");
            System.out.println("2. ✅ Показать активные отзывы");
            System.out.println("3. ⛔ Удалить отзыв");
            System.out.println("4. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    showAllReviews();
                    break;
                case 2:
                    showActiveReviews();
                    break;
                case 3:
                    deleteReview();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        System.out.println("\n" + "━".repeat(100));
        System.out.println("        📋 ВСЕ ОТЗЫВЫ (" + reviews.size() + ")        ");
        System.out.println("━".repeat(100));

        if (reviews.isEmpty()) {
            System.out.println("😔 Отзывов нет");
        } else {
            for (Review review : reviews) {
                String status = review.getIsActive() ? "✅ Активен" : "⛔ Скрыт";
                System.out.println("📝 Отзыв #" + review.getId());
                System.out.println("   📦 Заказ ID: " + review.getOrderId());
                System.out.println("   👤 Клиент ID: " + review.getUserId());
                System.out.println("   🍽️  Ресторан ID: " + review.getRestaurantId());
                System.out.println("   🚴 Курьер ID: " + review.getCourierId());
                System.out.println("   ⭐ Рейтинг: " + getStars(review.getRating()));
                System.out.println("   💬 Комментарий: " +
                        (review.getComment() != null && !review.getComment().isEmpty() ?
                                review.getComment() : "Нет комментария"));
                System.out.println("   📅 Дата: " + review.getCreatedAt());
                System.out.println("   📋 Статус: " + status);
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }

    private static void showActiveReviews() {
        List<Review> reviews = reviewService.getActiveReviews();
        System.out.println("\n" + "━".repeat(100));
        System.out.println("        ✅ АКТИВНЫЕ ОТЗЫВЫ (" + reviews.size() + ")        ");
        System.out.println("━".repeat(100));

        if (reviews.isEmpty()) {
            System.out.println("😔 Активных отзывов нет");
        } else {
            for (Review review : reviews) {
                System.out.println("⭐ " + getStars(review.getRating()));
                System.out.println("💬 " + (review.getComment() != null ?
                        (review.getComment().length() > 80 ?
                                review.getComment().substring(0, 77) + "..." : review.getComment())
                        : "Нет комментария"));
                System.out.println("📅 " + review.getCreatedAt().toLocalDate());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }

    private static void deleteReview() {
        System.out.print("\nВведите ID отзыва для удаления: ");
        Long reviewId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        System.out.print("Вы уверены, что хотите удалить отзыв #" + reviewId + "? (да/нет): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (confirm.equals("да") || confirm.equals("yes")) {
            if (reviewService.deleteReview(reviewId)) {
                System.out.println("✅ Отзыв #" + reviewId + " удален");
            } else {
                System.out.println("❌ Ошибка удаления отзыва");
            }
        } else {
            System.out.println("❌ Удаление отменено");
        }
    }

    // === УПРАВЛЕНИЕ ЗАКАЗАМИ ===
    private static void manageOrders() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        📦 УПРАВЛЕНИЕ ЗАКАЗАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 📋 Показать все заказы");
            System.out.println("2. ❌ Отменить заказ");
            System.out.println("3. 🚴 Назначить заказ курьеру");
            System.out.println("4. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    showAllOrders();
                    break;
                case 2:
                    cancelOrder();
                    break;
                case 3:
                    assignOrderToCourier();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void showAllOrders() {
        List<OrderService.Order> orders = orderService.getAllOrders();
        System.out.println("\n" + "━".repeat(120));
        System.out.println("        📦 ВСЕ ЗАКАЗЫ (" + orders.size() + ")        ");
        System.out.println("━".repeat(120));

        if (orders.isEmpty()) {
            System.out.println("😔 Заказов нет");
        } else {
            for (OrderService.Order order : orders) {
                System.out.println("📦 Заказ #" + order.getId());
                System.out.println("   👤 Клиент: " + (order.getClientName() != null ? order.getClientName() : "Неизвестно"));
                System.out.println("   🍽️  Ресторан: " + (order.getRestaurantName() != null ? order.getRestaurantName() : "Неизвестно"));

                // Получаем имя курьера через courier_assigned_orders
                Long courierId = orderService.getCourierIdForOrder(order.getId());
                String courierName = "Не назначен";
                if (courierId != null) {
                    Courier courier = courierService.getCourierById(courierId);
                    if (courier != null) {
                        courierName = courier.getFullName();
                    }
                }
                System.out.println("   🚴 Курьер: " + courierName);

                System.out.println("   💰 Сумма: " + order.getTotalAmount() + " руб.");
                System.out.println("   📍 Адрес доставки: " + order.getDeliveryAddress());
                System.out.println("   📋 Статус: " + translateOrderStatus(order.getStatus()));
                System.out.println("   📅 Дата создания: " + order.getCreatedAt());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }

    private static void cancelOrder() {
        System.out.print("\nВведите ID заказа для отмены: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (orderService.cancelOrder(orderId)) {
            System.out.println("✅ Заказ #" + orderId + " отменен");
        } else {
            System.out.println("❌ Ошибка отмены заказа");
        }
    }

    private static void assignOrderToCourier() {
        System.out.print("\nВведите ID заказа для назначения: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        System.out.print("Введите ID курьера: ");
        Long courierId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        // Используем CourierOrderService для назначения
        if (courierOrderService.assignOrderToCourier(orderId, courierId)) {
            System.out.println("✅ Заказ #" + orderId + " назначен курьеру #" + courierId);
        } else {
            System.out.println("❌ Ошибка назначения заказа");
        }
    }

    // === УПРАВЛЕНИЕ ФИНАНСАМИ ===
    private static void manageFinance() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("        💰 УПРАВЛЕНИЕ ФИНАНСАМИ        ");
            System.out.println("=".repeat(40));
            System.out.println("1. 💵 Начислить комиссию всем курьерам за заказ");
            System.out.println("2. 💰 Начислить комиссию конкретному курьеру");
            System.out.println("3. ↩️  Назад");

            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    addCommissionToAll();
                    break;
                case 2:
                    addCommissionToCourier();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("❌ Неверный выбор!");
            }
        }
    }

    private static void addCommissionToAll() {
        System.out.print("\nВведите ID заказа для начисления комиссии всем курьерам: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (courierService.addCommissionToAll(orderId)) {
            System.out.println("✅ Комиссия 10 руб. начислена всем активным курьерам за заказ #" + orderId);
        } else {
            System.out.println("❌ Ошибка начисления комиссии");
        }
    }

    private static void addCommissionToCourier() {
        System.out.print("\nВведите ID заказа: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        System.out.print("Введите ID курьера: ");
        Long courierId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (courierService.addCommissionToCourier(orderId, courierId)) {
            System.out.println("✅ Комиссия начислена курьеру #" + courierId + " за заказ #" + orderId);
        } else {
            System.out.println("❌ Ошибка начисления комиссии");
        }
    }

    // === СТАТИСТИКА ===
    private static void showStatistics() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("        📈 СТАТИСТИКА СИСТЕМЫ        ");
        System.out.println("=".repeat(50));

        try {
            // Курьеры
            List<Courier> couriers = courierService.getAllCouriers();
            long totalCouriers = couriers.size();
            long activeCouriers = couriers.stream().filter(Courier::getIsActive).count();
            long availableCouriers = couriers.stream()
                    .filter(c -> "available".equals(c.getStatus()))
                    .count();

            BigDecimal totalCourierBalance = couriers.stream()
                    .map(Courier::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("\n👥 КУРЬЕРЫ:");
            System.out.println("   Всего: " + totalCouriers);
            System.out.println("   Активных: " + activeCouriers);
            System.out.println("   Доступных сейчас: " + availableCouriers);
            System.out.println("   Общий баланс: " + totalCourierBalance + " руб.");

            // Клиенты
            List<ClientService.Client> clients = clientService.getAllClients();
            long totalClients = clients.size();
            long activeClients = clients.stream()
                    .filter(ClientService.Client::getIsActive)
                    .count();

            System.out.println("\n👤 КЛИЕНТЫ:");
            System.out.println("   Всего: " + totalClients);
            System.out.println("   Активных: " + activeClients);

            // Рестораны
            List<RestaurantService.Restaurant> restaurants = restaurantService.getAllRestaurants();
            long totalRestaurants = restaurants.size();
            long activeRestaurants = restaurants.stream()
                    .filter(RestaurantService.Restaurant::getIsActive)
                    .count();

            double averageRestaurantRating = restaurants.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToDouble(RestaurantService.Restaurant::getRating)
                    .average()
                    .orElse(0.0);

            System.out.println("\n🍽️  РЕСТОРАНЫ:");
            System.out.println("   Всего: " + totalRestaurants);
            System.out.println("   Активных: " + activeRestaurants);
            System.out.println("   Средний рейтинг: " + String.format("%.1f", averageRestaurantRating));

            // Заказы
            List<OrderService.Order> orders = orderService.getAllOrders();
            long totalOrders = orders.size();
            long newOrders = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
            long acceptedOrders = orders.stream().filter(o -> "ACCEPTED".equals(o.getStatus())).count();
            long deliveredOrders = orders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
            long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

            BigDecimal totalRevenue = orders.stream()
                    .filter(o -> "DELIVERED".equals(o.getStatus()) && o.getTotalAmount() != null)
                    .map(OrderService.Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("\n📦 ЗАКАЗЫ:");
            System.out.println("   Всего: " + totalOrders);
            System.out.println("   Новых (PENDING): " + newOrders);
            System.out.println("   Принятых (ACCEPTED): " + acceptedOrders);
            System.out.println("   Доставленных (DELIVERED): " + deliveredOrders);
            System.out.println("   Отмененных (CANCELLED): " + cancelledOrders);
            System.out.println("   Общая выручка: " + totalRevenue + " руб.");

            // Отзывы
            List<Review> reviews = reviewService.getAllReviews();
            long totalReviews = reviews.size();

            double averageReviewRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            System.out.println("\n⭐ ОТЗЫВЫ:");
            System.out.println("   Всего: " + totalReviews);
            System.out.println("   Средний рейтинг: " + String.format("%.1f", averageReviewRating));

            System.out.println("\n⏰ СИСТЕМА:");
            System.out.println("   Текущее время: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        } catch (Exception e) {
            System.out.println("❌ Ошибка получения статистики: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void changePassword() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🔐 СМЕНА ПАРОЛЯ        ");
        System.out.println("=".repeat(40));

        System.out.print("Текущий пароль: ");
        String currentPassword = scanner.nextLine();

        System.out.print("Новый пароль (мин. 6 символов): ");
        String newPassword = scanner.nextLine();

        System.out.print("Подтверждение нового пароля: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("❌ Пароли не совпадают!");
            return;
        }

        if (newPassword.length() < 6) {
            System.out.println("❌ Пароль должен быть не менее 6 символов!");
            return;
        }

        if (adminService.changePassword(currentAdmin.getId(), currentPassword, newPassword)) {
            System.out.println("✅ Пароль успешно изменен!");
        } else {
            System.out.println("❌ Ошибка смены пароля. Проверьте текущий пароль.");
        }
    }

    private static void logout() {
        currentAdmin = null;
        System.out.println("\n👋 Выход из системы...");
        System.out.println("✅ Вы успешно вышли из системы.");
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    private static String getVehicleEmoji(String vehicleType) {
        if (vehicleType == null) return "🚶";
        switch (vehicleType.toLowerCase()) {
            case "car": return "🚗";
            case "bicycle": return "🚲";
            case "scooter": return "🛵";
            case "motorcycle": return "🏍️";
            default: return "🚶";
        }
    }

    private static String getStars(int rating) {
        return "⭐".repeat(rating) + "☆".repeat(5 - rating);
    }

    private static String translateOrderStatus(String status) {
        if (status == null) return "Неизвестно";
        switch (status.toUpperCase()) {
            case "PENDING": return "🆕 Ожидание";
            case "ACCEPTED": return "✅ Принят";
            case "COOKING": return "👨‍🍳 Готовится";
            case "DELIVERING": return "🚚 Доставляется";
            case "DELIVERED": return "✅ Доставлен";
            case "CANCELLED": return "❌ Отменен";
            default: return status;
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка: Введите целое число!");
            }
        }
    }
}