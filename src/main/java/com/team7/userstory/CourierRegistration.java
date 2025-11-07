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

    public void registerCourierFromInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Регистрация нового курьера ===");

        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        // Вызов метода регистрации
        registerCourier(login, password, name, email);

        System.out.println("Курьер " + name + " успешно зарегистрирован!");
    }


    public void login(String login, String password) {}

    public void start(String login, String activityStatus) {}

    public void end(String login, String activityStatus) {}

    public void acceptOrder(String login, Long orderId) {}

    public void takeOrder(String login, Long orderId) {}

    public void giveOrder(String login, Long orderId) {}

    public List<Courier> getCouriers() {
        return couriers;
    }
}
