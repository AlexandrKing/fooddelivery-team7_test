package com.team7.userstory.courieadmin;

import com.team7.service.courieadmin.Admin;
import com.team7.service.courieadmin.AdminInterface;
import com.team7.service.courieadmin.Courier;
import com.team7.service.courieadmin.CourierService;
import com.team7.service.courieadmin.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AdminUserStories {
    public static void main(String[] args) {
        CourierService courierService = new CourierService();
        courierService.registerCourier("courier1", "Azat2007", "Азат Аминов", "azat@mail.com");
        courierService.registerCourier("courier2", "Alexandr2007", "Александр Горностаев", "alex@mail.com");
        courierService.registerCourier("courier3", "Vadim2008", "Вадим Выгон", "vadim@mail.com");

        List<Courier> couriers = courierService.getCouriers();
        List<Order> orders = getOrdersFromCourierService(courierService);

        AdminInterface adminService = new AdminInterface(couriers, orders);
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("\n=== Административная панель ===");

            if (adminService.isLoggedIn()) {
                Admin currentAdmin = adminService.getCurrentAdmin();
                System.out.println("Текущий администратор: " + currentAdmin.getLogin());
                System.out.println("=== Управление курьерами ===");
                System.out.println("1. Показать всех курьеров");
                System.out.println("2. Заблокировать курьера");
                System.out.println("3. Разблокировать курьера");

                System.out.println("=== Управление клиентами ===");
                System.out.println("4. Показать всех клиентов");
                System.out.println("5. Деактивировать клиента");
                System.out.println("6. Активировать клиента");

                System.out.println("=== Управление ресторанами ===");
                System.out.println("7. Показать все рестораны");
                System.out.println("8. Деактивировать ресторан");
                System.out.println("9. Активировать ресторан");

                System.out.println("=== Управление отзывами ===");
                System.out.println("10. Показать все отзывы");
                System.out.println("11. Удалить отзыв");

                System.out.println("=== Управление заказами ===");
                System.out.println("12. Показать все заказы");
                System.out.println("13. Отменить заказ");
                System.out.println("14. Назначить заказ курьеру");

                System.out.println("=== Статистика ===");
                System.out.println("15. Показать статистику");

                System.out.println("=== Система ===");
                System.out.println("16. Выйти из системы");
                System.out.println("17. Выйти из программы");
            } else {
                System.out.println("1. Войти как администратор");
                System.out.println("2. Выйти из программы");
            }

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

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
                adminService.blockCourierFromInput();
                break;
            case 3:
                adminService.unblockCourierFromInput();
                break;
            case 4:
                adminService.getAllClients();
                break;
            case 5:
                adminService.deactivateClientFromInput();
                break;
            case 6:
                adminService.activateClientFromInput();
                break;
            case 7:
                adminService.getAllRestaurants();
                break;
            case 8:
                adminService.deactivateRestaurantFromInput();
                break;
            case 9:
                adminService.activateRestaurantFromInput();
                break;
            case 10:
                adminService.getAllReviews();
                break;
            case 11:
                adminService.removeReviewFromInput();
                break;
            case 12:
                adminService.getAllOrders();
                break;
            case 13:
                adminService.cancelOrderFromInput();
                break;
            case 14:
                adminService.assignOrderFromInput();
                break;
            case 15:
                adminService.showStatistics();
                break;
            case 16:
                adminService.logout();
                break;
            case 17:
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

    private static List<Order> getOrdersFromCourierService(CourierService courierService) {
        try {
            java.lang.reflect.Field ordersField = CourierService.class.getDeclaredField("orders");
            ordersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) ordersField.get(courierService);
            return orders;
        } catch (Exception e) {
            System.out.println("Не удалось получить список заказов: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}