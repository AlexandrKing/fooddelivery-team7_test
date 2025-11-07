package com.team7.userstory;

import com.team7.service.Courier;
import com.team7.service.CourierService;

public class Test {
    public static void main(String[] args) {
        CourierService courierService = new CourierRegistration();

        // Регистрация курьера
        courierService.registerCourier("courier1", "Azat2007", "Азат Аминов", "azat@mail.com");
        courierService.registerCourier("courier2", "Alexandr2007", "Александр Горностаев", "alex@mail.com");
        courierService.registerCourier("courier3", "Vadim2008", "Вадим Выгон", "vadim@mail.com");

        // Проверка - получаем список (нужно привести к реализации)
        CourierRegistration serviceImpl = (CourierRegistration) courierService;
        System.out.println("Зарегистрировано курьеров: " + serviceImpl.getCouriers().size());

        for (Courier courier : serviceImpl.getCouriers()) {
            System.out.println("Курьер: " + courier.getName() + " | логин: " + courier.getLogin() + " | пароль: " + courier.getPassword() + " | почта: " + courier.getEmail() + " | время создания: " + courier.getCreatedAt() + " | деньги: " + courier.getMoney() + " | статус активности: " + courier.getActivityStatus() + " | статус: " + courier.getStatus());
        }
    }
}
