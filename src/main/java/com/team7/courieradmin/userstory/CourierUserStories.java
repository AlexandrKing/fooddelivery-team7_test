package com.team7.courieradmin.userstory;
import com.team7.courieradmin.service.Courier;
import com.team7.courieradmin.service.CourierService;
import com.team7.courieradmin.service.Order;
import com.team7.courieradmin.service.Item;
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
                System.out.println("Статус активности: " + current.getActivityStatus());
                System.out.println("Баланс: " + current.getMoney() + " руб.");

                showCourierMenu();
            } else {
                showGuestMenu();
            }

            System.out.print("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                if (courierService.isLoggedIn()) {
                    handleCourierMenu(choice, courierService, scanner);
                } else {
                    handleGuestMenu(choice, courierService, scanner, running);
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void showCourierMenu() {
        System.out.println("1. Начать работу");
        System.out.println("2. Завершить работу");
        System.out.println("3. Посмотреть доступные заказы");
        System.out.println("4. Взять заказ");
        System.out.println("5. Передать заказ (завершить доставку)");
        System.out.println("6. Посмотреть мой профиль");
        System.out.println("7. Пополнить баланс");
        System.out.println("8. Выйти из аккаунта");
        System.out.println("9. Выйти из программы");
    }

    private static void showGuestMenu() {
        System.out.println("1. Войти в систему");
        System.out.println("2. Зарегистрироваться");
        System.out.println("3. Выйти из программы");
    }

    private static void handleCourierMenu(int choice, CourierService service, Scanner scanner) {
        switch (choice) {
            case 1:
                startWork(service);
                break;
            case 2:
                endWork(service);
                break;
            case 3:
                showAvailableOrders(service);
                break;
            case 4:
                takeOrder(service, scanner);
                break;
            case 5:
                completeOrder(service, scanner);
                break;
            case 6:
                showMyProfile(service);
                break;
            case 7:
                addMoney(service, scanner);
                break;
            case 8:
                service.logout();
                break;
            case 9:
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private static void handleGuestMenu(int choice, CourierService service, Scanner scanner, boolean running) {
        switch (choice) {
            case 1:
                login(service, scanner);
                break;
            case 2:
                register(service, scanner);
                break;
            case 3:
                running = false;
                System.out.println("Выход из программы...");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private static void showAvailableOrders(CourierService service) {
        Courier current = service.getCurrentCourier();

        if (current.getActivityStatus() == Courier.ActivityStatus.NotActive) {
            System.out.println("Сначала начните работу!");
            return;
        }

        List<Order> availableOrders = service.getAvailableOrders();

        System.out.println("\n=== Доступные заказы ===");
        if (availableOrders.isEmpty()) {
            System.out.println("Нет доступных заказов в данный момент.");
        } else {
            for (Order order : availableOrders) {
                System.out.println("Заказ #" + order.getOrderId());
                System.out.println("   Ресторан ID: " + order.getRestaurantId());
                System.out.println("   Адрес ресторана: " + order.getRestaurantAddress());
                System.out.println("   Адрес клиента: " + order.getClientAddress());
                System.out.println("   Клиент: " + order.getClientLogin());
                System.out.println("   Стоимость заказа: " + order.getPrice() + " руб.");
                System.out.println("   Заработок курьера: " + order.getProfit() + " руб.");
                System.out.println("   Статус: " + order.getStatus());
                System.out.println("   Товары:");
                for (Item item : order.getItems()) {
                    System.out.println("      - " + item.toString());
                }
                System.out.println("----------------------------------------");
            }
        }
    }

    private static void startWork(CourierService service) {
        Courier current = service.getCurrentCourier();
        if (current.getActivityStatus() == Courier.ActivityStatus.Active) {
            System.out.println("Вы уже начали работу!");
            return;
        }

        service.start(current.getLogin(), "Active");
        System.out.println("Работа начата! Теперь вы можете принимать заказы.");
    }

    private static void endWork(CourierService service) {
        Courier current = service.getCurrentCourier();
        if (current.getActivityStatus() == Courier.ActivityStatus.NotActive) {
            System.out.println("Вы еще не начали работу!");
            return;
        }

        service.end(current.getLogin(), "NotActive");
        System.out.println("Работа завершена! Хорошего отдыха.");
    }

    private static void takeOrder(CourierService service, Scanner scanner) {
        Courier current = service.getCurrentCourier();

        if (current.getActivityStatus() == Courier.ActivityStatus.NotActive) {
            System.out.println("Сначала начните работу!");
            return;
        }

        showAvailableOrders(service);

        System.out.print("Введите ID заказа для взятия: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();

        try {
            service.takeOrder(current.getLogin(), orderId);
            System.out.println("Заказ #" + orderId + " успешно взят!");
            System.out.println("+50 руб. за взятие заказа");
            System.out.println("Текущий баланс: " + current.getMoney() + " руб.");
        } catch (Exception e) {
            System.out.println("Не удалось взять заказ: " + e.getMessage());
        }
    }

    private static void completeOrder(CourierService service, Scanner scanner) {
        Courier current = service.getCurrentCourier();

        if (current.getActivityStatus() == Courier.ActivityStatus.NotActive) {
            System.out.println("Сначала начните работу!");
            return;
        }

        System.out.print("Введите ID заказа для завершения доставки: ");
        Long orderId = scanner.nextLong();
        scanner.nextLine();

        try {
            service.giveOrder(current.getLogin(), orderId);
            System.out.println("Заказ #" + orderId + " успешно доставлен!");
            System.out.println("Текущий баланс: " + current.getMoney() + " руб.");
        } catch (Exception e) {
            System.out.println("Не удалось завершить заказ: " + e.getMessage());
        }
    }

    private static void showMyProfile(CourierService service) {
        Courier current = service.getCurrentCourier();

        System.out.println("\n=== Мой профиль ===");
        System.out.println("ID: " + current.getId());
        System.out.println("Имя: " + current.getName());
        System.out.println("Логин: " + current.getLogin());
        System.out.println("Email: " + current.getEmail());
        System.out.println("Баланс: " + current.getMoney() + " руб.");
        System.out.println("Статус активности: " + current.getActivityStatus());
        System.out.println("Статус аккаунта: " + current.getStatus());
        System.out.println("Дата регистрации: " + current.getCreatedAt());
    }

    private static void addMoney(CourierService service, Scanner scanner) {
        Courier current = service.getCurrentCourier();

        System.out.print("Введите сумму для пополнения: ");
        Long amount = scanner.nextLong();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Сумма должна быть положительной!");
            return;
        }

        current.setMoney(current.getMoney() + amount);
        System.out.println("Баланс пополнен на " + amount + " руб.");
        System.out.println("Текущий баланс: " + current.getMoney() + " руб.");
    }

    private static void login(CourierService service, Scanner scanner) {
        System.out.println("\n=== Вход в систему ===");

        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        try {
            service.login(login, password);
        } catch (Exception e) {
            System.out.println("Ошибка входа: " + e.getMessage());
        }
    }

    private static void register(CourierService service, Scanner scanner) {
        System.out.println("\n=== Регистрация нового курьера ===");

        String login;
        boolean loginExists;

        do {
            System.out.print("Введите логин: ");
            login = scanner.nextLine();

            loginExists = service.isLoginExists(login);
            if (loginExists) {
                System.out.println("Логин '" + login + "' уже занят. Попробуйте другой.");
            }
        } while (loginExists);

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        try {
            service.registerCourier(login, password, name, email);
            System.out.println("Регистрация прошла успешно! Теперь вы можете войти в систему.");
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }
}