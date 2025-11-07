package com.team7.userstory;
import com.team7.service.Courier;
import com.team7.service.CourierService;
import java.util.Scanner;
import java.util.List;

public class CourierUserStories {
    public static void main(String[] args) {
        CourierService courierService = new CourierService();
        Scanner scanner = new Scanner(System.in);

        courierService.registerCourier("courier1", "Azat2007", "Азат Аминов", "azat@mail.com");
        courierService.registerCourier("courier2", "Alexandr2007", "Александр Горностаев", "alex@mail.com");
        courierService.registerCourier("courier3", "Vadim2008", "Вадим Выгон", "vadim@mail.com");

        boolean running = true;

        while (running) {
            System.out.println("\n=== Система курьеров ===");

            if (courierService.isLoggedIn()) {
                Courier current = courierService.getCurrentCourier();
                System.out.println("Текущий пользователь: " + current.getName() + " (" + current.getLogin() + ")");
                System.out.println("1. Начать работу");
                System.out.println("2. Завершить работу");
                System.out.println("3. Принять заказ");
                System.out.println("4. Взять заказ");
                System.out.println("5. Передать заказ");
                System.out.println("6. Выйти из аккаунта");
                System.out.println("7. Показать всех курьеров");
                System.out.println("8. Выйти из программы");
            } else {
                System.out.println("1. Войти в систему");
                System.out.println("2. Зарегистрироваться");
                System.out.println("3. Показать всех курьеров");
                System.out.println("4. Выйти из программы");
            }

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // очистка буфера

            try {
                if (courierService.isLoggedIn()) {
                    handleLoggedInMenu(choice, courierService, scanner);
                } else {
                    handleLoggedOutMenu(choice, courierService, scanner, running);
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void handleLoggedInMenu(int choice, CourierService service, Scanner scanner) {
        switch (choice) {
            case 1:
                service.start(service.getCurrentCourier().getLogin(), "Active");
                break;
            case 2:
                service.end(service.getCurrentCourier().getLogin(), "NotActive");
                break;
            case 3:
                System.out.print("Введите ID заказа: ");
                Long orderId1 = scanner.nextLong();
                service.acceptOrder(service.getCurrentCourier().getLogin(), orderId1);
                break;
            case 4:
                System.out.print("Введите ID заказа: ");
                Long orderId2 = scanner.nextLong();
                service.takeOrder(service.getCurrentCourier().getLogin(), orderId2);
                break;
            case 5:
                System.out.print("Введите ID заказа: ");
                Long orderId3 = scanner.nextLong();
                service.giveOrder(service.getCurrentCourier().getLogin(), orderId3);
                break;
            case 6:
                service.logout();
                break;
            case 7:
                showAllCouriers(service);
                break;
            case 8:
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private static void handleLoggedOutMenu(int choice, CourierService service, Scanner scanner, boolean running) {
        switch (choice) {
            case 1:
                service.loginFromInput();
                break;
            case 2:
                service.registerCourierFromInput();
                break;
            case 3:
                showAllCouriers(service);
                break;
            case 4:
                running = false;
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private static void showAllCouriers(CourierService courierService) {
        List<Courier> couriers = courierService.getCouriers();

        if (couriers.isEmpty()) {
            System.out.println("Нет зарегистрированных курьеров.");
        } else {
            System.out.println("\n=== Список всех курьеров ===");
            for (Courier courier : couriers) {
                System.out.println("ID: " + courier.getId() +
                        " | Курьер: " + courier.getName() +
                        " | логин: " + courier.getLogin() +
                        " | пароль: " + courier.getPassword() +
                        " | почта: " + courier.getEmail() +
                        " | время создания: " + courier.getCreatedAt() +
                        " | деньги: " + courier.getMoney() +
                        " | статус активности: " + courier.getActivityStatus() +
                        " | статус: " + courier.getStatus());
            }
            System.out.println("Всего курьеров: " + couriers.size());
        }
    }
}