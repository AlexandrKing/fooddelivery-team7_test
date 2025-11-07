package com.team7.restaurant.userstory;

import com.team7.restaurant.service.AuthService;
import com.team7.restaurant.service.RestaurantService;
import com.team7.restaurant.model.Restaurant;
import java.util.List;
import java.util.Scanner;

public class RestaurantUserStories {
  private static Restaurant currentRestaurant = null;

  public static void main(String[] args) {
    AuthService authService = new AuthService();
    RestaurantService restaurantService = new RestaurantService();
    Scanner scanner = new Scanner(System.in);

    boolean running = true;

    while (running) {
      System.out.println("\n=== Панель управления рестораном ===");

      currentRestaurant = getCurrentRestaurant(authService);

      if (currentRestaurant != null) {
        System.out.println("Текущий ресторан: " + currentRestaurant.getName());
        System.out.println("1. Показать профиль ресторана");
        System.out.println("2. Изменить данные ресторана");
        System.out.println("3. Управление меню");
        System.out.println("4. Выйти из системы");
        System.out.println("5. Выйти из программы");
      } else {
        System.out.println("1. Зарегистрировать ресторан");
        System.out.println("2. Войти в систему");
        System.out.println("3. Выйти из программы");
      }

      System.out.print("Выберите действие: ");
      int choice = scanner.nextInt();
      scanner.nextLine();

      try {
        if (currentRestaurant != null) {
          running = handleRestaurantMenu(choice, authService, restaurantService, scanner, currentRestaurant);
        } else {
          running = handleAuthMenu(choice, authService, restaurantService, scanner);
        }
      } catch (Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
        e.printStackTrace();
      }
    }

    scanner.close();
    System.out.println("Программа завершена.");
  }

  private static Restaurant getCurrentRestaurant(AuthService authService) {
    if (currentRestaurant != null) {
      return currentRestaurant;
    }

    try {
      List<Restaurant> restaurants = authService.getAllRestaurants();
      return restaurants.stream()
          .filter(r -> "ACTIVE".equals(r.getStatus()))
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      return null;
    }
  }

  private static boolean handleRestaurantMenu(int choice, AuthService authService,
                                              RestaurantService restaurantService,
                                              Scanner scanner, Restaurant currentRestaurant) {
    switch (choice) {
      case 1:
        showRestaurantProfile(currentRestaurant);
        break;
      case 2:
        updateRestaurantData(restaurantService, scanner, currentRestaurant);
        break;
      case 3:
        manageMenu(scanner, currentRestaurant);
        break;
      case 4:
        logout(authService);
        return true;
      case 5:
        System.out.println("Выход из программы...");
        return false;
      default:
        System.out.println("Неверный выбор!");
    }
    return true;
  }

  private static boolean handleAuthMenu(int choice, AuthService authService,
                                        RestaurantService restaurantService,
                                        Scanner scanner) {
    switch (choice) {
      case 1:
        registerRestaurant(authService, scanner);
        break;
      case 2:
        loginRestaurant(authService, scanner);
        break;
      case 3:
        System.out.println("Выход из программы...");
        return false;
      default:
        System.out.println("Неверный выбор!");
    }
    return true;
  }

  private static void registerRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n=== РЕГИСТРАЦИЯ РЕСТОРАНА ===");

    System.out.print("Название ресторана: ");
    String name = scanner.nextLine();

    System.out.print("Email: ");
    String email = scanner.nextLine();

    System.out.print("Пароль (мин. 6 символов): ");
    String password = scanner.nextLine();

    System.out.print("Подтверждение пароля: ");
    String confirmPassword = scanner.nextLine();

    if (!password.equals(confirmPassword)) {
      System.out.println("Ошибка: Пароли не совпадают!");
      return;
    }

    if (password.length() < 6) {
      System.out.println("Ошибка: Пароль должен содержать минимум 6 символов!");
      return;
    }

    System.out.print("Телефон: ");
    String phone = scanner.nextLine();

    System.out.print("Адрес: ");
    String address = scanner.nextLine();

    System.out.print("Тип кухни: ");
    String cuisineType = scanner.nextLine();

    try {
      Restaurant restaurant = authService.registerRestaurant(name, email, password, phone, address, cuisineType);

      System.out.println("\nРЕГИСТРАЦИЯ УСПЕШНА!");
      System.out.println("ID ресторана: " + restaurant.getId());
      System.out.println("Статус: " + restaurant.getStatus());
      System.out.println("Сообщение: Ресторан отправлен на модерацию");

      currentRestaurant = restaurant;

    } catch (IllegalArgumentException e) {
      System.out.println("Ошибка регистрации: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Системная ошибка при регистрации: " + e.getMessage());
    }
  }

  private static void loginRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n=== ВХОД В СИСТЕМУ ===");

    System.out.print("Email: ");
    String email = scanner.nextLine();

    System.out.print("Пароль: ");
    String password = scanner.nextLine();

    try {
      Restaurant restaurant = authService.login(email, password);

      if (restaurant != null) {
        System.out.println("\nВХОД ВЫПОЛНЕН УСПЕШНО!");
        System.out.println("Добро пожаловать, " + restaurant.getName());
        System.out.println("Статус ресторана: " + restaurant.getStatus());
        currentRestaurant = restaurant;
      } else {
        System.out.println("ОШИБКА ВХОДА: Неверный email или пароль");
      }
    } catch (Exception e) {
      System.out.println("Ошибка при входе: " + e.getMessage());
    }
  }

  private static void updateRestaurantData(RestaurantService restaurantService,
                                           Scanner scanner, Restaurant currentRestaurant) {
    System.out.println("\n=== ИЗМЕНЕНИЕ ДАННЫХ РЕСТОРАНА ===");

    System.out.println("Текущие данные:");
    showRestaurantProfile(currentRestaurant);

    System.out.println("\nВведите новые данные (оставьте пустым чтобы не менять):");

    System.out.print("Новое название: ");
    String newName = scanner.nextLine();

    System.out.print("Новый телефон: ");
    String newPhone = scanner.nextLine();

    System.out.print("Новый адрес: ");
    String newAddress = scanner.nextLine();

    System.out.print("Новый тип кухни: ");
    String newCuisineType = scanner.nextLine();

    System.out.print("Новое описание: ");
    String newDescription = scanner.nextLine();

    try {
      Restaurant updatedRestaurant = restaurantService.updateRestaurant(
          currentRestaurant.getId(),
          newName.isEmpty() ? null : newName,
          newPhone.isEmpty() ? null : newPhone,
          newAddress.isEmpty() ? null : newAddress,
          newCuisineType.isEmpty() ? null : newCuisineType,
          newDescription.isEmpty() ? null : newDescription
      );

      if (updatedRestaurant != null) {
        System.out.println("\nДАННЫЕ УСПЕШНО ОБНОВЛЕНЫ!");
        currentRestaurant = updatedRestaurant;

        if (!newName.isEmpty() || !newAddress.isEmpty()) {
          System.out.println("Уведомление: Изменение критических данных требует модерации");
        }
      } else {
        System.out.println("Ошибка обновления данных");
      }
    } catch (Exception e) {
      System.out.println("Ошибка при обновлении данных: " + e.getMessage());
    }
  }

  private static void manageMenu(Scanner scanner, Restaurant currentRestaurant) {
    System.out.println("\n=== УПРАВЛЕНИЕ МЕНЮ ===");
    System.out.println("Функционал управления меню для ресторана: " + currentRestaurant.getName());
    System.out.println("Здесь будет реализовано:");
    System.out.println("- Добавление блюд");
    System.out.println("- Удаление блюд");
    System.out.println("- Изменение блюд");
    System.out.println("- Управление доступностью блюд");
    System.out.println("(Этот функционал будет подключен после реализации MenuService)");
  }

  private static void showRestaurantProfile(Restaurant restaurant) {
    System.out.println("\n=== ПРОФИЛЬ РЕСТОРАНА ===");
    System.out.println("ID: " + restaurant.getId());
    System.out.println("Название: " + restaurant.getName());
    System.out.println("Email: " + restaurant.getEmail());
    System.out.println("Телефон: " + restaurant.getPhone());
    System.out.println("Адрес: " + restaurant.getAddress());
    System.out.println("Тип кухни: " + restaurant.getCuisineType());
    System.out.println("Описание: " + (restaurant.getDescription() != null ? restaurant.getDescription() : "Не указано"));
    System.out.println("Статус: " + restaurant.getStatus());
    System.out.println("Дата регистрации: " + restaurant.getRegistrationDate());
    System.out.println("Email верифицирован: " + restaurant.getEmailVerified());
  }

  private static void logout(AuthService authService) {
    System.out.println("Выход из системы...");
    currentRestaurant = null;
    System.out.println("Вы успешно вышли из системы.");
  }
}