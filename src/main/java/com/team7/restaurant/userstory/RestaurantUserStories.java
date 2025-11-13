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
      System.out.println("=== Панель управления рестораном ===");

      if (currentRestaurant != null) {
        System.out.println("Текущий ресторан: " + currentRestaurant.getName());
        System.out.println("1. Показать профиль ресторана");
        System.out.println("2. Изменить данные ресторана");
        System.out.println("3. Выйти из системы");
        System.out.println("4. Выйти из программы");
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
          handleRestaurantMenu(choice, authService, restaurantService, scanner);
        } else {
          handleAuthMenu(choice, authService, restaurantService, scanner, running);
        }
      } catch (Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }

    scanner.close();
  }

  private static void handleRestaurantMenu(int choice, AuthService authService,
                                           RestaurantService restaurantService,
                                           Scanner scanner) {
    switch (choice) {
      case 1:
        showRestaurantProfile(currentRestaurant);
        break;
      case 2:
        updateRestaurantData(restaurantService, scanner);
        break;
      case 3:
        logout();
        break;
      case 4:
        System.out.println("Выход из программы...");
        System.exit(0);
        break;
      default:
        System.out.println("Неверный выбор!");
    }
  }

  private static void handleAuthMenu(int choice, AuthService authService,
                                     RestaurantService restaurantService,
                                     Scanner scanner, boolean running) {
    switch (choice) {
      case 1:
        registerRestaurant(authService, scanner);
        break;
      case 2:
        loginRestaurant(authService, scanner);
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

  private static void registerRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("=== РЕГИСТРАЦИЯ РЕСТОРАНА ===");

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

    System.out.print("Телефон: ");
    String phone = scanner.nextLine();

    System.out.print("Адрес: ");
    String address = scanner.nextLine();

    System.out.print("Тип кухни: ");
    String cuisineType = scanner.nextLine();

    try {
      Restaurant restaurant = authService.registerRestaurant(name, email, password, phone, address, cuisineType);

      System.out.println("РЕГИСТРАЦИЯ УСПЕШНА!");
      System.out.println("ID ресторана: " + restaurant.getId());
      System.out.println("Статус: " + restaurant.getStatus());
      System.out.println("Сообщение: Ресторан отправлен на модерацию");

    } catch (IllegalArgumentException e) {
      System.out.println("Ошибка регистрации: " + e.getMessage());
    }
  }

  private static void loginRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("=== ВХОД В СИСТЕМУ ===");

    System.out.print("Email: ");
    String email = scanner.nextLine();

    System.out.print("Пароль: ");
    String password = scanner.nextLine();

    Restaurant restaurant = authService.login(email, password);

    if (restaurant != null) {
      currentRestaurant = restaurant;
      System.out.println("ВХОД ВЫПОЛНЕН УСПЕШНО!");
      System.out.println("Добро пожаловать, " + restaurant.getName());
      System.out.println("Статус ресторана: " + restaurant.getStatus());
    } else {
      System.out.println("ОШИБКА ВХОДА: Неверный email или пароль");
    }
  }

  private static void updateRestaurantData(RestaurantService restaurantService, Scanner scanner) {
    System.out.println("=== ИЗМЕНЕНИЕ ДАННЫХ РЕСТОРАНА ===");

    System.out.println("Текущие данные:");
    showRestaurantProfile(currentRestaurant);

    System.out.println("Введите новые данные (оставьте пустым чтобы не менять):");

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

    String newEmail = null;
    System.out.print("Хотите изменить email? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      System.out.print("Новый email: ");
      newEmail = scanner.nextLine();

      System.out.print("Текущий пароль для подтверждения: ");
      String currentPassword = scanner.nextLine();

      boolean emailUpdated = restaurantService.updateEmail(
          currentRestaurant.getId(), newEmail, currentPassword);

      if (emailUpdated) {
        System.out.println("Email успешно изменен, требуется повторная верификация");
      } else {
        System.out.println("Ошибка изменения email: неверный пароль");
      }
    }

    Restaurant updatedRestaurant = restaurantService.updateRestaurant(
        currentRestaurant.getId(),
        newName.isEmpty() ? null : newName,
        newPhone.isEmpty() ? null : newPhone,
        newAddress.isEmpty() ? null : newAddress,
        newCuisineType.isEmpty() ? null : newCuisineType,
        newDescription.isEmpty() ? null : newDescription
    );

    if (updatedRestaurant != null) {
      currentRestaurant = updatedRestaurant;
      System.out.println("ДАННЫЕ УСПЕШНО ОБНОВЛЕНЫ!");
      System.out.println("Статус: Изменения сохранены в системе");

      if (!newName.isEmpty() || !newAddress.isEmpty()) {
        System.out.println("Уведомление: Изменение критических данных требует модерации");
      }
    } else {
      System.out.println("Ошибка обновления данных");
    }
  }

  private static void showRestaurantProfile(Restaurant restaurant) {
    System.out.println("=== ПРОФИЛЬ РЕСТОРАНА ===");
    System.out.println("ID: " + restaurant.getId());
    System.out.println("Название: " + restaurant.getName());
    System.out.println("Email: " + restaurant.getEmail());
    System.out.println("Телефон: " + restaurant.getPhone());
    System.out.println("Адрес: " + restaurant.getAddress());
    System.out.println("Тип кухни: " + restaurant.getCuisineType());
    System.out.println("Описание: " + restaurant.getDescription());
    System.out.println("Статус: " + restaurant.getStatus());
    System.out.println("Дата регистрации: " + restaurant.getRegistrationDate());
    System.out.println("Email верифицирован: " + restaurant.getEmailVerified());
  }

  private static void logout() {
    currentRestaurant = null;
    System.out.println("Выход из системы...");
  }
}
