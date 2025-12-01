package com.team7.userstory.client;

import com.team7.model.client.*;
import com.team7.service.client.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ClientsUserStories {

    public static void main(String[] args) {
        //Auth
        checkUserStory1();
        checkUserStory2();
        checkUserStory3();
        checkUserStory4();
        checkUserStory5();
        checkUserStory6();
        //Cart
        checkUserStory7();
        checkUserStory8();
        checkUserStory9();
        //Menu
        checkUserStory10();
        //Order
        checkUserStory11();
        checkUserStory12();
        checkUserStory13();
        checkUserStory14();
        //Restaurant
        checkUserStory15();
        checkUserStory16();
        //History
        checkUserStory17();
        checkUserStory18();
        //Review
        checkUserStory19();
        checkUserStory20();
    }

    //Auth
    public static void checkUserStory1() {
        System.out.println("User Story 1: Регистрация нового пользователя");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.register(
                UserRole.CLIENT,
                "Иван",
                "ivan@mail.ru",
                "+79161234567",
                "password123",
                "password123"
            );
            System.out.println("Пользователь зарегистрирован: " + user.getEmail());
            System.out.println("ID пользователя: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    public static void checkUserStory2() {
        System.out.println("\nUser Story 2: Вход в систему");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");
            System.out.println("Успешный вход: " + user.getEmail());
            System.out.println("Текущий пользователь: " + authService.getCurrentUser().getEmail());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка входа: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public static void checkUserStory3() {
        System.out.println("\nUser Story 3: Изменение данных профиля");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");

            user.setName("Александр");
            user.setPhone("+79167778899");
            User updatedUser = authService.updateProfile(user);

            System.out.println("Данные обновлены: " + updatedUser.getName());
            System.out.println("Новый телефон: " + updatedUser.getPhone());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка изменения данных: " + e.getMessage());
        }
    }

    public static void checkUserStory4() {
        System.out.println("\nUser Story 4: Управление адресами доставки");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");
            Address address = new Address();
            address.setLabel("дом");
            address.setAddress("ул. Ленина, 10");
            address.setApartment("25");

            User userWithAddress = authService.addAddress(user.getId(), address);

            System.out.println("Адрес добавлен: " + address.getAddress());
            System.out.println("Всего адресов: " + userWithAddress.getAddresses().size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка добавления адреса: " + e.getMessage());
        }
    }

    public static void checkUserStory5() {
        System.out.println("\nUser Story 5: Смена пароля");

        AuthService authService = new AuthServiceImpl();

        try {
            User user = authService.login("ivan@mail.ru", "password123");

            User updatedUser = authService.changePassword(
                user.getId(),
                "password123",
                "newPassword456"
            );

            System.out.println("Пароль успешно изменен");

            authService.logout();
            authService.login("ivan@mail.ru", "newPassword456");
            System.out.println("Вход с новым паролем выполнен успешно");

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка смены пароля: " + e.getMessage());
        }
    }

    public static void checkUserStory6() {
        System.out.println("\nUser Story 6: Валидация данных");

        AuthService authService = new AuthServiceImpl();

        System.out.println("Проверка доступности email 'test@mail.ru': " + authService.isEmailAvailable("test@mail.ru"));
        System.out.println("Проверка доступности телефона '+79161111111': " + authService.isPhoneAvailable("+79161111111"));

        try {
            authService.register(UserRole.CLIENT, "pass", "invalid-email", "+79161111111", "pass", "pass");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации email: " + e.getMessage());
        }

        try {
            authService.register(UserRole.CLIENT, "pass", "test@mail.ru", "89161111111", "pass", "pass");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации телефона: " + e.getMessage());
        }
    }

    //Cart
    public static void checkUserStory7() {
        System.out.println("\nUser Story 7: Добавление товара в корзину");

        CartService cartService = new CartServiceImpl();

        try {
            Cart cart = cartService.addItem(1L, 1L, 1L, 2);

            System.out.println("Товар добавлен в корзину");
            System.out.println("ID корзины: " + cart.getId());
            System.out.println("Количество товаров: " + cart.getItems().size());
            System.out.println("Общая сумма: " + cart.getTotalAmount() + " руб");

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка добавления товара: " + e.getMessage());
        }
    }

    public static void checkUserStory8() {
        System.out.println("\nUser Story 8: Управление корзиной");

        CartService cartService = new CartServiceImpl();

        try {
            cartService.addItem(2L, 1L, 1L, 1);
            cartService.addItem(2L, 2L, 3L, 2);
            cartService.addItem(2L, 3L, 5L, 1);

            Cart cart = cartService.getCart(2L);
            System.out.println("Корзина получена");
            System.out.println("Товаров в корзине: " + cart.getItems().size());

            if (!cart.getItems().isEmpty()) {
                // Исправлено: get(0) вместо getClass()
                Long itemId = cart.getItems().get(0).getId();

                cart = cartService.updateItemQuantity(2L, itemId, 3);
                // Исправлено: get(0) вместо getClass()
                System.out.println("Количество изменено: " + cart.getItems().get(0).getQuantity());

                cart = cartService.removeItem(2L, itemId);
                System.out.println("Товар удален. Осталось товаров: " + cart.getItems().size());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка управления корзиной: " + e.getMessage());
        }
    }

    public static void checkUserStory9() {
        System.out.println("\nUser Story 9: Очистка корзины");

        CartService cartService = new CartServiceImpl();

        try {
            cartService.addItem(3L, 1L, 1L, 3);
            cartService.addItem(3L, 1L, 2L, 1);

            Cart cartBefore = cartService.getCart(3L);
            System.out.println("Товаров до очистки: " + cartBefore.getItems().size());

            Cart cartAfter = cartService.clearCart(3L);
            System.out.println("Товаров после очистки: " + cartAfter.getItems().size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка очистки корзины: " + e.getMessage());
        }
    }

    //Menu
    public static void checkUserStory10() {
        System.out.println("\nUser Story 10: Просмотр меню");

        MenuService menuService = new MenuServiceImpl();

        try {
            List<Menu> emptyMenu = menuService.getMenu(1L);
            System.out.println("Меню пустого ресторана:");
            System.out.println("Количество блюд: " + emptyMenu.size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка получения меню: " + e.getMessage());
        }

        try {
            System.out.println("\nПоиск несуществующего блюда:");
            menuService.getMenuItem(1L, 1L);

        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }

        try {
            System.out.println("\nРабота с несуществующим рестораном:");
            List<Menu> nonExistentMenu = menuService.getMenu(999L);
            System.out.println("Количество блюд: " + nonExistentMenu.size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    //Order
    public static void checkUserStory11() {
        System.out.println("\nUser Story 11: Создание заказа");

        OrderService orderService = new OrderServiceImpl();

        try {
            addItemsToCart(1L);

            Order order = orderService.createOrder(
                1L,
                1L,
                "ул. Ленина, 10, кв. 25",
                DeliveryType.DELIVERY,
                LocalDateTime.now().plusHours(1),
                PaymentMethod.CARD
            );

            System.out.println("Заказ создан: " + order.getId());
            System.out.println("Статус: " + order.getStatus());
            System.out.println("Сумма: " + order.getTotalAmount() + " руб");
            System.out.println("Способ доставки: " + order.getDeliveryType());
            System.out.println("Способ оплаты: " + order.getPaymentMethod());
            System.out.println("Товаров в заказе: " + order.getItems().size());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания заказа: " + e.getMessage());
        }
    }

    public static void checkUserStory12() {
        System.out.println("\nUser Story 12: Выбор способа доставки и оплаты");

        OrderService orderService = new OrderServiceImpl();

        try {
            addItemsToCart(2L);

            Order deliveryOrder = orderService.createOrder(
                2L, 1L, "ул. Пушкина, 5",
                DeliveryType.DELIVERY, LocalDateTime.now().plusHours(1), PaymentMethod.CASH
            );
            System.out.println("Курьерская доставка + наличные: " + deliveryOrder.getDeliveryType() + ", " + deliveryOrder.getPaymentMethod());

            addItemsToCart(3L);
            Order pickupOrder = orderService.createOrder(
                3L, 1L, "Самовывоз",
                DeliveryType.PICKUP, LocalDateTime.now().plusMinutes(30), PaymentMethod.CARD
            );
            System.out.println("Самовывоз + карта: " + pickupOrder.getDeliveryType() + ", " + pickupOrder.getPaymentMethod());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания заказа: " + e.getMessage());
        }
    }

    public static void checkUserStory13() {
        System.out.println("\nUser Story 13: Отмена заказа");

        OrderService orderService = new OrderServiceImpl();

        try {
            addItemsToCart(4L);

            Order order = orderService.createOrder(
                4L, 1L, "ул. Тестовая, 1",
                DeliveryType.DELIVERY, LocalDateTime.now().plusHours(1), PaymentMethod.CARD
            );

            System.out.println("Заказ до отмены: " + order.getStatus());

            Order cancelledOrder = orderService.cancelOrder(order.getId());
            System.out.println("Заказ после отмены: " + cancelledOrder.getStatus());

            orderService.cancelOrder(order.getId());

        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void addItemsToCart(Long userId) {
        CartService cartService = new CartServiceImpl();
        cartService.addItem(userId, 1L, 1L, 2);
        cartService.addItem(userId, 1L, 2L, 1);
    }

    public static void checkUserStory14() {
        System.out.println("\nUser Story 14: Отслеживание статуса заказа");

        OrderTrackingService trackingService = new OrderTrackingServiceImpl();

        try {
            Long orderId = createTestOrder();

            Order order = trackingService.getOrderStatus(orderId);
            System.out.println("Текущий статус заказа " + orderId + ": " + order.getStatus());

            System.out.println("\nОбновление статусов заказа:");

            trackingService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
            order = trackingService.getOrderStatus(orderId);
            System.out.println("Статус обновлен: " + order.getStatus());

            trackingService.updateOrderStatus(orderId, OrderStatus.COOKING);
            order = trackingService.getOrderStatus(orderId);
            System.out.println("Статус обновлен: " + order.getStatus());

            Order finalOrder = trackingService.getOrderStatus(orderId);
            System.out.println("\nФинальный статус заказа: " + finalOrder.getStatus());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка отслеживания заказа: " + e.getMessage());
        }
    }

    private static Long createTestOrder() {
        CartService cartService = new CartServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        cartService.addItem(1L, 1L, 1L, 2);

        Order order = orderService.createOrder(
            1L, 1L, "ул. Тестовая, 1",
            DeliveryType.DELIVERY,
            java.time.LocalDateTime.now().plusHours(1),
            PaymentMethod.CARD
        );

        return order.getId();
    }

    //Restaurant
    public static void checkUserStory15() {
        System.out.println("\nUser Story 15: Поиск ресторанов");

        RestaurantService restaurantService = new RestaurantServiceImpl();

        try {
            addTestRestaurants(restaurantService);

            List<Restaurant> restaurants = restaurantService.getRestaurants();
            System.out.println("Всего ресторанов: " + restaurants.size());

            System.out.println("\nСписок ресторанов:");
            for (Restaurant restaurant : restaurants) {
                System.out.println("\n" + restaurant.getName());
                System.out.println("Рейтинг: " + restaurant.getRating());
                System.out.println("Время доставки: " + restaurant.getDeliveryTime() + " мин");
                System.out.println("Мин. заказ: " + restaurant.getMinOrderAmount() + " руб");
                System.out.println("Часы работы: " + String.join(", ", restaurant.getWorkingHours()));
                System.out.println("Активен: " + (restaurant.getIsActive() ? "Да" : "Нет"));
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка получения ресторанов: " + e.getMessage());
        }
    }

    public static void checkUserStory16() {
        System.out.println("\nUser Story 16: Фильтрация ресторанов");

        RestaurantService restaurantService = new RestaurantServiceImpl();

        try {
            addTestRestaurants(restaurantService);

            List<Restaurant> highRated = restaurantService.filterRestaurants(4.5, null);
            System.out.println("Рестораны с рейтингом 4.5+: " + highRated.size());
            highRated.forEach(r -> System.out.println("   - " + r.getName() + " (" + r.getRating() + ")"));

            List<Restaurant> fastDelivery = restaurantService.filterRestaurants(null, 30);
            System.out.println("\nДоставка до 30 мин: " + fastDelivery.size());
            fastDelivery.forEach(r -> System.out.println("   - " + r.getName() + " (" + r.getDeliveryTime() + " мин)"));

            List<Restaurant> bestRestaurants = restaurantService.filterRestaurants(4.3, 35);
            System.out.println("\nРейтинг 4.3+ и доставка до 35 мин: " + bestRestaurants.size());
            bestRestaurants.forEach(r -> System.out.println("   - " + r.getName()));

            Restaurant restaurant = restaurantService.getRestaurantById(1L);
            System.out.println("\nРесторан по ID 1: " + restaurant.getName());

            restaurantService.getRestaurantById(999L);

        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void addTestRestaurants(RestaurantService restaurantService) {
        // Ресторан 1
        Restaurant pizzaRestaurant = new Restaurant(
            1L, "Пицца Мария", 4.7, 25, 500.0,
            Arrays.asList("10:00-23:00", "10:00-23:00", "10:00-23:00", "10:00-23:00", "10:00-00:00", "10:00-00:00", "10:00-23:00"),
            true
        );

        // Проверяем тип и вызываем соответствующий метод
        if (restaurantService instanceof RestaurantServiceImpl) {
            ((RestaurantServiceImpl) restaurantService).addRestaurant(pizzaRestaurant);
        }

        // Ресторан 2
        Restaurant burgerRestaurant = new Restaurant(
            2L, "Бургер Кинг", 5.0, 20, 300.0,
            Arrays.asList("09:00-22:00", "09:00-22:00", "09:00-22:00", "09:00-22:00", "09:00-23:00", "09:00-23:00", "09:00-22:00"),
            true
        );

        if (restaurantService instanceof RestaurantServiceImpl) {
            ((RestaurantServiceImpl) restaurantService).addRestaurant(burgerRestaurant);
        }

        // Ресторан 3
        Restaurant sushiRestaurant = new Restaurant(
            3L, "Суши Вок", 4.2, 35, 600.0,
            Arrays.asList("11:00-23:00", "11:00-23:00", "11:00-23:00", "11:00-23:00", "11:00-00:00", "11:00-00:00", "11:00-23:00"),
            true
        );

        if (restaurantService instanceof RestaurantServiceImpl) {
            ((RestaurantServiceImpl) restaurantService).addRestaurant(sushiRestaurant);
        }
    }

    //History
    public static void checkUserStory17() {
        System.out.println("\nUser Story 17: История заказов");

        HistoryService historyService = new HistoryServiceImpl();

        try {
            createTestOrders(1L);

            List<Order> history = historyService.getOrderHistory(1L);
            System.out.println("История заказов пользователя:");
            System.out.println("Количество заказов: " + history.size());

            for (Order order : history) {
                System.out.println("\nЗаказ #" + order.getId());
                System.out.println("Статус: " + order.getStatus());
                System.out.println("Сумма: " + order.getTotalAmount() + " руб");
                System.out.println("Товаров: " + order.getItems().size());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка получения истории: " + e.getMessage());
        }
    }

    public static void checkUserStory18() {
        System.out.println("\nUser Story 18: Повтор заказа");

        HistoryService historyService = new HistoryServiceImpl();

        try {
            Long originalOrderId = createTestOrderForRepeat();

            Order repeatedOrder = historyService.repeatOrder(originalOrderId);
            System.out.println("Повторный заказ создан: " + repeatedOrder.getId());
            System.out.println("Статус повторного заказа: " + repeatedOrder.getStatus());
            System.out.println("Сумма повторного заказа: " + repeatedOrder.getTotalAmount() + " руб");

            Order originalOrder = historyService.getOrderById(originalOrderId);
            System.out.println("Оригинальный заказ: " + originalOrder.getId());
            System.out.println("Статус оригинального заказа: " + originalOrder.getStatus());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка повторного заказа: " + e.getMessage());
        }

        try {
            historyService.getOrderById(999L);
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка для несуществующего заказа: " + e.getMessage());
        }
    }

    private static void createTestOrders(Long userId) {
        CartService cartService = new CartServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        cartService.addItem(userId, 1L, 1L, 2);
        orderService.createOrder(
            userId, 1L, "ул. Ленина, 10",
            DeliveryType.DELIVERY, LocalDateTime.now().minusDays(2), PaymentMethod.CARD
        );

        cartService.addItem(userId, 2L, 3L, 1);
        orderService.createOrder(
            userId, 2L, "ул. Пушкина, 5",
            DeliveryType.PICKUP, LocalDateTime.now().minusDays(1), PaymentMethod.CASH
        );
    }

    private static Long createTestOrderForRepeat() {
        CartService cartService = new CartServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        cartService.addItem(2L, 1L, 1L, 1);
        Order order = orderService.createOrder(
            2L, 1L, "ул. Тестовая, 1",
            DeliveryType.DELIVERY, LocalDateTime.now(), PaymentMethod.CARD
        );
        return order.getId();
    }

    //Review
    public static void checkUserStory19() {
        System.out.println("\nUser Story 19: Написание отзыва");

        ReviewService reviewService = new ReviewServiceImpl();

        try {
            Review review1 = reviewService.createReview(100L, 5, 4, "Отличная еда и быстрая доставка");
            System.out.println("Отзыв создан: " + review1.getId());
            System.out.println("Рейтинг ресторана: " + review1.getRestaurantRating());
            System.out.println("Рейтинг курьера: " + review1.getCourierRating());
            System.out.println("Комментарий: " + review1.getComment());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания отзыва: " + e.getMessage());
        }

        try {
            Review review2 = reviewService.createReview(101L, 3, null, "Еда была холодная");
            System.out.println("Отзыв создан: " + review2.getId());
            System.out.println("Рейтинг ресторана: " + review2.getRestaurantRating());
            System.out.println("Рейтинг курьера: " + review2.getCourierRating());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания отзыва: " + e.getMessage());
        }

        try {
            reviewService.createReview(102L, 6, 5, "Тест");
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    public static void checkUserStory20() {
        System.out.println("\nUser Story 20: Просмотр отзывов и рейтингов");

        ReviewService reviewService = new ReviewServiceImpl();

        try {
            reviewService.createReview(200L, 5, 5, "Все супер!");
            reviewService.createReview(201L, 4, 3, "Нормально");
            reviewService.createReview(202L, 5, 4, "Очень вкусно");
            reviewService.createReview(203L, 2, 1, "Ужасное обслуживание");
            reviewService.createReview(204L, 4, null, "Еда хорошая");

            List<Review> reviews = reviewService.getReviews(1L);
            System.out.println("Отзывы пользователя:");
            System.out.println("Количество отзывов: " + reviews.size());

            for (Review review : reviews) {
                System.out.println("\nЗаказ " + review.getOrderId());
                System.out.println("Ресторан: " + review.getRestaurantRating());
                System.out.println("Курьер: " + review.getCourierRating());
                System.out.println("Комментарий: " + review.getComment());
            }

            Double restaurantRating = reviewService.getRestaurantRating(1L);
            System.out.println("\nРейтинг ресторана: " + restaurantRating);

            Double courierRating = reviewService.getCourierRating(1L);
            System.out.println("Рейтинг курьера: " + courierRating);

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка получения отзывов: " + e.getMessage());
        }

        try {
            Double unknownRestaurantRating = reviewService.getRestaurantRating(999L);
            System.out.println("Рейтинг несуществующего ресторана: " + unknownRestaurantRating);

            Double unknownCourierRating = reviewService.getCourierRating(999L);
            System.out.println("Рейтинг несуществующего курьера: " + unknownCourierRating);

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
