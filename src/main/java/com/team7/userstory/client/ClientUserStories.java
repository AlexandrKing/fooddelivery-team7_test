package com.team7.userstory.client;

import com.team7.service.client.*;
import com.team7.model.client.*;
import com.team7.service.config.DatabaseConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ClientUserStories {
    private static User currentUser = null;
    private static Restaurant currentRestaurant = null;

    public static void main(String[] args) {
        // Создаем сервисы без DatabaseConfig
        AuthService authService = new AuthServiceImpl();
        RestaurantService restaurantService = new RestaurantServiceImpl();
        MenuService menuService = new MenuServiceImpl();
        CartService cartService = new CartServiceImpl();
        OrderService orderService = new OrderServiceImpl(cartService);
        ReviewService reviewService = new ReviewServiceImpl();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== 🍽️ СЕРВИС ДОСТАВКИ ЕДЫ ===");

            if (currentUser != null) {
                System.out.println("👋 Добро пожаловать, " + currentUser.getName() + "!");
                System.out.println("1. 📍 Поиск ресторанов");
                System.out.println("2. 📋 Просмотр меню текущего ресторана");
                System.out.println("3. 🛒 Корзина");
                System.out.println("4. 📦 Мои заказы");
                System.out.println("5. 📝 Управление отзывами"); // ⬅️ НОВЫЙ ПУНКТ
                System.out.println("6. 👤 Профиль");
                System.out.println("7. 🚪 Выйти из системы");
                System.out.println("8. ❌ Выйти из программы");

                if (currentRestaurant != null) {
                    System.out.println("📍 Текущий ресторан: " + currentRestaurant.getName());
                }
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
                    handleUserMenu(choice, authService, restaurantService, menuService, cartService, orderService, reviewService, scanner);
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
                                       ReviewService reviewService, // ⬅️ ДОБАВЬТЕ ЭТОТ ПАРАМЕТР
                                       Scanner scanner) {
        switch (choice) {
            case 1:
                searchRestaurants(restaurantService, menuService, cartService, scanner);
                break;
            case 2:
                if (currentRestaurant != null) {
                    viewRestaurantMenu(menuService, cartService, scanner);
                } else {
                    System.out.println("⚠️  Сначала выберите ресторан через пункт 'Поиск ресторанов'");
                }
                break;
            case 3:
                manageCart(cartService, orderService, scanner);
                break;
            case 4:
                viewOrders(orderService, scanner);
                break;
            case 5:
                manageReviews(reviewService, orderService, scanner); // ⬅️ НОВЫЙ ПУНКТ
                break;
            case 6:
                manageProfile(authService, scanner);
                break;
            case 7:
                logout(authService);
                break;
            case 8:
                System.out.println("👋 Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("❌ Неверный выбор!");
        }
    }

    private static void manageReviews(ReviewService reviewService, OrderService orderService, Scanner scanner) {
        System.out.println("\n=== 📝 УПРАВЛЕНИЕ ОТЗЫВАМИ ===");

        // Получаем заказы пользователя
        List<Order> orders = orderService.getUserOrders(currentUser.getId());

        if (orders.isEmpty()) {
            System.out.println("😔 У вас еще нет заказов");
            return;
        }

        // Показываем меню для отзывов
        System.out.println("1. ✍️  Оставить отзыв на заказ");
        System.out.println("2. 📋 Посмотреть мои отзывы");
        System.out.println("3. ↩️  Назад");

        System.out.print("Выберите действие: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                createReview(reviewService, orderService, orders, scanner);
                break;
            case 2:
                viewMyReviews(reviewService);
                break;
            case 3:
                return;
            default:
                System.out.println("❌ Неверный выбор!");
        }
    }

    private static void createReview(ReviewService reviewService, OrderService orderService,
                                     List<Order> orders, Scanner scanner) {
        System.out.println("\n=== ✍️  СОЗДАНИЕ ОТЗЫВА ===");

        // Показываем заказы, доступные для отзыва
        System.out.println("📦 Ваши заказы:");
        int index = 1;
        for (Order order : orders) {
            System.out.println(index++ + ". Заказ #" + order.getId() +
                " | Сумма: " + order.getTotalAmount() + " руб" +
                " | Статус: " + order.getStatus() +
                " | Дата: " + order.getCreatedAt());
        }

        System.out.print("\nВыберите номер заказа для отзыва: ");
        int orderChoice = scanner.nextInt();
        scanner.nextLine();

        if (orderChoice < 1 || orderChoice > orders.size()) {
            System.out.println("❌ Неверный выбор заказа!");
            return;
        }

        Order selectedOrder = orders.get(orderChoice - 1);

        // Проверяем, можно ли оставить отзыв на этот заказ
        if (selectedOrder.getStatus() != OrderStatus.DELIVERED) {
            System.out.println("⚠️  Отзыв можно оставить только на доставленные заказы");
            System.out.println("📦 Статус этого заказа: " + selectedOrder.getStatus());
            return;
        }

        System.out.println("\n📋 Информация о заказе:");
        System.out.println("   #" + selectedOrder.getId() + " | " + selectedOrder.getCreatedAt());
        System.out.println("   Сумма: " + selectedOrder.getTotalAmount() + " руб");
        System.out.println("   Адрес доставки: " + selectedOrder.getDeliveryAddress());

        // Оценка ресторана
        System.out.print("\n⭐ Оцените ресторан (1-5, 0 - пропустить): ");
        int restaurantRating = scanner.nextInt();
        scanner.nextLine();

        // Оценка курьера
        System.out.print("⭐ Оцените курьера (1-5, 0 - пропустить): ");
        int courierRating = scanner.nextInt();
        scanner.nextLine();

        // Комментарий
        System.out.print("📝 Комментарий (необязательно): ");
        String comment = scanner.nextLine();

        // Валидация оценок
        if (restaurantRating != 0 && (restaurantRating < 1 || restaurantRating > 5)) {
            System.out.println("❌ Оценка ресторана должна быть от 1 до 5");
            return;
        }

        if (courierRating != 0 && (courierRating < 1 || courierRating > 5)) {
            System.out.println("❌ Оценка курьера должна быть от 1 до 5");
            return;
        }

        // Создаем отзыв
        try {
            Integer restaurantRatingObj = restaurantRating == 0 ? null : restaurantRating;
            Integer courierRatingObj = courierRating == 0 ? null : courierRating;

            Review review = reviewService.createReview(
                selectedOrder.getId(),
                restaurantRatingObj,
                courierRatingObj,
                comment
            );

            System.out.println("\n" + "✅".repeat(30));
            System.out.println("        🎉 ОТЗЫВ УСПЕШНО СОЗДАН!        ");
            System.out.println("✅".repeat(30));
            System.out.println("📦 Номер заказа: " + selectedOrder.getId());
            if (review.getRestaurantRating() != null) {
                System.out.println("⭐ Оценка ресторана: " + review.getRestaurantRating() + "/5");
            }
            if (review.getCourierRating() != null) {
                System.out.println("⭐ Оценка курьера: " + review.getCourierRating() + "/5");
            }
            if (review.getComment() != null && !review.getComment().isEmpty()) {
                System.out.println("📝 Ваш комментарий: " + review.getComment());
            }
            System.out.println("📅 Дата отзыва: " + review.getCreatedAt());

        } catch (Exception e) {
            System.out.println("❌ Ошибка создания отзыва: " + e.getMessage());
        }
    }

    private static void viewMyReviews(ReviewService reviewService) {
        System.out.println("\n=== 📋 МОИ ОТЗЫВЫ ===");

        try {
            List<Review> reviews = reviewService.getReviews(currentUser.getId());

            if (reviews.isEmpty()) {
                System.out.println("😔 У вас еще нет отзывов");
                return;
            }

            System.out.println("📊 Всего отзывов: " + reviews.size());
            System.out.println("-".repeat(50));

            for (Review review : reviews) {
                System.out.println("📦 Заказ #" + review.getOrderId());

                if (review.getRestaurantRating() != null) {
                    System.out.println("   🍽️  Ресторан: " + getStars(review.getRestaurantRating()));
                }

                if (review.getCourierRating() != null) {
                    System.out.println("   🚚 Курьер: " + getStars(review.getCourierRating()));
                }

                if (review.getComment() != null && !review.getComment().isEmpty()) {
                    System.out.println("   📝 Комментарий: " + review.getComment());
                }

                System.out.println("   📅 Дата: " + review.getCreatedAt());
                System.out.println("-".repeat(30));
            }

            // Средние оценки
            Double avgRestaurantRating = reviewService.getRestaurantRating(currentUser.getId());
            Double avgCourierRating = reviewService.getCourierRating(currentUser.getId());

            System.out.println("\n📈 Средние оценки:");
            if (avgRestaurantRating > 0) {
                System.out.println("   🍽️  Средняя оценка ресторанов: " + String.format("%.1f", avgRestaurantRating) + "/5");
            }
            if (avgCourierRating > 0) {
                System.out.println("   🚚 Средняя оценка курьеров: " + String.format("%.1f", avgCourierRating) + "/5");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка получения отзывов: " + e.getMessage());
        }
    }

    // Вспомогательный метод для звездочек
    private static String getStars(int rating) {
        return "⭐".repeat(rating) + " (" + rating + "/5)";
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
            User user = authService.register(name, email, phone, password, confirmPassword);
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

    private static void searchRestaurants(RestaurantService restaurantService,
                                          MenuService menuService,
                                          CartService cartService,
                                          Scanner scanner) {
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
                        " 💰 Мин. заказ: " + r.getMinOrderAmount() + " руб" +
                        " | Кухня: " + r.getCuisineType());
            }

            System.out.print("\nВыберите ресторан для просмотра меню (0 - назад): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= restaurants.size()) {
                Restaurant selectedRestaurant = restaurants.get(choice - 1);
                currentRestaurant = selectedRestaurant;
                System.out.println("\n        ВЫБРАН РЕСТОРАН: " + selectedRestaurant.getName().toUpperCase());

                // Сразу показываем меню выбранного ресторана
                viewRestaurantMenu(menuService, cartService, scanner);
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при загрузке ресторанов: " + e.getMessage());
        }
    }

    private static void viewRestaurantMenu(MenuService menuService, CartService cartService, Scanner scanner) {
        if (currentRestaurant == null) {
            System.out.println("⚠️  Ресторан не выбран!");
            return;
        }

        System.out.println("\n=== 📋 МЕНЮ РЕСТОРАНА: " + currentRestaurant.getName() + " ===");
        System.out.println("⭐ Рейтинг: " + currentRestaurant.getRating());
        System.out.println("🚚 Время доставки: " + currentRestaurant.getDeliveryTime() + " мин");
        System.out.println("💰 Минимальный заказ: " + currentRestaurant.getMinOrderAmount() + " руб");
        System.out.println("📍 Адрес: " + currentRestaurant.getAddress());
        System.out.println("-".repeat(50));

        try {
            List<Menu> menu = menuService.getMenu(currentRestaurant.getId());

            if (menu.isEmpty()) {
                System.out.println("😔 Меню пустое");
                return;
            }

            System.out.println("🍽️  Блюда:");
            for (int i = 0; i < menu.size(); i++) {
                Menu item = menu.get(i);
                System.out.println((i + 1) + ". " + item.getName() +
                        " - 💰 " + item.getPrice() + " руб");
                if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                    System.out.println("   📝 " + item.getDescription());
                }
                if (item.getCalories() != null) {
                    System.out.println("   🔥 " + item.getCalories() + " ккал");
                }
                if (item.getCookingTime() != null) {
                    System.out.println("   ⏱️  Готовится: " + item.getCookingTime() + " мин");
                }
                System.out.println();
            }

            boolean inMenu = true;
            while (inMenu) {
                System.out.println("\n=== УПРАВЛЕНИЕ МЕНЮ ===");
                System.out.println("1. ➕ Добавить блюдо в корзину");
                System.out.println("2. 🛒 Перейти в корзину");
                System.out.println("3. 🔙 Вернуться к списку ресторанов");
                System.out.println("4. 🏠 Вернуться в главное меню");

                System.out.print("Выберите действие: ");
                int menuChoice = scanner.nextInt();
                scanner.nextLine();

                switch (menuChoice) {
                    case 1:
                        addToCartFromMenu(menu, cartService, scanner);
                        break;
                    case 2:
                        inMenu = false;
                        // Корзину обработаем в главном меню
                        break;
                    case 3:
                        currentRestaurant = null;
                        inMenu = false;
                        break;
                    case 4:
                        currentRestaurant = null;
                        inMenu = false;
                        return;
                    default:
                        System.out.println("❌ Неверный выбор!");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void addToCartFromMenu(List<Menu> menu, CartService cartService, Scanner scanner) {
        System.out.print("Введите номер блюда: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= menu.size()) {
            Menu selectedItem = menu.get(choice - 1);
            System.out.print("Количество: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();

            if (quantity <= 0) {
                System.out.println("❌ Количество должно быть больше 0");
                return;
            }

            try {
                Cart cart = cartService.addItem(currentUser.getId(), currentRestaurant.getId(), selectedItem.getId(), quantity);

                System.out.println("\n" + "=".repeat(50));
                System.out.println("        ДОБАВЛЕНО В КОРЗИНУ!");
                System.out.println("=".repeat(50));
                System.out.println("🍽️  Блюдо: " + selectedItem.getName());
                System.out.println("💰 Цена за шт: " + selectedItem.getPrice() + " руб");
                System.out.println("📦 Количество: " + quantity);
                System.out.println("💰 Итого: " + (selectedItem.getPrice() * quantity) + " руб");
                System.out.println("🛒 Товаров в корзине: " + cart.getItems().size());
                System.out.println("💰 Общая сумма корзины: " + cart.getTotalAmount() + " руб");

            } catch (Exception e) {
                System.out.println("❌ Ошибка при добавлении в корзину: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Неверный номер блюда!");
        }
    }

    private static void manageCart(CartService cartService, OrderService orderService, Scanner scanner) {
        System.out.println("\n=== 🛒 КОРЗИНА ===");

        try {
            Cart cart = cartService.getCart(currentUser.getId());

            if (cart.getItems().isEmpty()) {
                System.out.println("🛒 Корзина пуста");

                if (currentRestaurant != null) {
                    System.out.print("\nХотите вернуться к меню ресторана '" + currentRestaurant.getName() + "'? (да/нет): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.equals("да") || answer.equals("д") || answer.equals("yes") || answer.equals("y")) {
                        // Ничего не делаем - вернемся в главное меню, а затем можно выбрать пункт 2
                    }
                }
                return;
            }

            // Показываем информацию о ресторане
            if (currentRestaurant != null) {
                System.out.println("📍 Ресторан: " + currentRestaurant.getName());
            }

            System.out.println("\n📦 Товары в корзине:");
            for (int i = 0; i < cart.getItems().size(); i++) {
                CartItem item = cart.getItems().get(i);
                System.out.println((i + 1) + ". " + item.getName() +
                        " | 💰 " + item.getPrice() + " руб" +
                        " | Количество: " + item.getQuantity() +
                        " | Сумма: " + (item.getPrice() * item.getQuantity()) + " руб");
            }
            System.out.println("💰 Общая сумма: " + cart.getTotalAmount() + " руб");

            boolean inCart = true;
            while (inCart) {
                System.out.println("\n=== УПРАВЛЕНИЕ КОРЗИНОЙ ===");
                System.out.println("1. ✏️  Изменить количество");
                System.out.println("2. ❌ Удалить товар");
                System.out.println("3. 🗑️  Очистить корзину");
                System.out.println("4. 📦 Оформить заказ");
                System.out.println("5. 🔙 Вернуться в меню ресторана");
                System.out.println("6. 🏠 Вернуться в главное меню");

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
                                // Обновляем корзину
                                cart = cartService.getCart(currentUser.getId());
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
                                // Обновляем корзину
                                cart = cartService.getCart(currentUser.getId());
                                if (cart.getItems().isEmpty()) {
                                    System.out.println("🛒 Корзина теперь пуста");
                                    inCart = false;
                                }
                            } catch (Exception e) {
                                System.out.println("❌ Ошибка: " + e.getMessage());
                            }
                        }
                        break;
                    case 3:
                        try {
                            cartService.clearCart(currentUser.getId());
                            System.out.println("✅ Корзина очищена");
                            inCart = false;
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка: " + e.getMessage());
                        }
                        break;
                    case 4:
                        createOrder(cartService, orderService, scanner);
                        inCart = false;
                        break;
                    case 5:
                        if (currentRestaurant != null) {
                            inCart = false;
                            // Вернемся в главное меню, а затем можно выбрать пункт 2
                        } else {
                            System.out.println("⚠️  Ресторан не выбран!");
                        }
                        break;
                    case 6:
                        inCart = false;
                        break;
                    default:
                        System.out.println("❌ Неверный выбор!");
                }

                // Если корзина не пуста, показываем обновленный список
                if (inCart && !cart.getItems().isEmpty()) {
                    System.out.println("\n📦 Товары в корзине:");
                    for (int i = 0; i < cart.getItems().size(); i++) {
                        CartItem item = cart.getItems().get(i);
                        System.out.println((i + 1) + ". " + item.getName() +
                                " | 💰 " + item.getPrice() + " руб" +
                                " | Количество: " + item.getQuantity());
                    }
                    System.out.println("💰 Общая сумма: " + cart.getTotalAmount() + " руб");
                }
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

            // Показываем сводку заказа
            System.out.println("📋 Сводка заказа:");

            // ВАЖНОЕ ИСПРАВЛЕНИЕ: Проверяем currentRestaurant
            if (currentRestaurant != null) {
                System.out.println("📍 Ресторан: " + currentRestaurant.getName());
            } else {
                // Если currentRestaurant null, получаем restaurantId из корзины
                System.out.println("📍 Ресторан ID: " + cart.getRestaurantId());
            }

            System.out.println("📦 Товары:");
            for (CartItem item : cart.getItems()) {
                System.out.println("   • " + item.getName() + " x" + item.getQuantity() +
                        " = " + (item.getPrice() * item.getQuantity()) + " руб");
            }
            System.out.println("💰 Общая сумма: " + cart.getTotalAmount() + " руб");

            System.out.print("\n📍 Адрес доставки: ");
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

            // ВАЖНОЕ ИСПРАВЛЕНИЕ: Используем restaurantId из корзины
            Long restaurantId = (currentRestaurant != null) ? currentRestaurant.getId() : cart.getRestaurantId();

            if (restaurantId == null) {
                System.out.println("❌ Не удалось определить ресторан для заказа");
                return;
            }

            Order order = orderService.createOrder(
                    currentUser.getId(),
                    restaurantId,  // Используем restaurantId из корзины или currentRestaurant
                    address,
                    deliveryType,
                    LocalDateTime.now().plusHours(1),
                    paymentMethod
            );

            System.out.println("\n" + "=".repeat(50));
            System.out.println("        🎉 ЗАКАЗ СОЗДАН УСПЕШНО!        ");
            System.out.println("=".repeat(50));
            System.out.println("📦 Номер заказа: " + order.getId());
            System.out.println("📊 Статус: " + order.getStatus());
            System.out.println("💰 Сумма: " + order.getTotalAmount() + " руб");
            System.out.println("📍 Адрес: " + order.getDeliveryAddress());
            System.out.println("🚚 Способ доставки: " + (deliveryType == DeliveryType.DELIVERY ? "Курьерская доставка" : "Самовывоз"));
            System.out.println("💳 Способ оплаты: " + (paymentMethod == PaymentMethod.CARD ? "Картой онлайн" : "Наличными при получении"));

            // Очищаем корзину после заказа
            cartService.clearCart(currentUser.getId());
            System.out.println("🛒 Корзина очищена");

            // Сбрасываем текущий ресторан
            currentRestaurant = null;

        } catch (Exception e) {
            System.out.println("❌ Ошибка создания заказа: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("\n📦 Заказ #" + order.getId());
                System.out.println("   📊 Статус: " + order.getStatus());
                System.out.println("   💰 Сумма: " + order.getTotalAmount() + " руб");
                System.out.println("   📅 Дата: " + order.getCreatedAt());
                System.out.println("   📍 Адрес: " + order.getDeliveryAddress());

                // Показываем возможность оставить отзыв для доставленных заказов
                if (order.getStatus() == OrderStatus.DELIVERED) {
                    System.out.println("   ✅ Этот заказ можно оценить!");
                }
            }

            // Предлагаем оставить отзыв
            System.out.print("\n📝 Хотите оставить отзыв на доставленный заказ? (да/нет): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("да") || answer.equals("д") || answer.equals("yes") || answer.equals("y")) {
                // Здесь можно вызвать метод для создания отзыва
                // Для простоты можно сразу перейти к созданию отзыва
                System.out.println("🎯 Перейдите в раздел 'Отзывы' в главном меню!");
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
            System.out.println((i + 1) + ". " + addr.getLabel() + ": " + addr.getAddress() + ", кв. " + addr.getApartment());
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
        currentRestaurant = null;
        System.out.println("👋 Выход из системы...");
    }
}