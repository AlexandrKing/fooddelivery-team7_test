package com.team7.courieradmin.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CourierService {

    public static final List<Courier> couriers = new ArrayList<>();
    private long currentId = 1;
    private Courier currentCourier; // Текущий авторизованный курьер


    public void registerCourier(String login, String password, String name, String email) {

        if (isLoginExists(login)) {
            throw new IllegalArgumentException("Логин '" + login + "' уже занят. Выберите другой логин.");
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

    public void loginFromInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Вход в систему ===");

        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        try {
            login(login, password);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка входа: " + e.getMessage());
        }
    }

    public void logout() {
        if (currentCourier != null) {
            System.out.println("До свидания, " + currentCourier.getName() + "!");
            currentCourier = null;
        } else {
            System.out.println("Вы не авторизованы");
        }
    }

    public boolean isLoggedIn() {
        return currentCourier != null;
    }

    public Courier getCurrentCourier() {
        return currentCourier;
    }

    private boolean isLoginExists(String login) {
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

    public void registerCourierFromInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Регистрация нового курьера ===");

        String login;
        boolean loginExists;

        do {
            System.out.print("Введите логин: ");
            login = scanner.nextLine();

            loginExists = isLoginExists(login);
            if (loginExists) {
                System.out.println("Логин '" + login + "' уже занят. Попробуйте другой логин.");
            }
        } while (loginExists);

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        try {
            registerCourier(login, password, name, email);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
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
        checkAuthorization();
        System.out.println("Курьер " + currentCourier.getName() + " взял заказ " + orderId);
    }


    public void giveOrder(String login, Long orderId) {
        checkAuthorization();
        System.out.println("Курьер " + currentCourier.getName() + " передал заказ " + orderId);
    }

    private void checkAuthorization() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Для выполнения действия необходимо авторизоваться");
        }
    }

    public List<Courier> getCouriers() {
        return couriers;
    }
}
