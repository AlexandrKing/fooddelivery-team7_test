package com.team7.service.courieadmin;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminInterface {
    private List<Courier> couriers;
    private List<Order> orders;
    private List<Admin> admins;
    private List<Client> clients;
    private List<Restaurant> restaurants;
    private List<Review> reviews;
    private Admin currentAdmin;
    private long currentClientId = 1;
    private long currentRestaurantId = 1;
    private long currentReviewId = 1;

    public AdminInterface(List<Courier> couriers, List<Order> orders) {
        this.couriers = couriers;
        this.orders = orders;
        this.admins = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.restaurants = new ArrayList<>();
        this.reviews = new ArrayList<>();
        initializeAdmins();
        initializeTestClients();
        initializeTestRestaurants();
        initializeTestReviews();
    }

    private void initializeAdmins() {
        admins.add(new Admin("admin", "admin123", "admin@system.com"));
        admins.add(new Admin("superadmin", "super123", "super@system.com"));
    }

    private void initializeTestClients() {
        clients.add(new Client(currentClientId++, "client1", "pass123", "Иван Иванов",
                "ivan@mail.com", "+7-900-111-11-11", "ул. Ленина, 10"));
        clients.add(new Client(currentClientId++, "client2", "pass456", "Мария Петрова",
                "maria@mail.com", "+7-900-222-22-22", "пр. Мира, 25"));
        clients.add(new Client(currentClientId++, "client3", "pass789", "Алексей Сидоров",
                "alex@mail.com", "+7-900-333-33-33", "ул. Садовая, 15"));
        clients.add(new Client(currentClientId++, "client4", "pass000", "Екатерина Козлова",
                "ekaterina@mail.com", "+7-900-444-44-44", "ул. Центральная, 8"));
        clients.add(new Client(currentClientId++, "client5", "pass111", "Дмитрий Волков",
                "dmitry@mail.com", "+7-900-555-55-55", "пр. Победы, 33"));
    }

    private void initializeTestRestaurants() {
        restaurants.add(new Restaurant(currentRestaurantId++, "Пицца Мания",
                "Лучшая пицца в городе", "Итальянская",
                "ул. Пушкина, 10", "+7-800-111-11-11", "pizza@mania.com"));
        restaurants.add(new Restaurant(currentRestaurantId++, "Суши Бар",
                "Свежие суши и роллы", "Японская",
                "пр. Мира, 15", "+7-800-222-22-22", "sushi@bar.com"));
        restaurants.add(new Restaurant(currentRestaurantId++, "Бургер Хаус",
                "Американские бургеры", "Американская",
                "ул. Центральная, 33", "+7-800-333-33-33", "burger@house.com"));
        restaurants.add(new Restaurant(currentRestaurantId++, "Восточная Кухня",
                "Блюда азиатской кухни", "Азиатская",
                "ул. Заводская, 45", "+7-800-444-44-44", "eastern@kitchen.com"));
        restaurants.add(new Restaurant(currentRestaurantId++, "Кофе Пойнт",
                "Кофе и выпечка", "Кафе",
                "ул. Цветочная, 22", "+7-800-555-55-55", "coffee@point.com"));

        restaurants.get(0).setRating(4.5);
        restaurants.get(1).setRating(4.8);
        restaurants.get(2).setRating(4.2);
        restaurants.get(3).setRating(4.7);
        restaurants.get(4).setRating(4.9);
    }

    private void initializeTestReviews() {
        reviews.add(new Review(currentReviewId++, 1L, 1L, 1L, 1L, 5,
                "Отличная пицца, доставка быстрая!"));
        reviews.add(new Review(currentReviewId++, 2L, 2L, 2L, 2L, 4,
                "Вкусные суши, но немного долгая доставка"));
        reviews.add(new Review(currentReviewId++, 3L, 3L, 3L, 3L, 3,
                "Бургеры хорошие, но курьер опоздал"));
        reviews.add(new Review(currentReviewId++, 4L, 4L, 4L, 1L, 5,
                "Прекрасная азиатская кухня, всем рекомендую!"));
        reviews.add(new Review(currentReviewId++, 5L, 5L, 5L, 2L, 4,
                "Кофе отличный, выпечка свежая"));
        reviews.add(new Review(currentReviewId++, 6L, 1L, 1L, 3L, 2,
                "Пицца была холодная, не понравилось"));
        reviews.add(new Review(currentReviewId++, 7L, 2L, 2L, 1L, 5,
                "Суши просто восхитительные!"));
    }

    public List<Courier> getCouriers() {
        System.out.println("\n=== Список всех курьеров ===");
        if (couriers.isEmpty()) {
            System.out.println("Нет зарегистрированных курьеров.");
        } else {
            for (Courier courier : couriers) {
                System.out.println("ID: " + courier.getId() +
                        " | Имя: " + courier.getName() +
                        " | Логин: " + courier.getLogin() +
                        " | Email: " + courier.getEmail() +
                        " | Баланс: " + courier.getMoney() + " руб." +
                        " | Статус: " + courier.getStatus() +
                        " | Активность: " + courier.getActivityStatus() +
                        " | Зарегистрирован: " + courier.getCreatedAt());
            }
            System.out.println("Всего курьеров: " + couriers.size());
        }
        return couriers;
    }

    public void block(Long id) {
        Courier courier = findCourierById(id);
        if (courier != null) {
            courier.setStatus(Courier.Status.Banned);
            System.out.println("Курьер " + courier.getName() + " (ID: " + id + ") заблокирован");
        } else {
            System.out.println("Курьер с ID " + id + " не найден");
        }
    }

    public void unblock(Long id) {
        Courier courier = findCourierById(id);
        if (courier != null) {
            courier.setStatus(Courier.Status.Default);
            System.out.println("Курьер " + courier.getName() + " (ID: " + id + ") разблокирован");
        } else {
            System.out.println("Курьер с ID " + id + " не найден");
        }
    }

    public List<Client> getAllClients() {
        System.out.println("\n=== Список всех клиентов ===");
        if (clients.isEmpty()) {
            System.out.println("Нет зарегистрированных клиентов.");
        } else {
            for (Client client : clients) {
                System.out.println("ID: " + client.getClientId() +
                        " | Имя: " + client.getName() +
                        " | Логин: " + client.getLogin() +
                        " | Email: " + client.getEmail() +
                        " | Телефон: " + client.getPhone() +
                        " | Адрес: " + client.getAddress() +
                        " | Статус: " + (client.getIsActive() ? "Активен" : "Неактивен"));
            }
            System.out.println("Всего клиентов: " + clients.size());
        }
        return clients;
    }

    public void deactivateClient(Long clientId) {
        Client client = findClientById(clientId);
        if (client != null) {
            client.setIsActive(false);
            System.out.println("Клиент " + client.getName() + " деактивирован");
        } else {
            System.out.println("Клиент с ID " + clientId + " не найден");
        }
    }

    public void activateClient(Long clientId) {
        Client client = findClientById(clientId);
        if (client != null) {
            client.setIsActive(true);
            System.out.println("Клиент " + client.getName() + " активирован");
        } else {
            System.out.println("Клиент с ID " + clientId + " не найден");
        }
    }

    public List<Restaurant> getAllRestaurants() {
        System.out.println("\n=== Список всех ресторанов ===");
        if (restaurants.isEmpty()) {
            System.out.println("Нет зарегистрированных ресторанов.");
        } else {
            for (Restaurant restaurant : restaurants) {
                System.out.println("ID: " + restaurant.getRestaurantId() +
                        " | Название: " + restaurant.getName() +
                        " | Кухня: " + restaurant.getCuisine() +
                        " | Адрес: " + restaurant.getAddress() +
                        " | Телефон: " + restaurant.getPhone() +
                        " | Рейтинг: " + restaurant.getRating() +
                        " | Статус: " + (restaurant.getIsActive() ? "Активен" : "Неактивен"));
            }
            System.out.println("Всего ресторанов: " + restaurants.size());
        }
        return restaurants;
    }

    public void deactivateRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        if (restaurant != null) {
            restaurant.setIsActive(false);
            System.out.println("Ресторан " + restaurant.getName() + " деактивирован");
        } else {
            System.out.println("Ресторан с ID " + restaurantId + " не найден");
        }
    }

    public void activateRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        if (restaurant != null) {
            restaurant.setIsActive(true);
            System.out.println("Ресторан " + restaurant.getName() + " активирован");
        } else {
            System.out.println("Ресторан с ID " + restaurantId + " не найден");
        }
    }

    public List<Review> getAllReviews() {
        System.out.println("\n=== Список всех отзывов ===");
        if (reviews.isEmpty()) {
            System.out.println("Нет отзывов в системе.");
        } else {
            for (Review review : reviews) {
                if (review.getIsActive()) {
                    Client client = findClientById(review.getClientId());
                    Restaurant restaurant = findRestaurantById(review.getRestaurantId());
                    Courier courier = findCourierById(review.getCourierId());

                    System.out.println("Отзыв #" + review.getReviewId());
                    System.out.println("   Заказ: " + review.getOrderId());
                    System.out.println("   Клиент: " + (client != null ? client.getName() : "Неизвестен"));
                    System.out.println("   Ресторан: " + (restaurant != null ? restaurant.getName() : "Неизвестен"));
                    System.out.println("   Курьер: " + (courier != null ? courier.getName() : "Неизвестен"));
                    System.out.println("   Рейтинг: " + "⭐".repeat(review.getRating()));
                    System.out.println("   Комментарий: " + review.getComment());
                    System.out.println("   Дата: " + review.getCreatedAt());
                    System.out.println("----------------------------------------");
                }
            }
        }
        return reviews;
    }

    public void removeReview(Long reviewId) {
        Review review = findReviewById(reviewId);
        if (review != null) {
            review.setIsActive(false);
            System.out.println("Отзыв с ID " + reviewId + " удален");
        } else {
            System.out.println("Отзыв с ID " + reviewId + " не найден");
        }
    }

    public List<Order> getAllOrders() {
        System.out.println("\n=== Все заказы ===");
        if (orders.isEmpty()) {
            System.out.println("Нет заказов в системе.");
        } else {
            for (Order order : orders) {
                Courier courier = findCourierById(order.getCourierId());
                System.out.println("Заказ #" + order.getOrderId());
                System.out.println("   Ресторан ID: " + order.getRestaurantId());
                System.out.println("   Адрес ресторана: " + order.getRestaurantAddress());
                System.out.println("   Адрес клиента: " + order.getClientAddress());
                System.out.println("   Клиент: " + order.getClientLogin());
                System.out.println("   Стоимость: " + order.getPrice() + " руб.");
                System.out.println("   Прибыль: " + order.getProfit() + " руб.");
                System.out.println("   Статус: " + order.getStatus());
                System.out.println("   Курьер: " + (courier != null ? courier.getName() : "Не назначен"));
                System.out.println("   Создан: " + order.getCreatedAt());
                System.out.println("   Товары: " + order.getItems().size() + " позиций");
                System.out.println("----------------------------------------");
            }
        }
        return orders;
    }

    public void cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            System.out.println("Заказ #" + orderId + " отменен");
        } else {
            System.out.println("Заказ с ID " + orderId + " не найден");
        }
    }

    public void assignOrderToCourier(Long orderId, Long courierId) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);

        if (order != null && courier != null) {
            order.setCourierId(courierId);
            order.setStatus("ASSIGNED");
            System.out.println("Заказ #" + orderId + " назначен курьеру " + courier.getName());
        } else {
            System.out.println("Заказ или курьер не найдены");
        }
    }

    public void comissionAll(Long orderId) {
        Order order = findOrderById(orderId);
        if (order != null) {
            int count = 0;
            for (Courier courier : couriers) {
                if (courier.getActivityStatus() == Courier.ActivityStatus.Active &&
                        courier.getStatus() == Courier.Status.Default) {
                    courier.setMoney(courier.getMoney() + 10);
                    count++;
                }
            }
            System.out.println("Комиссия 10 руб. начислена " + count + " активным курьерам за заказ " + orderId);
        } else {
            System.out.println("Заказ с ID " + orderId + " не найден");
        }
    }

    public void comissionCourier(Long orderId) {
        Order order = findOrderById(orderId);
        if (order != null && order.getCourierId() != null) {
            Courier courier = findCourierById(order.getCourierId());
            if (courier != null) {
                long commission = order.getProfit() != null ? order.getProfit() / 2 : 50L;
                courier.setMoney(courier.getMoney() + commission);
                System.out.println("Комиссия " + commission + " руб. начислена курьеру " +
                        courier.getName() + " за заказ " + orderId);
            }
        } else {
            System.out.println("Заказ не найден или не назначен курьеру");
        }
    }

    public void showStatistics() {
        System.out.println("\n=== Статистика системы ===");

        long activeCouriers = couriers.stream()
                .filter(c -> c.getActivityStatus() == Courier.ActivityStatus.Active)
                .count();
        long bannedCouriers = couriers.stream()
                .filter(c -> c.getStatus() == Courier.Status.Banned)
                .count();
        long totalCourierBalance = couriers.stream()
                .mapToLong(Courier::getMoney)
                .sum();

        System.out.println("Курьеры:");
        System.out.println("   Всего: " + couriers.size());
        System.out.println("   Активных: " + activeCouriers);
        System.out.println("   Заблокированных: " + bannedCouriers);
        System.out.println("   Общий баланс: " + totalCourierBalance + " руб.");

        long activeClients = clients.stream()
                .filter(Client::getIsActive)
                .count();

        System.out.println("👥 Клиенты:");
        System.out.println("   Всего: " + clients.size());
        System.out.println("   Активных: " + activeClients);

        long activeRestaurants = restaurants.stream()
                .filter(Restaurant::getIsActive)
                .count();
        double averageRestaurantRating = restaurants.stream()
                .mapToDouble(Restaurant::getRating)
                .average()
                .orElse(0.0);

        System.out.println("Рестораны:");
        System.out.println("   Всего: " + restaurants.size());
        System.out.println("   Активных: " + activeRestaurants);
        System.out.println("   Средний рейтинг: " + String.format("%.1f", averageRestaurantRating));

        long newOrders = orders.stream().filter(o -> "NEW".equals(o.getStatus())).count();
        long acceptedOrders = orders.stream().filter(o -> "ACCEPTED".equals(o.getStatus())).count();
        long completedOrders = orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        long totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToLong(Order::getPrice)
                .sum();

        long totalProfit = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToLong(Order::getProfit)
                .sum();

        System.out.println("Заказы:");
        System.out.println("   Всего: " + orders.size());
        System.out.println("   Новых: " + newOrders);
        System.out.println("   Принятых: " + acceptedOrders);
        System.out.println("   Завершенных: " + completedOrders);
        System.out.println("   Отмененных: " + cancelledOrders);

        long activeReviews = reviews.stream()
                .filter(Review::getIsActive)
                .count();
        double averageReviewRating = reviews.stream()
                .filter(Review::getIsActive)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        System.out.println("Отзывы:");
        System.out.println("   Всего: " + reviews.size());
        System.out.println("   Активных: " + activeReviews);
        System.out.println("   Средний рейтинг: " + String.format("%.1f", averageReviewRating));

        System.out.println("Финансы:");
        System.out.println("   Общая выручка: " + totalRevenue + " руб.");
        System.out.println("   Общая прибыль: " + totalProfit + " руб.");
    }

    public boolean login(String login, String password) {
        for (Admin admin : admins) {
            if (admin.getLogin().equals(login) && admin.getPassword().equals(password)) {
                currentAdmin = admin;
                System.out.println("Администратор " + login + " успешно вошел в систему");
                return true;
            }
        }
        System.out.println("Неверный логин или пароль администратора");
        return false;
    }

    public void logout() {
        if (currentAdmin != null) {
            System.out.println("Администратор " + currentAdmin.getLogin() + " вышел из системы");
            currentAdmin = null;
        }
    }

    public boolean isLoggedIn() {
        return currentAdmin != null;
    }

    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    private Courier findCourierById(Long id) {
        for (Courier courier : couriers) {
            if (courier.getId().equals(id)) {
                return courier;
            }
        }
        return null;
    }

    private Order findOrderById(Long orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    private Client findClientById(Long clientId) {
        for (Client client : clients) {
            if (client.getClientId().equals(clientId)) {
                return client;
            }
        }
        return null;
    }

    private Restaurant findRestaurantById(Long restaurantId) {
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                return restaurant;
            }
        }
        return null;
    }

    private Review findReviewById(Long reviewId) {
        for (Review review : reviews) {
            if (review.getReviewId().equals(reviewId)) {
                return review;
            }
        }
        return null;
    }

    public void loginFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Вход для администратора ===");

        System.out.print("Логин: ");
        String login = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        login(login, password);
    }

    public void blockCourierFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID курьера для блокировки: ");
        Long id = scanner.nextLong();
        scanner.nextLine();
        block(id);
    }

    public void unblockCourierFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID курьера для разблокировки: ");
        Long id = scanner.nextLong();
        scanner.nextLine();
        unblock(id);
    }

    public void deactivateClientFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID клиента для деактивации: ");
        Long clientId = scanner.nextLong();
        scanner.nextLine();
        deactivateClient(clientId);
    }

    public void activateClientFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID клиента для активации: ");
        Long clientId = scanner.nextLong();
        scanner.nextLine();
        activateClient(clientId);
    }

    public void deactivateRestaurantFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID ресторана для деактивации: ");
        Long restaurantId = scanner.nextLong();
        scanner.nextLine();
        deactivateRestaurant(restaurantId);
    }

    public void activateRestaurantFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID ресторана для активации: ");
        Long restaurantId = scanner.nextLong();
        scanner.nextLine();
        activateRestaurant(restaurantId);
    }

    public void removeReviewFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID отзыва для удаления: ");
        Long reviewId = scanner.nextLong();
        scanner.nextLine();
        removeReview(reviewId);
    }

    public void cancelOrderFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID заказа для отмены: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();
        cancelOrder(orderId);
    }

    public void assignOrderFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID заказа для назначения: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();
        System.out.print("Введите ID курьера: ");
        Long courierId = scanner.nextLong();
        scanner.nextLine();
        assignOrderToCourier(orderId, courierId);
    }

    public void comissionAllFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID заказа для начисления комиссии всем курьерам: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();
        comissionAll(orderId);
    }

    public void comissionCourierFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID заказа для начисления комиссии курьеру: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();
        comissionCourier(orderId);
    }
}
