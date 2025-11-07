package com.team7.userstory;
import com.team7.service.Courier;
import com.team7.service.CourierService;
import java.util.Scanner;
import java.util.List;

public class CourierRegistrationWithDB {
    public static void main(String[] args) {
        CourierService courierService = new CourierRegistration();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Регистрация курьера
        courierService.registerCourier("courier1", "Azat2007", "Азат Аминов", "azat@mail.com");
        courierService.registerCourier("courier2", "Alexandr2007", "Александр Горностаев", "alex@mail.com");
        courierService.registerCourier("courier3", "Vadim2008", "Вадим Выгон", "vadim@mail.com");



        while (running) {
            System.out.println("\n=== Система регистрации курьеров ===");
            System.out.println("1. Зарегистрировать курьера");
            System.out.println("2. Показать всех курьеров");
            System.out.println("3. Выйти");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // очистка буфера

            switch (choice) {
                case 1:
                    ((CourierRegistration) courierService).registerCourierFromInput();
                    break;

                case 2:
                    showAllCouriers((CourierRegistration) courierService);
                    break;

                case 3:
                    running = false;
                    System.out.println("Выход из программы...");
                    break;

                default:
                    System.out.println("Неверный выбор!");
            }
        }

        scanner.close();
    }

    private static void showAllCouriers(CourierRegistration courierService) {
        List<Courier> couriers = courierService.getCouriers();

        if (couriers.isEmpty()) {
            System.out.println("Нет зарегистрированных курьеров.");
        } else {
            System.out.println("\n=== Список всех курьеров ===");
            CourierRegistration serviceImpl = (CourierRegistration) courierService;
            System.out.println("Зарегистрировано курьеров: " + serviceImpl.getCouriers().size());
            for (Courier courier : couriers) {
                System.out.println("ID: " + courier.getId() + " | Курьер: " + courier.getName() + " | логин: " + courier.getLogin() + " | пароль: " + courier.getPassword() + " | почта: " + courier.getEmail() + " | время создания: " + courier.getCreatedAt() + " | деньги: " + courier.getMoney() + " | статус активности: " + courier.getActivityStatus() + " | статус: " + courier.getStatus());
            }
        }
    }
}
