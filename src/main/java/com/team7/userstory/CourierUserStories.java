package com.team7.userstory;
import com.team7.service.Courier;
import com.team7.service.CourierService;

import java.util.ArrayList;
import java.util.List;

public class CourierUserStories {
    public static List<Courier> couriers = new ArrayList<>();
    public static void main(String[] args) {
        CourierService courierService = new CourierServiceImpl();

        try {
            courierService.registerCourier("ivan_2024", "secure123", "Иван Петров", "ivan@example.com");
            System.out.println("Регистрация прошла успешно!");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }
}
