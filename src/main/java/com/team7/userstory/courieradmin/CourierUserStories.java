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
                System.out.println("7. 💰 Пополнить баланс");
                System.out.println("8. 🔐 Сменить пароль");
                System.out.println("9. 🚪 Выйти из системы");
                System.out.println("0. ❌ Выйти из программы");
            } else {
                System.out.println("1. 🔑 Войти в систему");
                System.out.println("2. 📝 Зарегистрироваться");
                System.out.println("3. ❌ Выйти из программы");
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
                addMoney();
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
                register();
                break;
            case 3:
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

        if (courierService.registerCourier(username, password, fullName, email, phone, vehicleType)) {
            System.out.println("\n✅ Регистрация успешна!");
            System.out.println("Теперь вы можете войти в систему.");
        } else {
            System.out.println("❌ Ошибка регистрации. Возможно, пользователь с таким логином или email уже существует.");
        }
    }

    private static void showMyProfile() {
        System.out.println("\n" + "━".repeat(50));
        System.out.println("        📋 МОЙ ПРОФИЛЬ        ");
        System.out.println("━".repeat(50));
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
        System.out.println("📅 Дата регистрации: " + currentCourier.getCreatedAt());
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
            return;
        }

        List<OrderService.Order> orders = orderService.getAvailableOrders();

        if (orders.isEmpty()) {
            System.out.println("😔 Нет доступных заказов в данный момент.");
        } else {
            System.out.println("📦 Найдено " + orders.size() + " доступных заказов:");
            System.out.println("━".repeat(80));

            for (OrderService.Order order : orders) {
                System.out.println("Заказ #" + order.getId());
                System.out.println("   🍽️  Ресторан: " + order.getRestaurantName());
                System.out.println("   👤 Клиент: " + order.getClientName());
                System.out.println("   📍 Адрес доставки: " + order.getDeliveryAddress());
                System.out.println("   💰 Сумма: " + order.getTotalAmount() + " руб.");
                System.out.println("   📅 Дата создания: " + order.getCreatedAt());
                System.out.println("   💸 Ваша прибыль: ~" +
                    (order.getTotalAmount() != null ?
                        order.getTotalAmount().multiply(new BigDecimal("0.50")) + " руб." : "неизвестно"));
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
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

        // Показываем доступные заказы
        showAvailableOrders();

        List<OrderService.Order> orders = orderService.getAvailableOrders();
        if (orders.isEmpty()) {
            return;
        }

        System.out.print("\nВведите ID заказа для взятия: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        if (orderService.takeOrder(orderId, currentCourier.getId())) {
            System.out.println("✅ Заказ #" + orderId + " успешно взят!");

            // Меняем статус курьера на "busy"
            courierService.updateCourierStatus(currentCourier.getId(), "busy");
            currentCourier.setStatus("busy");

            // Начисляем 50 руб за взятие заказа
            courierService.addMoneyToCourier(currentCourier.getId(), new BigDecimal("50"));
            currentCourier = courierService.getCourierById(currentCourier.getId());

            System.out.println("💰 +50 руб. за взятие заказа!");
            System.out.println("💰 Текущий баланс: " + currentCourier.getBalance() + " руб.");
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

        System.out.print("Введите ID заказа для завершения: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine(); // consume newline

        System.out.print("Примечания к доставке (опционально): ");
        String notes = scanner.nextLine().trim();

        if (orderService.completeOrder(orderId)) {
            System.out.println("✅ Заказ #" + orderId + " успешно доставлен!");

            // Начисляем комиссию за доставку (50% от суммы заказа)
            BigDecimal orderAmount = orderService.getOrderProfit(orderId);
            if (orderAmount.compareTo(BigDecimal.ZERO) > 0) {
                courierService.addMoneyToCourier(currentCourier.getId(), orderAmount);
                currentCourier = courierService.getCourierById(currentCourier.getId());
                System.out.println("💰 +" + orderAmount + " руб. за доставку!");
            }

            // Меняем статус курьера на "available"
            courierService.updateCourierStatus(currentCourier.getId(), "available");
            currentCourier.setStatus("available");

            System.out.println("💰 Текущий баланс: " + currentCourier.getBalance() + " руб.");
            System.out.println("⭐ Текущий рейтинг: " + currentCourier.getRating());
        } else {
            System.out.println("❌ Ошибка завершения заказа. Проверьте ID заказа.");
        }
    }

    private static void addMoney() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        💰 ПОПОЛНЕНИЕ БАЛАНСА        ");
        System.out.println("=".repeat(40));

        System.out.print("Введите сумму для пополнения: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline

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
            case "new": return "Новый";
            case "assigned": return "Назначен";
            case "delivered": return "Доставлен";
            case "cancelled": return "Отменен";
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