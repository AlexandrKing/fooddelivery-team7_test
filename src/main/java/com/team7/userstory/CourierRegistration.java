package com.team7.userstory;

import com.team7.service.Courier;
import com.team7.service.CourierService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CourierRegistration implements CourierService {

    private final List<Courier> couriers = new ArrayList<>();
    private long currentId = 1;
    private Courier currentCourier;


    public void registerCourier(String login, String password, String name, String email) {
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
    }
    private boolean isLoginExists(String login) {
        for (Courier courier : couriers) {
            if (courier.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
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

        registerCourier(login, password, name, email);

        System.out.println("Курьер " + name + " успешно зарегистрирован!");
    }

    public void start(String login, String activityStatus) {}

    public void end(String login, String activityStatus) {}

    public void acceptOrder(String login, Long orderId) {}

    public void takeOrder(String login, Long orderId) {}

    public void giveOrder(String login, Long orderId) {}

    public List<Courier> getCouriers() {
        return couriers;
    }


    public void login(String login, String password) {
        // Поиск курьера по логину
        Courier courier = findCourierByLogin(login);

        if (courier == null) {
            throw new IllegalArgumentException("Курьер с логином '" + login + "' не найден");
        }

        // Проверка пароля
        if (!courier.getPassword().equals(password)) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        // Проверка статуса аккаунта
        if (courier.getStatus() == Courier.Status.Banned) {
            throw new IllegalArgumentException("Аккаунт заблокирован");
        }

        // Успешная авторизация
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

    // Метод для выхода
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


    private Courier findCourierByLogin(String login) {
        for (Courier courier : couriers) {
            if (courier.getLogin().equals(login)) {
                return courier;
            }
        }
        return null;
    }
}
