package com.team7.courieradmin.userstory;

import com.team7.courieradmin.service.Admin;
import com.team7.courieradmin.service.AdminInterface;
import com.team7.courieradmin.service.Courier;
import com.team7.courieradmin.service.CourierService;
import java.util.List;
import java.util.Scanner;

public class AdminUserStories {
    public static void main(String[] args) {
        // Создаем сервис курьеров и добавляем тестовых курьеров
        CourierService courierService = new CourierService();
        courierService.registerCourier("courier1", "Azat2007", "Азат Аминов", "azat@mail.com");
        courierService.registerCourier("courier2", "Alexandr2007", "Александр Горностаев", "alex@mail.com");
        courierService.registerCourier("courier3", "Vadim2008", "Вадим Выгон", "vadim@mail.com");

        // Получаем список курьеров для административного сервиса
        List<Courier> couriers = courierService.getCouriers();

        // Создаем административный сервис
        AdminInterface adminService = new AdminInterface(couriers);
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("\n=== Административная панель ===");

            if (adminService.isLoggedIn()) {
                Admin currentAdmin = adminService.getCurrentAdmin();
                System.out.println("Текущий администратор: " + currentAdmin.getLogin());
                System.out.println("1. Показать всех курьеров");
                System.out.println("2. Выйти из системы");
                System.out.println("3. Выйти из программы");
            } else {
                System.out.println("1. Войти как администратор");
                System.out.println("2. Выйти из программы");
            }

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // очистка буфера

            try {
                if (adminService.isLoggedIn()) {
                    handleAdminMenu(choice, adminService, scanner);
                } else {
                    handleLoginMenu(choice, adminService, scanner, running);
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void handleAdminMenu(int choice, AdminInterface adminService, Scanner scanner) {
        switch (choice) {
            case 1:
                adminService.getCouriers();
                break;
            case 2:
                adminService.logout();
                break;
            case 3:
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private static void handleLoginMenu(int choice, AdminInterface adminService, Scanner scanner, boolean running) {
        switch (choice) {
            case 1:
                adminService.loginFromInput();
                break;
            case 2:
                running = false;
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }
}