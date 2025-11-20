package com.team7.restaurant.userstory;

import com.team7.restaurant.service.AuthService;
import com.team7.restaurant.service.RestaurantService;
import com.team7.restaurant.service.MenuService;
import com.team7.restaurant.model.Restaurant;
import com.team7.restaurant.model.Dish;
import com.team7.restaurant.model.MenuCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class RestaurantUserStories {
  private static Restaurant currentRestaurant = null;
  private static MenuService menuService = new MenuService();

  public static void main(String[] args) {
    AuthService authService = new AuthService();
    RestaurantService restaurantService = new RestaurantService();
    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    while (running) {
      System.out.println("\n=== Панель управления рестораном ===");

      if (currentRestaurant != null) {
        System.out.println("Текущий ресторан: " + currentRestaurant.getName());
        System.out.println("1. Показать профиль ресторана");
        System.out.println("2. Изменить данные ресторана");
        System.out.println("3. Управление меню");
        System.out.println("4. Управление блюдами");
        System.out.println("5. Выйти из системы");
        System.out.println("6. Выйти из программы");
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
          running = handleAuthMenu(choice, authService, restaurantService, scanner);
        }
      } catch (Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }

    scanner.close();
    System.out.println("Программа завершена.");
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
        manageMenu(scanner);
        break;
      case 4:
        manageDishes(scanner);
        break;
      case 5:
        logout(authService);
        break;
      case 6:
        System.out.println("Выход из программы...");
        System.exit(0);
        break;
      default:
        System.out.println("Неверный выбор!");
    }
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

  private static void manageMenu(Scanner scanner) {
    System.out.println("\n=== УПРАВЛЕНИЕ МЕНЮ ===");
    System.out.println("1. Показать все блюда");
    System.out.println("2. Показать доступные блюда");
    System.out.println("3. Показать категории меню");
    System.out.println("4. Добавить категорию меню");
    System.out.println("5. Назад");

    System.out.print("Выберите действие: ");
    int choice = scanner.nextInt();
    scanner.nextLine();

    switch (choice) {
      case 1:
        showAllDishes();
        break;
      case 2:
        showAvailableDishes();
        break;
      case 3:
        showMenuCategories();
        break;
      case 4:
        addMenuCategory(scanner);
        break;
      case 5:
        return;
      default:
        System.out.println("Неверный выбор!");
    }
  }

  private static void manageDishes(Scanner scanner) {
    System.out.println("\n=== УПРАВЛЕНИЕ БЛЮДАМИ ===");
    System.out.println("1. Добавить блюдо");
    System.out.println("2. Удалить блюдо");
    System.out.println("3. Изменить блюдо");
    System.out.println("4. Переключить доступность блюда");
    System.out.println("5. Назад");

    System.out.print("Выберите действие: ");
    int choice = scanner.nextInt();
    scanner.nextLine();

    switch (choice) {
      case 1:
        addDish(scanner);
        break;
      case 2:
        removeDish(scanner);
        break;
      case 3:
        updateDish(scanner);
        break;
      case 4:
        toggleDishAvailability(scanner);
        break;
      case 5:
        return;
      default:
        System.out.println("Неверный выбор!");
    }
  }

  private static void showAllDishes() {
    List<Dish> dishes = menuService.getMenuByRestaurantId(currentRestaurant.getId());
    System.out.println("\n=== ВСЕ БЛЮДА ===");
    if (dishes.isEmpty()) {
      System.out.println("Блюд нет в меню");
    } else {
      for (Dish dish : dishes) {
        System.out.println(dish.getId() + ". " + dish.getName() + " - " + dish.getPrice() + " руб. - " +
            (dish.getAvailable() ? "Доступно" : "Недоступно"));
      }
    }
  }

  private static void showAvailableDishes() {
    List<Dish> dishes = menuService.getAvailableDishes(currentRestaurant.getId());
    System.out.println("\n=== ДОСТУПНЫЕ БЛЮДА ===");
    if (dishes.isEmpty()) {
      System.out.println("Нет доступных блюд");
    } else {
      for (Dish dish : dishes) {
        System.out.println(dish.getId() + ". " + dish.getName() + " - " + dish.getPrice() + " руб.");
      }
    }
  }

  private static void showMenuCategories() {
    List<MenuCategory> categories = currentRestaurant.getMenuCategories();
    System.out.println("\n=== КАТЕГОРИИ МЕНЮ ===");
    if (categories.isEmpty()) {
      System.out.println("Категорий нет");
    } else {
      for (MenuCategory category : categories) {
        System.out.println(category.getName() + " - " + category.getDescription());
      }
    }
  }

  private static void addMenuCategory(Scanner scanner) {
    System.out.println("\n=== ДОБАВЛЕНИЕ КАТЕГОРИИ МЕНЮ ===");
    System.out.print("Название категории: ");
    String name = scanner.nextLine();
    System.out.print("Описание категории: ");
    String description = scanner.nextLine();

    MenuCategory category = new MenuCategory();
    category.setName(name);
    category.setDescription(description);
    currentRestaurant.getMenuCategories().add(category);
    System.out.println("Категория добавлена!");
  }

  private static void addDish(Scanner scanner) {
    System.out.println("\n=== ДОБАВЛЕНИЕ БЛЮДА ===");
    System.out.print("Название блюда: ");
    String name = scanner.nextLine();
    System.out.print("Описание блюда: ");
    String description = scanner.nextLine();
    System.out.print("Цена: ");
    double price = scanner.nextDouble();
    scanner.nextLine();

    Dish dish = new Dish();
    dish.setName(name);
    dish.setDescription(description);
    dish.setPrice(BigDecimal.valueOf(price));
    dish.setAvailable(true);

    Dish addedDish = menuService.addDishToMenu(currentRestaurant.getId(), dish);
    if (addedDish != null) {
      System.out.println("Блюдо успешно добавлено!");
    } else {
      System.out.println("Ошибка добавления блюда");
    }
  }

  private static void removeDish(Scanner scanner) {
    System.out.println("\n=== УДАЛЕНИЕ БЛЮДА ===");
    showAllDishes();
    System.out.print("Введите ID блюда для удаления: ");
    Long dishId = scanner.nextLong();
    scanner.nextLine();

    menuService.removeDishFromMenu(currentRestaurant.getId(), dishId);
    System.out.println("Блюдо удалено!");
  }

  private static void updateDish(Scanner scanner) {
    System.out.println("\n=== ИЗМЕНЕНИЕ БЛЮДА ===");
    showAllDishes();
    System.out.print("Введите ID блюда для изменения: ");
    Long dishId = scanner.nextLong();
    scanner.nextLine();
    System.out.print("Новое название: ");
    String newName = scanner.nextLine();
    System.out.print("Новое описание: ");
    String newDescription = scanner.nextLine();
    System.out.print("Новая цена: ");
    double newPrice = scanner.nextDouble();
    scanner.nextLine();

    Dish dish = new Dish();
    dish.setId(dishId);
    dish.setName(newName);
    dish.setDescription(newDescription);
    dish.setPrice(BigDecimal.valueOf(newPrice));

    menuService.updateDish(currentRestaurant.getId(), dish);
    System.out.println("Блюдо обновлено!");
  }

  private static void toggleDishAvailability(Scanner scanner) {
    System.out.println("\n=== ИЗМЕНЕНИЕ ДОСТУПНОСТИ БЛЮДА ===");
    showAllDishes();
    System.out.print("Введите ID блюда: ");
    Long dishId = scanner.nextLong();
    scanner.nextLine();

    menuService.toggleDishAvailability(currentRestaurant.getId(), dishId);
    System.out.println("Доступность блюда изменена!");
  }

  private static void registerRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n=== РЕГИСТРАЦИЯ РЕСТОРАНА ===");
    System.out.print("Название ресторана: ");
    String name = scanner.nextLine();

    String email;
    while (true) {
      System.out.print("Email: ");
      email = scanner.nextLine();
      if (isValidEmail(email)) {
        break;
      } else {
        System.out.println("Ошибка: Неверный формат email! Пример: restaurant@mail.com");
      }
    }

    String password;
    while (true) {
      System.out.print("Пароль (мин. 6 символов): ");
      password = scanner.nextLine();
      if (password.length() >= 6) {
        break;
      } else {
        System.out.println("Ошибка: Пароль должен быть не менее 6 символов!");
      }
    }

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
      System.out.println("\nРЕГИСТРАЦИЯ УСПЕШНА!");
      System.out.println("ID ресторана: " + restaurant.getId());
      System.out.println("Статус: " + restaurant.getStatus());
      System.out.println("Сообщение: Ресторан отправлен на модерацию");
      currentRestaurant = restaurant;
    } catch (IllegalArgumentException e) {
      System.out.println("Ошибка регистрации: " + e.getMessage());
    }
  }

  private static boolean isValidEmail(String email) {
    return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
  }

  private static void loginRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n=== ВХОД В СИСТЕМУ ===");
    System.out.print("Email: ");
    String email = scanner.nextLine();
    System.out.print("Пароль: ");
    String password = scanner.nextLine();

    Restaurant restaurant = authService.login(email, password);
    if (restaurant != null) {
      currentRestaurant = authService.getCurrentRestaurant();
      System.out.println("\nВХОД ВЫПОЛНЕН УСПЕШНО!");
      System.out.println("Добро пожаловать, " + restaurant.getName());
      System.out.println("Статус ресторана: " + restaurant.getStatus());
    } else {
      System.out.println("ОШИБКА ВХОДА: Неверный email или пароль");
    }
  }

  private static void updateRestaurantData(RestaurantService restaurantService, Scanner scanner) {
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
        currentRestaurant = updatedRestaurant;
        System.out.println("\nДАННЫЕ УСПЕШНО ОБНОВЛЕНЫ!");
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
    authService.logout();
    currentRestaurant = null;
    System.out.println("Выход из системы...");
    System.out.println("Вы успешно вышли из системы.");
  }
}
