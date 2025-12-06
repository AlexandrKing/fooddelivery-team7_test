package com.team7.userstory.courieradmin;

import com.team7.service.courieradmin.CourierService;
import com.team7.service.courieradmin.OrderService;
import com.team7.service.courieradmin.CourierOrderService;
import com.team7.model.courier.Courier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class CourierUserStories {
    private static Courier currentCourier = null;
    private static CourierService courierService = new CourierService();
    private static CourierOrderService courierOrderService = new CourierOrderService();
    private static OrderService orderService = new OrderService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=".repeat(50));
        System.out.println("        🚴 СИСТЕМА КУРЬЕРОВ        ");
        System.out.println("=".repeat(50));

        while (running) {
            System.out.println("\n" + "━".repeat(40));

            if (currentCourier != null) {
                System.out.println("👤 Текущий курьер: " + currentCourier.getFullName());
                System.out.println("📱 Статус: " + getStatusEmoji(currentCourier.getStatus()) + " " +
                        translateStatus(currentCourier.getStatus()));
                System.out.println("💰 Баланс: " + currentCourier.getBalance() + " руб.");
                System.out.println("⭐ Рейтинг: " + currentCourier.getRating());
                System.out.println("1. 📋 Показать мой профиль");
                System.out.println("2. 🔄 Изменить статус работы");
                System.out.println("3. 📍 Обновить местоположение");
                System.out.println("4. 📦 Показать доступные заказы");
                System.out.println("5. 🚚 Взять заказ");
                System.out.println("6. ✅ Отметить заказ как доставленный");
                System.out.println("7. 📋 Мои заказы");
                System.out.println("8. 💰 Пополнить баланс");
                System.out.println("9. 🔐 Сменить пароль");
                System.out.println("10. 🚪 Выйти из системы");
                System.out.println("0. ❌ Выйти из программы");
            } else {
                System.out.println("1. 🔑 Войти в систему");
                System.out.println("2. 📝 Зарегистрироваться");
                System.out.println("3. 🔧 Тест БД (для разработки)");
                System.out.println("0. ❌ Выйти из программы");
            }

            System.out.println("━".repeat(40));

            int choice = readInt("Выберите действие: ");

            try {
                if (currentCourier != null) {
                    running = handleCourierMenu(choice);
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

    private static boolean handleCourierMenu(int choice) {
        switch (choice) {
            case 1:
                showMyProfile();
                break;
            case 2:
                changeWorkStatus();
                break;
            case 3:
                updateLocation();
                break;
            case 4:
                showAvailableOrders();
                break;
            case 5:
                takeOrder();
                break;
            case 6:
                completeOrder();
                break;
            case 7:
                showMyOrders();
                break;
            case 8:
                addMoney();
                break;
            case 9:
                changePassword();
                break;
            case 10:
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
                register();
                break;
            case 3:
                debugDatabase();
                break;
            case 0:
                System.out.println("👋 Выход из программы...");
                return false;
            default:
                System.out.println("❌ Неверный выбор!");
        }
        return true;
    }

    private static void login() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🔑 ВХОД КУРЬЕРА        ");
        System.out.println("=".repeat(40));

        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        Courier courier = courierService.login(username, password);
        if (courier != null) {
            currentCourier = courier;
            System.out.println("\n" + "✅".repeat(20));
            System.out.println("        🎉 ВХОД ВЫПОЛНЕН УСПЕШНО!        ");
            System.out.println("✅".repeat(20));
            System.out.println("👋 Добро пожаловать, " + courier.getFullName() + "!");
            System.out.println("💰 Ваш баланс: " + courier.getBalance() + " руб.");
            System.out.println("⭐ Ваш рейтинг: " + courier.getRating());

            // Автоматически устанавливаем статус "available" при входе
            if ("offline".equals(courier.getStatus())) {
                courierService.updateCourierStatus(courier.getId(), "available");
                currentCourier.setStatus("available");
                System.out.println("🔄 Ваш статус изменен на 'available'");
            }
        } else {
            System.out.println("❌ ОШИБКА ВХОДА: Неверное имя пользователя или пароль");
        }
    }

    private static void register() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        📝 РЕГИСТРАЦИЯ КУРЬЕРА        ");
        System.out.println("=".repeat(40));

        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль (мин. 6 символов): ");
        String password = scanner.nextLine();

        if (password.length() < 6) {
            System.out.println("❌ Пароль должен быть не менее 6 символов!");
            return;
        }

        System.out.print("ФИО: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Телефон: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Тип транспорта (car/bicycle/scooter/motorcycle): ");
        String vehicleType = scanner.nextLine().trim();

        if (vehicleType.isEmpty()) {
            vehicleType = "bicycle";
        }

        if (courierService.registerCourier(username, password, fullName, email, phone, vehicleType)) {
            System.out.println("\n✅ Регистрация успешна!");
            System.out.println("Теперь вы можете войти в систему.");
        } else {
            System.out.println("❌ Ошибка регистрации. Возможно, пользователь с таким логином или email уже существует.");
        }
    }

    private static void debugDatabase() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🔧 ТЕСТ БАЗЫ ДАННЫХ        ");
        System.out.println("=".repeat(40));

        System.out.println("1. Проверить все заказы");
        System.out.println("2. Создать тестовый заказ");
        System.out.println("3. Проверить курьеров");
        System.out.println("4. Проверить доступные заказы (детально)");
        System.out.println("5. ↩️  Назад");

        int choice = readInt("Выберите действие: ");

        switch (choice) {
            case 1:
                orderService.debugAllOrders();
                break;
            case 2:
                createTestOrder();
                break;
            case 3:
                debugCouriers();
                break;
            case 4:
                debugAvailableOrdersDetailed();
                break;
            case 5:
                return;
            default:
                System.out.println("❌ Неверный выбор!");
        }
    }

    private static void debugAvailableOrdersDetailed() {
        System.out.println("\n=== ДЕТАЛЬНАЯ ОТЛАДКА ДОСТУПНЫХ ЗАКАЗОВ ===");

        // 1. Проверяем через OrderService
        System.out.println("\n1. Через OrderService.getAvailableOrders():");
        List<OrderService.Order> orders = orderService.getAvailableOrders();
        System.out.println("   Найдено: " + orders.size() + " заказов");

        if (!orders.isEmpty()) {
            System.out.println("   Список доступных заказов:");
            for (OrderService.Order order : orders) {
                System.out.println("   - #" + order.getId() +
                        ", сумма: " + order.getTotalAmount() +
                        ", ресторан: " + order.getRestaurantName());
            }
        }

        // 2. Прямой SQL запрос
        System.out.println("\n2. Прямой SQL запрос заказов PENDING:");
        try {
            String sql = "SELECT o.id, o.status, o.total_amount, o.created_at, " +
                    "u.full_name as client, r.name as restaurant " +
                    "FROM orders o " +
                    "LEFT JOIN users u ON o.user_id = u.id " +
                    "LEFT JOIN restaurants r ON o.restaurant_id = r.id " +
                    "WHERE o.status = 'PENDING'";
            var conn = com.team7.service.config.DatabaseConfig.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("   - #" + rs.getLong("id") +
                        ", статус: " + rs.getString("status") +
                        ", сумма: " + rs.getBigDecimal("total_amount") +
                        ", клиент: " + rs.getString("client") +
                        ", ресторан: " + rs.getString("restaurant"));
            }

            if (count == 0) {
                System.out.println("   ❌ Нет заказов со статусом PENDING!");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("❌ Ошибка SQL запроса: " + e.getMessage());
        }

        // 3. Проверяем courier_assigned_orders
        System.out.println("\n3. Проверка назначений в courier_assigned_orders:");
        try {
            String sql = "SELECT order_id, courier_id, status FROM courier_assigned_orders";
            var conn = com.team7.service.config.DatabaseConfig.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);

            int assignmentCount = 0;
            while (rs.next()) {
                assignmentCount++;
                System.out.println("   Заказ #" + rs.getLong("order_id") +
                        " назначен курьеру #" + rs.getLong("courier_id") +
                        ", статус: " + rs.getString("status"));
            }

            if (assignmentCount == 0) {
                System.out.println("   ✅ Нет назначенных заказов");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void createTestOrder() {
        System.out.print("\nСоздать тестовый заказ? (да/нет): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (!confirm.equals("да") && !confirm.equals("yes")) {
            System.out.println("❌ Отмена создания заказа");
            return;
        }

        if (orderService.createTestOrder()) {
            System.out.println("✅ Тестовый заказ создан успешно!");
            System.out.println("💡 Теперь вернитесь в меню курьера и проверьте доступные заказы");
        } else {
            System.out.println("❌ Ошибка создания тестового заказа");
        }
    }

    private static void debugCouriers() {
        System.out.println("\n=== ТЕСТ КУРЬЕРОВ ===");

        List<Courier> couriers = courierService.getAllCouriers();

        System.out.printf("%-5s %-15s %-20s %-15s %-10s%n",
                "ID", "Логин", "ФИО", "Статус", "Баланс");
        System.out.println("━".repeat(70));

        for (Courier courier : couriers) {
            System.out.printf("%-5d %-15s %-20s %-15s %-10.2f%n",
                    courier.getId(),
                    courier.getUsername(),
                    courier.getFullName(),
                    courier.getStatus(),
                    courier.getBalance().doubleValue());
        }

        System.out.println("━".repeat(70));
        System.out.println("✅ Всего курьеров: " + couriers.size());

        // Проверяем доступных курьеров
        List<Courier> availableCouriers = courierService.getAvailableCouriers();
        System.out.println("✅ Доступных курьеров (status='available'): " + availableCouriers.size());
    }

    private static void showMyProfile() {
        System.out.println("\n" + "━".repeat(50));
        System.out.println("        📋 МОЙ ПРОФИЛЬ        ");
        System.out.println("━".repeat(50));

        // Обновляем данные курьера из базы
        currentCourier = courierService.getCourierById(currentCourier.getId());

        if (currentCourier == null) {
            System.out.println("❌ Ошибка загрузки профиля");
            return;
        }

        System.out.println("👤 ID: " + currentCourier.getId());
        System.out.println("👤 ФИО: " + currentCourier.getFullName());
        System.out.println("👤 Логин: " + currentCourier.getUsername());
        System.out.println("📧 Email: " + currentCourier.getEmail());
        System.out.println("📱 Телефон: " + currentCourier.getPhone());
        System.out.println("🚗 Транспорт: " +
                (currentCourier.getVehicleType() != null ? currentCourier.getVehicleType() : "не указан"));
        System.out.println("📍 Текущий статус: " + getStatusEmoji(currentCourier.getStatus()) + " " +
                translateStatus(currentCourier.getStatus()));
        System.out.println("📍 Местоположение: " +
                (currentCourier.getCurrentLocation() != null ? currentCourier.getCurrentLocation() : "не указано"));
        System.out.println("💰 Баланс: " + currentCourier.getBalance() + " руб.");
        System.out.println("⭐ Рейтинг: " + currentCourier.getRating());
        System.out.println("📦 Выполнено заказов: " + currentCourier.getCompletedOrders());

        if (currentCourier.getCreatedAt() != null) {
            System.out.println("📅 Дата регистрации: " + currentCourier.getCreatedAt());
        }

        if (currentCourier.getLastLoginAt() != null) {
            System.out.println("⏰ Последний вход: " + currentCourier.getLastLoginAt());
        }

        System.out.println("✅ Активен: " + (currentCourier.getIsActive() ? "Да" : "Нет"));
        System.out.println("━".repeat(50));
    }

    private static void changeWorkStatus() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🔄 ИЗМЕНЕНИЕ СТАТУСА РАБОТЫ        ");
        System.out.println("=".repeat(40));

        System.out.println("Текущий статус: " + getStatusEmoji(currentCourier.getStatus()) + " " +
                translateStatus(currentCourier.getStatus()));
        System.out.println("\nВыберите новый статус:");
        System.out.println("1. 🟢 available - доступен для заказов");
        System.out.println("2. 🟡 busy - занят (доставляю заказ)");
        System.out.println("3. ⚫ offline - не доступен");
        System.out.println("4. ↩️  Отмена");

        int choice = readInt("Выберите действие (1-4): ");

        String newStatus = null;
        switch (choice) {
            case 1:
                newStatus = "available";
                break;
            case 2:
                newStatus = "busy";
                break;
            case 3:
                newStatus = "offline";
                break;
            case 4:
                System.out.println("❌ Отмена изменения статуса");
                return;
            default:
                System.out.println("❌ Неверный выбор!");
                return;
        }

        if (courierService.updateCourierStatus(currentCourier.getId(), newStatus)) {
            currentCourier.setStatus(newStatus);
            System.out.println("✅ Статус успешно изменен на: " +
                    getStatusEmoji(newStatus) + " " + translateStatus(newStatus));
        } else {
            System.out.println("❌ Ошибка изменения статуса");
        }
    }

    private static void updateLocation() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        📍 ОБНОВЛЕНИЕ МЕСТОПОЛОЖЕНИЯ        ");
        System.out.println("=".repeat(40));

        System.out.print("Введите ваше текущее местоположение (адрес или координаты): ");
        String location = scanner.nextLine().trim();

        if (location.isEmpty()) {
            System.out.println("❌ Местоположение не может быть пустым!");
            return;
        }

        if (courierService.updateCourierLocation(currentCourier.getId(), location)) {
            currentCourier.setCurrentLocation(location);
            System.out.println("✅ Местоположение успешно обновлено: " + location);
        } else {
            System.out.println("❌ Ошибка обновления местоположения");
        }
    }

    private static void showAvailableOrders() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        📦 ДОСТУПНЫЕ ЗАКАЗЫ        ");
        System.out.println("=".repeat(40));

        if (!"available".equals(currentCourier.getStatus())) {
            System.out.println("❌ Вы должны быть в статусе 'available' для просмотра заказов");
            System.out.println("   Текущий статус: " + translateStatus(currentCourier.getStatus()));
            System.out.println("   💡 Используйте пункт меню '2' для изменения статуса");
            return;
        }

        System.out.println("🔍 Поиск доступных заказов...");

        List<OrderService.Order> orders = orderService.getAvailableOrders();

        if (orders.isEmpty()) {
            System.out.println("\n😔 Нет доступных заказов в данный момент.");
            System.out.println("\n💡 Возможные причины:");
            System.out.println("   • Все заказы уже назначены другим курьерам");
            System.out.println("   • Нет заказов со статусом PENDING");
            System.out.println("   • Все заказы уже доставлены или отменены");

            System.out.print("\n🛠️  Создать тестовый заказ? (да/нет): ");
            String create = scanner.nextLine().toLowerCase();

            if (create.equals("да") || create.equals("yes")) {
                createTestOrder();
                // Повторно показываем доступные заказы
                System.out.println("\nПовторный поиск заказов...");
                showAvailableOrders();
            } else {
                System.out.println("\n🛠️  Используйте '3. 🔧 Тест БД' в главном меню для диагностики");
            }
        } else {
            System.out.println("✅ Найдено " + orders.size() + " доступных заказов:");
            System.out.println("━".repeat(90));

            for (OrderService.Order order : orders) {
                System.out.println("📦 Заказ #" + order.getId());
                System.out.println("   🍽️  Ресторан: " +
                        (order.getRestaurantName() != null ? order.getRestaurantName() : "Неизвестно"));
                System.out.println("   👤 Клиент: " +
                        (order.getClientName() != null ? order.getClientName() : "Неизвестно"));
                System.out.println("   📍 Адрес доставки: " + order.getDeliveryAddress());
                System.out.println("   💰 Сумма: " + order.getTotalAmount() + " руб.");
                System.out.println("   📋 Статус заказа: " + translateOrderStatus(order.getStatus()));
                System.out.println("   📅 Дата создания: " + order.getCreatedAt());

                if (order.getDeliveryType() != null) {
                    System.out.println("   🚚 Тип доставки: " +
                            ("DELIVERY".equals(order.getDeliveryType()) ? "Доставка" : "Самовывоз"));
                }

                // Расчет прибыли
                if (order.getTotalAmount() != null) {
                    BigDecimal profit = order.getTotalAmount().multiply(new BigDecimal("0.50"));
                    System.out.println("   💸 Ваша прибыль: ~" + profit + " руб.");
                }
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            System.out.println("\n💡 Для взятия заказа используйте пункт меню '5'");
        }
    }

    private static void takeOrder() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        🚚 ВЗЯТИЕ ЗАКАЗА        ");
        System.out.println("=".repeat(40));

        if (!"available".equals(currentCourier.getStatus())) {
            System.out.println("❌ Вы должны быть в статусе 'available' для взятия заказов");
            System.out.println("   Текущий статус: " + translateStatus(currentCourier.getStatus()));
            return;
        }

        // Сначала показываем доступные заказы
        showAvailableOrders();

        List<OrderService.Order> orders = orderService.getAvailableOrders();
        if (orders.isEmpty()) {
            System.out.println("\n❌ Нет доступных заказов для взятия");
            return;
        }

        System.out.print("\nВведите ID заказа для взятия: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("❌ ID заказа не может быть пустым");
            return;
        }

        Long orderId;
        try {
            orderId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Неверный формат ID заказа");
            return;
        }

        // Проверяем, существует ли заказ
        OrderService.Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.out.println("❌ Заказ #" + orderId + " не найден");
            return;
        }

        // Проверяем, не назначен ли уже заказ
        Long assignedCourierId = orderService.getCourierIdForOrder(orderId);
        if (assignedCourierId != null) {
            System.out.println("❌ Заказ #" + orderId + " уже назначен курьеру #" + assignedCourierId);
            return;
        }

        // Проверяем статус заказа
        if (!"PENDING".equals(order.getStatus())) {
            System.out.println("❌ Заказ #" + orderId + " имеет статус '" + order.getStatus() +
                    "', можно брать только заказы со статусом PENDING");
            return;
        }

        System.out.print("\nВы уверены, что хотите взять заказ #" + orderId + "? (да/нет): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (!confirm.equals("да") && !confirm.equals("yes")) {
            System.out.println("❌ Отмена взятия заказа");
            return;
        }

        // Назначаем заказ через CourierOrderService
        if (courierOrderService.assignOrderToCourier(orderId, currentCourier.getId())) {
            System.out.println("✅ Заказ #" + orderId + " успешно взят!");
            System.out.println("💰 Приблизительная прибыль: " +
                    (order.getTotalAmount() != null ?
                            order.getTotalAmount().multiply(new BigDecimal("0.50")) + " руб." : "расчитывается"));

            // Обновляем данные курьера
            currentCourier = courierService.getCourierById(currentCourier.getId());

            System.out.println("💰 Текущий баланс: " + currentCourier.getBalance() + " руб.");
            System.out.println("🔄 Ваш статус автоматически изменен на 'busy'");
        } else {
            System.out.println("❌ Не удалось взять заказ. Возможно, он уже взят другим курьером.");
        }
    }

    private static void completeOrder() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        ✅ ЗАВЕРШЕНИЕ ДОСТАВКИ        ");
        System.out.println("=".repeat(40));

        if (!"busy".equals(currentCourier.getStatus())) {
            System.out.println("❌ Вы должны быть в статусе 'busy' для завершения доставки");
            System.out.println("   Текущий статус: " + translateStatus(currentCourier.getStatus()));
            return;
        }

        // Показываем мои текущие заказы
        showMyOrders();

        System.out.print("\nВведите ID заказа для завершения: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("❌ ID заказа не может быть пустым");
            return;
        }

        Long orderId;
        try {
            orderId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Неверный формат ID заказа");
            return;
        }

        // Проверяем, назначен ли этот заказ текущему курьеру
        Long assignedCourierId = orderService.getCourierIdForOrder(orderId);
        if (assignedCourierId == null) {
            System.out.println("❌ Заказ #" + orderId + " не назначен ни одному курьеру");
            return;
        }

        if (!assignedCourierId.equals(currentCourier.getId())) {
            System.out.println("❌ Заказ #" + orderId + " назначен другому курьеру (#" + assignedCourierId + ")");
            return;
        }

        System.out.print("Примечания к доставке (опционально): ");
        String notes = scanner.nextLine().trim();

        System.out.print("\nВы уверены, что доставили заказ #" + orderId + "? (да/нет): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (!confirm.equals("да") && !confirm.equals("yes")) {
            System.out.println("❌ Отмена завершения доставки");
            return;
        }

        if (courierOrderService.markOrderAsDelivered(orderId, notes)) {
            System.out.println("✅ Заказ #" + orderId + " успешно доставлен!");

            // Обновляем данные курьера
            currentCourier = courierService.getCourierById(currentCourier.getId());

            System.out.println("💰 Текущий баланс: " + currentCourier.getBalance() + " руб.");
            System.out.println("⭐ Текущий рейтинг: " + currentCourier.getRating());
            System.out.println("📦 Всего выполнено заказов: " + currentCourier.getCompletedOrders());
            System.out.println("🔄 Ваш статус автоматически изменен на 'available'");
        } else {
            System.out.println("❌ Ошибка завершения заказа.");
        }
    }

    private static void showMyOrders() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        📋 МОИ ЗАКАЗЫ        ");
        System.out.println("=".repeat(40));

        // Получаем заказы курьера
        List<OrderService.Order> orders = orderService.getOrdersByCourierId(currentCourier.getId());

        if (orders.isEmpty()) {
            System.out.println("😔 У вас нет назначенных заказов.");
            System.out.println("💡 Станьте 'available' и возьмите заказ из списка доступных!");
        } else {
            System.out.println("📦 У вас " + orders.size() + " назначенных заказов:");
            System.out.println("━".repeat(90));

            for (OrderService.Order order : orders) {
                System.out.println("Заказ #" + order.getId());
                System.out.println("   🍽️  Ресторан: " +
                        (order.getRestaurantName() != null ? order.getRestaurantName() : "Неизвестно"));
                System.out.println("   👤 Клиент: " +
                        (order.getClientName() != null ? order.getClientName() : "Неизвестно"));
                System.out.println("   📍 Адрес доставки: " + order.getDeliveryAddress());
                System.out.println("   💰 Сумма: " + order.getTotalAmount() + " руб.");
                System.out.println("   📋 Статус заказа: " + translateOrderStatus(order.getStatus()));
                System.out.println("   📅 Дата создания: " + order.getCreatedAt());

                // Получаем дополнительную информацию из courier_assigned_orders
                String assignmentStatus = courierOrderService.getOrderAssignmentStatus(order.getId());
                if (assignmentStatus != null) {
                    System.out.println("   🚴 Статус доставки: " + translateOrderStatus(assignmentStatus));
                }


                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }

    private static void addMoney() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        💰 ПОПОЛНЕНИЕ БАЛАНСА        ");
        System.out.println("=".repeat(40));

        System.out.print("Введите сумму для пополнения: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("❌ Сумма не может быть пустой!");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Неверный формат суммы!");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("❌ Сумма должна быть положительной!");
            return;
        }

        if (courierService.addMoneyToCourier(currentCourier.getId(), amount)) {
            currentCourier = courierService.getCourierById(currentCourier.getId());
            System.out.println("✅ Баланс пополнен на " + amount + " руб.");
            System.out.println("💰 Текущий баланс: " + currentCourier.getBalance() + " руб.");
        } else {
            System.out.println("❌ Ошибка пополнения баланса");
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

        System.out.println("⚠️  Для смены пароля обратитесь к администратору");
        System.out.println("   Или используйте SQL запрос:");
        System.out.println("   UPDATE courier_users SET password_hash = [новый_хэш] WHERE id = " + currentCourier.getId() + ";");
    }

    private static void logout() {
        // Меняем статус на offline при выходе
        if (currentCourier != null) {
            courierService.updateCourierStatus(currentCourier.getId(), "offline");
        }

        currentCourier = null;
        System.out.println("\n👋 Выход из системы...");
        System.out.println("✅ Вы успешно вышли из системы.");
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    private static String getStatusEmoji(String status) {
        if (status == null) return "❓";
        switch (status.toLowerCase()) {
            case "available": return "🟢";
            case "busy": return "🟡";
            case "offline": return "⚫";
            default: return "❓";
        }
    }

    private static String translateStatus(String status) {
        if (status == null) return "Неизвестно";
        switch (status.toLowerCase()) {
            case "available": return "Доступен";
            case "busy": return "Занят";
            case "offline": return "Не доступен";
            default: return status;
        }
    }

    private static String translateOrderStatus(String status) {
        if (status == null) return "Неизвестно";
        switch (status.toUpperCase()) {
            case "PENDING": return "🆕 Ожидание";
            case "ACCEPTED": return "✅ Принят";
            case "COOKING": return "👨‍🍳 Готовится";
            case "DELIVERING": return "🚚 Доставляется";
            case "CANCELLED": return "❌ Отменен";
            case "ASSIGNED": return "🚴 Назначен";
            case "PICKED_UP": return "📦 Забран";
            case "DELIVERED": return "✅ Доставлен";
            case "IN_PROGRESS": return "🚗 В пути";
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