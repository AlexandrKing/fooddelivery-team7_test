package com.team7.userstory;
import com.team7.service.Courier;
import com.team7.service.CourierService;
import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

public class CourierUserStories {
    public static List<Courier> couriers = new ArrayList<>();
    public long currentId = 1;

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
        System.out.println("Курьер добавлен! Всего курьеров: " + couriers.size());
    }

    public List<Courier> getCouriers() {
        return couriers;
    }
    public static void main(String[] args) {
        CourierService courierService = new CourierService() {
            @Override
            public void registerCourier(String login, String password, String name, String email) {

            }

            @Override
            public void login(String login, String password) {

            }

            @Override
            public void start(String login, String activityStatus) {

            }

            @Override
            public void end(String login, String activityStatus) {

            }

            @Override
            public void acceptOrder(String login, Long orderId) {

            }

            @Override
            public void takeOrder(String login, Long orderId) {

            }

            @Override
            public void giveOrder(String login, Long orderId) {

            }

            @Override
            public List<Courier> getCouriers() {
                return couriers;
            }
        };

        // Регистрация курьера
        courierService.registerCourier("courier1", "pass123", "Иван Иванов", "ivan@mail.com");
        courierService.registerCourier("courier2", "pass456", "Петр Петров", "petr@mail.com");

        // Проверка - получаем список (нужно привести к реализации)
        System.out.println("Зарегистрировано курьеров: " + ((CourierService) courierService).getCouriers().size());

        for (Courier courier : ((CourierService) courierService).getCouriers()) {
            System.out.println("Курьер: " + courier.getName() + ", логин: " + courier.getLogin());
        }
    }
}
