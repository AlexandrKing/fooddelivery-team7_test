package com.team7.userstory.client;

import com.team7.service.client.*;
import com.team7.model.client.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ClientUserStories {
    private static User currentUser = null;
    private static DatabaseService dbService = new DatabaseService();

    public static void main(String[] args) {
        AuthService authService = new AuthServiceImpl(dbService);
        RestaurantService restaurantService = new RestaurantServiceImpl(dbService);
        MenuService menuService = new MenuServicelmpl(dbService);
        CartService cartService = new CartServiceImpl();
        OrderService orderService = new OrderServiceImpl(dbService, cartService);
        HistoryService historyService = new HistoryServiceImpl(orderService);
        OrderTrackingService orderTrackingService = new OrderTrackingServiceImpl(orderService);
        ReviewService reviewService = new ReviewServiceImpl(dbService);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== 🍽️ СЕРВИС ДОСТАВКИ ЕДЫ ===");

            if (currentUser != null) {
                System.out.println("👋 Добро пожаловать, " + currentUser.getName() + "!");
                System.out.println("1. 📍 Поиск ресторанов");
                System.out.println("2. 📋 Просмотр меню");
                System.out.println("3. 🛒 Корзина");
                System.out.println("4. 📦 Мои заказы");
                System.out.println("5. 👤 Профиль");
                System.out.println("6. 🚪 Выйти из системы");
                System.out.println("7. ❌ Выйти из программы");
            } else {
                System.out.println("1. 📝 Регистрация");
                System.out.println("2. 🔑 Вход в систему");
                System.out.println("3. ❌ Выйти из программы");
            }

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                if (currentUser != null) {
                    handleUserMenu(choice, authService, restaurantService, menuService, cartService, orderService, scanner);
                } else {
                    handleAuthMenu(choice, authService, scanner);
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    private static void handleUserMenu(int choice, AuthService authService,
                                       RestaurantService restaurantService,
                                       MenuService menuService,
                                       CartService cartService,
                                       OrderService orderService,
                                       Scanner scanner) {
        switch (choice) {
            case 1:
                searchRestaurants(restaurantService, scanner);
                break;
            case 2:
                viewMenu(menuService, cartService, scanner);
                break;
            case 3:
                manageCart(cartService, orderService, scanner);
                break;
            case 4:
                viewOrders(orderService, scanner);
                break;
            case 5:
                manageProfile(authService, scanner);
                break;
            case 6:
                logout(authService);
                break;
            case 7:
                System.out.println("👋 Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("❌ Неверный выбор!");
        }
    }

    private static void handleAuthMenu(int choice, AuthService authService,
                                       Scanner scanner) {
        switch (choice) {
            case 1:
                registerUser(authService, scanner);
                break;
            case 2:
                loginUser(authService, scanner);
                break;
            case 3:
                System.out.println("👋 Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("❌ Неверный выбор!");
        }
    }

    private static void registerUser(AuthService authService, Scanner scanner) {
        System.out.println("\n=== 📝 РЕГИСТРАЦИЯ ПОЛЬЗОВАТЕЛЯ ===");

        System.out.print("Имя: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Телефон (+79XXXXXXXXX): ");
        String phone = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        System.out.print("Подтверждение пароля: ");
        String confirmPassword = scanner.nextLine();

        try {
            User user = authService.register(UserRole.CLIENT, name, email, phone, password, confirmPassword);
            if (user != null) {
                currentUser = user;
                System.out.println("✅ РЕГИСТРАЦИЯ УСПЕШНА!");
                System.out.println("👋 Добро пожаловать, " + user.getName() + "!");
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка регистрации: " + e.getMessage());
        }
    }

    private static void loginUser(AuthService authService, Scanner scanner) {
        System.out.println("\n=== 🔑 ВХОД В СИСТЕМУ ===");

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            User user = authService.login(email, password);
            if (user != null) {
                currentUser = user;
                System.out.println("✅ ВХОД ВЫПОЛНЕН УСПЕШНО!");
                System.out.println("👋 Добро пожаловать, " + user.getName() + "!");
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка входа: " + e.getMessage());
        }
    }

    private static void searchRestaurants(RestaurantService restaurantService, Scanner scanner) {
        System.out.println("\n=== 📍 ПОИСК РЕСТОРАНОВ ===");

        try {
            List<Restaurant> restaurants = restaurantService.getRestaurants();

            if (restaurants.isEmpty()) {
                System.out.println("😔 Рестораны не найдены");
                return;
            }

            System.out.println("🍽️  Доступные рестораны:");
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant r = restaurants.get(i);
                System.out.println((i + 1) + ". " + r.getName() +
                    " ⭐ " + r.getRating() +
                    " 🚚 " + r.getDeliveryTime() + " мин" +
                    " 💰 Мин. заказ: " + r.getMinOrderAmount() + " руб");
            }

            System.out.print("\nВыберите ресторан для просмотра меню (0 - назад): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= restaurants.size()) {
                Restaurant selectedRestaurant = restaurants.get(choice - 1);
                System.out.println("✅ Выбран ресторан: " + selectedRestaurant.getName());
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при загрузке ресторанов: " + e.getMessage());
        }
    }

    private static void viewMenu(MenuService menuService, CartService cartService, Scanner scanner) {
        System.out.println("\n=== 📋 ПРОСМОТР МЕНЮ ===");

        System.out.print("Введите ID ресторана: ");
        Long restaurantId = scanner.nextLong();
        scanner.nextLine();

        try {
            List<Menu> menu = menuService.getMenu(restaurantId);

            if (menu.isEmpty()) {
                System.out.println("😔 Меню пустое");
                return;
            }

            System.out.println("🍽️  Меню ресторана:");
            for (int i = 0; i < menu.size(); i++) {
                Menu item = menu.get(i);
                System.out.println((i + 1) + ". " + item.getName() +
                    " - 💰 " + item.getPrice() + " руб" +
                    " | " + item.getDescription());
            }

            System.out.print("\nДобавить в корзину (номер блюда, 0 - назад): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= menu.size()) {
                Menu selectedItem = menu.get(choice - 1);
                System.out.print("Количество: ");
                int quantity = scanner.nextInt();
                scanner.nextLine();

                try {
                    Cart cart = cartService.addItem(currentUser.getId(), restaurantId, selectedItem.getId(), quantity);
                    if (cart != null) {
                        System.out.println("✅ Добавлено в корзину: " + selectedItem.getName());
                    }
                } catch (Exception e) {
                    System.out.println("❌ Ошибка при добавлении в корзину: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void manageCart(CartService cartService, OrderService orderService, Scanner scanner) {
        System.out.println("\n=== 🛒 КОРЗИНА ===");

        try {
            Cart cart = cartService.getCart(currentUser.getId());

            if (cart.getItems().isEmpty()) {
                System.out.println("🛒 Корзина пуста");
                return;
            }

            System.out.println("📦 Товары в корзине:");
            for (int i = 0; i < cart.getItems().size(); i++) {
                CartItem item = cart.getItems().get(i);
                System.out.println((i + 1) + ". Товар ID: " + item.getMenuItemId() +
                    " | Количество: " + item.getQuantity());
            }
            System.out.println("💰 Общая сумма: " + cart.getTotalAmount() + " руб");

            System.out.println("\n1. ✏️  Изменить количество");
            System.out.println("2. ❌ Удалить товар");
            System.out.println("3. 🗑️  Очистить корзину");
            System.out.println("4. 📦 Оформить заказ");
            System.out.println("0. ↩️  Назад");

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Номер товара: ");
                    int itemNum = scanner.nextInt();
                    System.out.print("Новое количество: ");
                    int newQuantity = scanner.nextInt();
                    scanner.nextLine();

                    if (itemNum > 0 && itemNum <= cart.getItems().size()) {
                        CartItem item = cart.getItems().get(itemNum - 1);
                        try {
                            cartService.updateItemQuantity(currentUser.getId(), item.getId(), newQuantity);
                            System.out.println("✅ Количество обновлено");
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка: " + e.getMessage());
                        }
                    }
                    break;
                case 2:
                    System.out.print("Номер товара: ");
                    itemNum = scanner.nextInt();
                    scanner.nextLine();

                    if (itemNum > 0 && itemNum <= cart.getItems().size()) {
                        CartItem item = cart.getItems().get(itemNum - 1);
                        try {
                            cartService.removeItem(currentUser.getId(), item.getId());
                            System.out.println("✅ Товар удален");
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка: " + e.getMessage());
                        }
                    }
                    break;
                case 3:
                    try {
                        cartService.clearCart(currentUser.getId());
                        System.out.println("✅ Корзина очищена");
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка: " + e.getMessage());
                    }
                    break;
                case 4:
                    createOrder(cartService, orderService, scanner);
                    break;
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void createOrder(CartService cartService, OrderService orderService, Scanner scanner) {
        System.out.println("\n=== 📦 ОФОРМЛЕНИЕ ЗАКАЗА ===");

        try {
            Cart cart = cartService.getCart(currentUser.getId());

            if (cart.getItems().isEmpty()) {
                System.out.println("❌ Корзина пуста");
                return;
            }

            System.out.print("📍 Адрес доставки: ");
            String address = scanner.nextLine();

            System.out.println("🚚 Способ доставки:");
            System.out.println("1. Курьерская доставка");
            System.out.println("2. Самовывоз");
            System.out.print("Выберите способ: ");
            int deliveryChoice = scanner.nextInt();
            scanner.nextLine();

            DeliveryType deliveryType = (deliveryChoice == 2) ? DeliveryType.PICKUP : DeliveryType.DELIVERY;

            System.out.println("💳 Способ оплаты:");
            System.out.println("1. Картой онлайн");
            System.out.println("2. Наличными при получении");
            System.out.print("Выберите способ: ");
            int paymentChoice = scanner.nextInt();
            scanner.nextLine();

            PaymentMethod paymentMethod = (paymentChoice == 1) ? PaymentMethod.CARD : PaymentMethod.CASH;

            Order order = orderService.createOrder(
                currentUser.getId(),
                cart.getRestaurantId(),
                address,
                deliveryType,
                LocalDateTime.now().plusHours(1),
                paymentMethod
            );

            System.out.println("\n" + "✅".repeat(20));
            System.out.println("        🎉 ЗАКАЗ СОЗДАН УСПЕШНО!        ");
            System.out.println("✅".repeat(20));
            System.out.println("📦 Номер заказа: " + order.getId());
            System.out.println("📊 Статус: " + order.getStatus());
            System.out.println("💰 Сумма: " + order.getTotalAmount() + " руб");

        } catch (Exception e) {
            System.out.println("❌ Ошибка создания заказа: " + e.getMessage());
        }
    }

    private static void viewOrders(OrderService orderService, Scanner scanner) {
        System.out.println("\n=== 📦 МОИ ЗАКАЗЫ ===");

        try {
            List<Order> orders = orderService.getUserOrders(currentUser.getId());

            if (orders.isEmpty()) {
                System.out.println("😔 Заказы не найдены");
                return;
            }

            System.out.println("📊 История заказов:");
            for (Order order : orders) {
                System.out.println("📦 Заказ #" + order.getId() +
                    " | 📊 Статус: " + order.getStatus() +
                    " | 💰 Сумма: " + order.getTotalAmount() + " руб" +
                    " | 📅 Дата: " + order.getCreatedAt());
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void manageProfile(AuthService authService, Scanner scanner) {
        System.out.println("\n=== 👤 ПРОФИЛЬ ===");

        System.out.println("1. 📋 Просмотр профиля");
        System.out.println("2. ✏️  Изменить данные");
        System.out.println("3. 🔐 Сменить пароль");
        System.out.println("4. 📍 Управление адресами");
        System.out.println("0. ↩️  Назад");

        System.out.print("Выберите действие: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                showUserProfile();
                break;
            case 2:
                updateProfile(authService, scanner);
                break;
            case 3:
                changePassword(authService, scanner);
                break;
            case 4:
                manageAddresses(authService, scanner);
                break;
        }
    }

    private static void showUserProfile() {
        System.out.println("\n=== 📋 ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ ===");
        System.out.println("👤 Имя: " + currentUser.getName());
        System.out.println("📧 Email: " + currentUser.getEmail());
        System.out.println("📱 Телефон: " + currentUser.getPhone());
        System.out.println("📍 Адреса: " + currentUser.getAddresses().size());
    }

    private static void updateProfile(AuthService authService, Scanner scanner) {
        System.out.println("\n=== ✏️  ИЗМЕНЕНИЕ ПРОФИЛЯ ===");

        System.out.print("Новое имя: ");
        String newName = scanner.nextLine();

        System.out.print("Новый телефон: ");
        String newPhone = scanner.nextLine();

        System.out.print("Новый email: ");
        String newEmail = scanner.nextLine();

        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setName(newName);
        updatedUser.setPhone(newPhone);
        updatedUser.setEmail(newEmail);

        try {
            User result = authService.updateProfile(updatedUser);
            currentUser = result;
            System.out.println("✅ Профиль успешно обновлен!");
        } catch (Exception e) {
            System.out.println("❌ Ошибка обновления: " + e.getMessage());
        }
    }

    private static void changePassword(AuthService authService, Scanner scanner) {
        System.out.println("\n=== 🔐 СМЕНА ПАРОЛЯ ===");

        System.out.print("Текущий пароль: ");
        String oldPassword = scanner.nextLine();

        System.out.print("Новый пароль: ");
        String newPassword = scanner.nextLine();

        try {
            User updatedUser = authService.changePassword(currentUser.getId(), oldPassword, newPassword);
            currentUser = updatedUser;
            System.out.println("✅ Пароль успешно изменен!");
        } catch (Exception e) {
            System.out.println("❌ Ошибка смены пароля: " + e.getMessage());
        }
    }

    private static void manageAddresses(AuthService authService, Scanner scanner) {
        System.out.println("\n=== 📍 УПРАВЛЕНИЕ АДРЕСАМИ ===");

        System.out.println("📍 Мои адреса:");
        for (int i = 0; i < currentUser.getAddresses().size(); i++) {
            Address addr = currentUser.getAddresses().get(i);
            System.out.println((i + 1) + ". " + addr.getLabel() + ": " + addr.getAddress());
        }

        System.out.println("\n1. ➕ Добавить адрес");
        System.out.println("2. ❌ Удалить адрес");
        System.out.println("0. ↩️  Назад");

        System.out.print("Выберите действие: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            Address newAddress = new Address();
            System.out.print("Название (дом, работа): ");
            newAddress.setLabel(scanner.nextLine());
            System.out.print("Адрес: ");
            newAddress.setAddress(scanner.nextLine());
            System.out.print("Квартира: ");
            newAddress.setApartment(scanner.nextLine());

            try {
                User user = authService.addAddress(currentUser.getId(), newAddress);
                currentUser = user;
                System.out.println("✅ Адрес успешно добавлен!");
            } catch (Exception e) {
                System.out.println("❌ Ошибка добавления адреса: " + e.getMessage());
            }
        }
    }

    private static void logout(AuthService authService) {
        authService.logout();
        currentUser = null;
        System.out.println("👋 Выход из системы...");
    }
}