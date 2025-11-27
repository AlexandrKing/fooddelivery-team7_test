package com.team7.courieradmin.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CourierService {

    private List<Courier> couriers = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private long currentId = 1;
    private long currentOrderId = 1;
    private Courier currentCourier;

    public CourierService() {
        initializeTestOrders();
    }

    private void initializeTestOrders() {

        Order pizzaOrder = new Order(currentOrderId++, 1L, 101L, "client1",
                "ул. Пушкина, 10", "ул. Лермонтова, 25");
        pizzaOrder.addItem(new Item(1L, "Пицца Маргарита", "Классическая пицца", 450L, 1));
        pizzaOrder.addItem(new Item(2L, "Кола", "Напиток 0.5л", 100L, 2));
        pizzaOrder.calculateTotalPrice();
        orders.add(pizzaOrder);

        Order sushiOrder = new Order(currentOrderId++, 2L, 102L, "client2",
                "пр. Мира, 15", "ул. Садовая, 8");
        sushiOrder.addItem(new Item(3L, "Ролл Филадельфия", "8 шт.", 320L, 1));
        sushiOrder.addItem(new Item(4L, "Ролл Калифорния", "8 шт.", 280L, 1));
        sushiOrder.addItem(new Item(5L, "Суп Мисо", "Традиционный японский суп", 150L, 1));
        sushiOrder.calculateTotalPrice();
        orders.add(sushiOrder);

        Order burgerOrder = new Order(currentOrderId++, 3L, 103L, "client3",
                "ул. Центральная, 33", "ул. Молодежная, 12");
        burgerOrder.addItem(new Item(6L, "Чизбургер", "С говяжьей котлетой", 220L, 2));
        burgerOrder.addItem(new Item(7L, "Картофель фри", "Средняя порция", 120L, 1));
        burgerOrder.addItem(new Item(8L, "Молочный коктейль", "Ванильный", 180L, 1));
        burgerOrder.calculateTotalPrice();
        orders.add(burgerOrder);

        Order asianOrder = new Order(currentOrderId++, 4L, 104L, "client4",
                "ул. Заводская, 45", "пр. Победы, 67");
        asianOrder.addItem(new Item(9L, "Пад Тай", "Тайская лапша", 380L, 1));
        asianOrder.addItem(new Item(10L, "Том Ям", "Острый суп", 290L, 1));
        asianOrder.addItem(new Item(11L, "Спринг-роллы", "4 шт.", 190L, 1));
        asianOrder.calculateTotalPrice();
        orders.add(asianOrder);

        Order coffeeOrder = new Order(currentOrderId++, 5L, 105L, "client5",
                "ул. Цветочная, 22", "ул. Солнечная, 15");
        coffeeOrder.addItem(new Item(12L, "Капучино", "Большой", 180L, 2));
        coffeeOrder.addItem(new Item(13L, "Круассан", "Шоколадный", 90L, 3));
        coffeeOrder.addItem(new Item(14L, "Чизкейк", "Нью-Йорк", 220L, 1));
        coffeeOrder.calculateTotalPrice();
        orders.add(coffeeOrder);
    }

    public List<Order> getAvailableOrders() {
        List<Order> availableOrders = new ArrayList<>();
        for (Order order : orders) {
            if ("NEW".equals(order.getStatus())) {
                availableOrders.add(order);
            }
        }
        return availableOrders;
    }

    public boolean takeOrder(Long orderId) {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Для взятия заказа необходимо авторизоваться");
        }

        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ с ID " + orderId + " не найден");
        }

        if (!"NEW".equals(order.getStatus())) {
            throw new IllegalArgumentException("Заказ уже взят другим курьером");
        }

        order.setStatus("ACCEPTED");
        order.setCourierId(currentCourier.getId());

        currentCourier.setMoney(currentCourier.getMoney() + 50L);

        return true;
    }

    public boolean completeOrder(Long orderId) {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Для передачи заказа необходимо авторизоваться");
        }

        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ с ID " + orderId + " не найден");
        }

        if (order.getCourierId() == null || !order.getCourierId().equals(currentCourier.getId())) {
            throw new IllegalArgumentException("Этот заказ не был вами взят");
        }

        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Невозможно передать заказ в текущем статусе");
        }

        order.setStatus("COMPLETED");

        currentCourier.setMoney(currentCourier.getMoney() + order.getProfit());

        return true;
    }

    private Order findOrderById(Long orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    public void registerCourier(String login, String password, String name, String email) {
        if (isLoginExists(login)) {
            throw new IllegalArgumentException("Логин '" + login + "' уже занят.");
        }

        Courier courier = new Courier();
        courier.setId(currentId++);
        courier.setLogin(login);
        courier.setPassword(password);
        courier.setName(name);
        courier.setEmail(email);
        courier.setCreatedAt(Instant.now());
        courier.setMoney(0L);
        courier.setActivityStatus(Courier.ActivityStatus.NotActive);
        courier.setStatus(Courier.Status.Default);

        couriers.add(courier);
        System.out.println("Курьер успешно зарегистрирован!");
    }

    public void login(String login, String password) {
        Courier courier = findCourierByLogin(login);

        if (courier == null) {
            throw new IllegalArgumentException("Курьер с логином '" + login + "' не найден");
        }

        if (!courier.getPassword().equals(password)) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        if (courier.getStatus() == Courier.Status.Banned) {
            throw new IllegalArgumentException("Аккаунт заблокирован");
        }

        currentCourier = courier;
        System.out.println("Успешный вход! Добро пожаловать, " + courier.getName() + "!");
    }

    public void start(String login, String activityStatus) {
        checkAuthorization();
        System.out.println("Курьер " + currentCourier.getName() + " начал работу");
        currentCourier.setActivityStatus(Courier.ActivityStatus.Active);
    }

    public void end(String login, String activityStatus) {
        checkAuthorization();
        System.out.println("Курьер " + currentCourier.getName() + " завершил работу");
        currentCourier.setActivityStatus(Courier.ActivityStatus.NotActive);
    }

    public void acceptOrder(String login, Long orderId) {
        checkAuthorization();
        System.out.println("Курьер " + currentCourier.getName() + " принял заказ " + orderId);
    }

    public void takeOrder(String login, Long orderId) {
        if (takeOrder(orderId)) {
            System.out.println("Курьер " + currentCourier.getName() + " взял заказ " + orderId + "! +50 руб. к балансу");
        }
    }

    public void giveOrder(String login, Long orderId) {
        if (completeOrder(orderId)) {
            Order order = findOrderById(orderId);
            System.out.println("Курьер " + currentCourier.getName() + " передал заказ " + orderId +
                    "! +" + order.getProfit() + " руб. к балансу");
        }
    }

    public List<Courier> getCouriers() {
        return couriers;
    }

    public boolean isLoginExists(String login) {
        for (Courier courier : couriers) {
            if (courier.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private Courier findCourierByLogin(String login) {
        for (Courier courier : couriers) {
            if (courier.getLogin().equals(login)) {
                return courier;
            }
        }
        return null;
    }

    private void checkAuthorization() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Для выполнения действия необходимо авторизоваться");
        }
    }

    public boolean isLoggedIn() {
        return currentCourier != null;
    }

    public Courier getCurrentCourier() {
        return currentCourier;
    }

    public void logout() {
        if (currentCourier != null) {
            System.out.println("До свидания, " + currentCourier.getName() + "!");
            currentCourier = null;
        }
    }
}