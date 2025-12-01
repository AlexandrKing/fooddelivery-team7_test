package com.team7.userstory.restaurant;

import com.team7.service.restaurant.AuthService;
import com.team7.service.restaurant.RestaurantService;
import com.team7.service.restaurant.MenuService;
import com.team7.model.restaurant.Restaurant;
import com.team7.model.restaurant.Dish;
import com.team7.model.restaurant.MenuCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class RestaurantUserStories {
  private static Restaurant currentRestaurant = null;
  private static MenuService menuService = new MenuService();

  // Класс для безопасного ввода чисел
  private static class InputUtils {
    public static BigDecimal readBigDecimal(Scanner scanner, String prompt) {
      while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
          BigDecimal value = new BigDecimal(input);

          if (value.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("❌ Ошибка: Цена должна быть больше 0!");
            continue;
          }

          return value;

        } catch (NumberFormatException e) {
          System.out.println("❌ Ошибка: Введите корректное число! Пример: 199.99 или 250");
        }
      }
    }

    public static long readLong(Scanner scanner, String prompt) {
      while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
          return Long.parseLong(input);
        } catch (NumberFormatException e) {
          System.out.println("❌ Ошибка: Введите целое число!");
        }
      }
    }

    public static int readInt(Scanner scanner, String prompt) {
      while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
          int value = Integer.parseInt(input);
          if (value < 0) {
            System.out.println("❌ Ошибка: Число не может быть отрицательным!");
            continue;
          }
          return value;
        } catch (NumberFormatException e) {
          System.out.println("❌ Ошибка: Введите целое число!");
        }
      }
    }
  }

  public static void main(String[] args) {
    AuthService authService = new AuthService();
    RestaurantService restaurantService = new RestaurantService();
    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    System.out.println("=".repeat(50));
    System.out.println("        🍽️  СИСТЕМА УПРАВЛЕНИЯ РЕСТОРАНОМ        ");
    System.out.println("=".repeat(50));

    while (running) {
      System.out.println("\n" + "━".repeat(40));

      if (currentRestaurant != null) {
        System.out.println("🏠 Текущий ресторан: " + currentRestaurant.getName());
        System.out.println("1. 📋 Показать профиль ресторана");
        System.out.println("2. ✏️  Изменить данные ресторана");
        System.out.println("3. 📊 Управление меню");
        System.out.println("4. 🍲 Управление блюдами");
        System.out.println("5. 🔐 Сменить пароль");
        System.out.println("6. 🚪 Выйти из системы");
        System.out.println("7. ❌ Выйти из программы");
      } else {
        System.out.println("1. 📝 Зарегистрировать ресторан");
        System.out.println("2. 🔑 Войти в систему");
        System.out.println("3. 🔓 Сбросить пароль");
        System.out.println("4. ❌ Выйти из программы");
      }

      System.out.println("━".repeat(40));

      // Безопасный ввод выбора меню
      int choice;
      if (currentRestaurant != null) {
        choice = InputUtils.readInt(scanner, "Выберите действие (1-7): ");
      } else {
        choice = InputUtils.readInt(scanner, "Выберите действие (1-4): ");
      }

      try {
        if (currentRestaurant != null) {
          handleRestaurantMenu(choice, authService, restaurantService, scanner);
        } else {
          running = handleAuthMenu(choice, authService, restaurantService, scanner);
        }
      } catch (Exception e) {
        System.out.println("❌ Ошибка: " + e.getMessage());
      }
    }

    scanner.close();
    System.out.println("\n👋 Программа завершена.");
  }

  private static void handleRestaurantMenu(int choice, AuthService authService,
                                           RestaurantService restaurantService,
                                           Scanner scanner) {
    switch (choice) {
      case 1:
        showRestaurantProfile(currentRestaurant);
        break;
      case 2:
        updateRestaurantData(authService, restaurantService, scanner);
        break;
      case 3:
        manageMenu(scanner);
        break;
      case 4:
        manageDishes(scanner);
        break;
      case 5:
        changePassword(authService, scanner);
        break;
      case 6:
        logout(authService);
        break;
      case 7:
        System.out.println("👋 Выход из программы...");
        System.exit(0);
        break;
      default:
        System.out.println("❌ Неверный выбор!");
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
        resetPassword(authService, scanner);
        break;
      case 4:
        System.out.println("👋 Выход из программы...");
        return false;
      default:
        System.out.println("❌ Неверный выбор!");
    }
    return true;
  }

  private static void registerRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        📝 РЕГИСТРАЦИЯ РЕСТОРАНА        ");
    System.out.println("=".repeat(40));

    // 1. Проверка названия
    String name;
    while (true) {
      System.out.print("Название ресторана: ");
      name = scanner.nextLine().trim();

      if (name.isEmpty()) {
        System.out.println("❌ Ошибка: Название не может быть пустым!");
        continue;
      }

      // Проверка на уникальность названия
      if (authService.isRestaurantNameExists(name)) {
        System.out.println("❌ Ошибка: Ресторан с таким названием уже зарегистрирован!");

        // Показываем похожие названия для подсказки
        List<String> similarNames = authService.findSimilarRestaurantNames(name);
        if (!similarNames.isEmpty()) {
          System.out.println("   📌 Похожие названия:");
          for (String similar : similarNames) {
            System.out.println("   - " + similar);
          }
        }

        System.out.println("   💡 Используйте другое название или добавьте уточнение");
        continue;
      }
      break;
    }

    // 2. Проверка email
    String email;
    while (true) {
      System.out.print("Email: ");
      email = scanner.nextLine().trim();

      if (!isValidEmail(email)) {
        System.out.println("❌ Ошибка: Неверный формат email! Пример: restaurant@mail.com");
        continue;
      }

      if (authService.isEmailExists(email)) {
        System.out.println("❌ Ошибка: Этот email уже зарегистрирован!");
        continue;
      }
      break;
    }

    // 3. Пароль
    String password;
    while (true) {
      System.out.print("Пароль (мин. 6 символов): ");
      password = scanner.nextLine();

      if (password.length() < 6) {
        System.out.println("❌ Ошибка: Пароль должен быть не менее 6 символов!");
        continue;
      }
      break;
    }

    // Подтверждение пароля
    System.out.print("Подтверждение пароля: ");
    String confirmPassword = scanner.nextLine();

    if (!password.equals(confirmPassword)) {
      System.out.println("❌ Ошибка: Пароли не совпадают!");
      return;
    }

    // 4. Проверка телефона
    String phone;
    while (true) {
      System.out.print("Телефон (формат: +7XXXXXXXXXX или 8XXXXXXXXXX): ");
      phone = scanner.nextLine().trim();

      if (!isValidPhone(phone)) {
        System.out.println("❌ Ошибка: Неверный формат телефона! Пример: +79161234567 или 89161234567");
        continue;
      }

      if (authService.isPhoneExists(phone)) {
        System.out.println("❌ Ошибка: Этот телефон уже зарегистрирован!");
        continue;
      }
      break;
    }

    // 5. Проверка адреса
    String address;
    while (true) {
      System.out.print("Адрес: ");
      address = scanner.nextLine().trim();

      if (address.isEmpty()) {
        System.out.println("❌ Ошибка: Адрес не может быть пустым!");
        continue;
      }

      if (authService.isRestaurantAddressExists(address)) {
        System.out.println("⚠️  Предупреждение: По этому адресу уже зарегистрирован ресторан!");
        System.out.println("   💡 Уточните адрес (этаж, вход, корпус и т.д.)");

        System.out.print("   Вы уверены, что хотите использовать этот адрес? (да/нет): ");
        String confirm = scanner.nextLine().toLowerCase();

        if (!confirm.equals("да") && !confirm.equals("yes")) {
          continue;
        }
      }
      break;
    }

    // 6. Тип кухни
    System.out.print("Тип кухни: ");
    String cuisineType = scanner.nextLine().trim();

    if (cuisineType.isEmpty()) {
      cuisineType = "Не указан";
    }

    try {
      System.out.println("\n⏳ Регистрация ресторана...");

      Restaurant restaurant = authService.registerRestaurant(
          name, email, password, phone, address, cuisineType
      );

      if (restaurant != null) {
        System.out.println("\n" + "✅".repeat(20));
        System.out.println("        🎉 РЕГИСТРАЦИЯ УСПЕШНА!        ");
        System.out.println("✅".repeat(20));
        System.out.println("   📍 ID ресторана: " + restaurant.getId());
        System.out.println("   🏠 Название: " + restaurant.getName());
        System.out.println("   📍 Адрес: " + restaurant.getAddress());
        System.out.println("   👨‍🍳 Тип кухни: " + restaurant.getCuisineType());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📋 Статус: " + restaurant.getStatus());
        System.out.println("   📧 Ресторан отправлен на модерацию");
        System.out.println("   ⏰ Мы свяжемся с вами после проверки");

        currentRestaurant = restaurant;

        // Предложение сразу добавить меню
        System.out.println("\n🎯 Хотите сразу добавить меню?");
        System.out.print("   (да - добавить меню, нет - перейти в панель): ");
        String addMenuChoice = scanner.nextLine().toLowerCase();

        if (addMenuChoice.equals("да") || addMenuChoice.equals("yes")) {
          manageMenu(scanner);
        }
      } else {
        System.out.println("❌ Ошибка: Не удалось зарегистрировать ресторан");
      }

    } catch (IllegalArgumentException e) {
      System.out.println("❌ Ошибка регистрации: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("❌ Неожиданная ошибка: " + e.getMessage());
    }
  }

  private static void updateRestaurantData(AuthService authService, RestaurantService restaurantService, Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        ✏️  ИЗМЕНЕНИЕ ДАННЫХ РЕСТОРАНА        ");
    System.out.println("=".repeat(40));
    System.out.println("📋 Текущие данные:");
    showRestaurantProfile(currentRestaurant);
    System.out.println("\n💡 Введите новые данные (оставьте пустым чтобы не менять):");

    // Название
    String newName = null;
    while (true) {
      System.out.print("Новое название: ");
      newName = scanner.nextLine().trim();

      if (newName.isEmpty()) {
        newName = null;
        break; // Не меняем
      }

      // Проверяем, не изменилось ли название
      if (!newName.equals(currentRestaurant.getName())) {
        // Проверяем уникальность нового названия
        if (authService.isRestaurantNameExists(newName)) {
          System.out.println("❌ Ошибка: Ресторан с таким названием уже существует!");
          continue;
        }
      }
      break;
    }

    // Телефон
    String newPhone = null;
    while (true) {
      System.out.print("Новый телефон: ");
      newPhone = scanner.nextLine().trim();

      if (newPhone.isEmpty()) {
        newPhone = null;
        break;
      }

      if (!isValidPhone(newPhone)) {
        System.out.println("❌ Ошибка: Неверный формат телефона! Пример: +79161234567 или 89161234567");
        continue;
      }

      // Проверяем, не изменился ли телефон
      if (!newPhone.equals(currentRestaurant.getPhone())) {
        if (authService.isPhoneExists(newPhone)) {
          System.out.println("❌ Ошибка: Этот телефон уже используется другим рестораном!");
          continue;
        }
      }
      break;
    }

    // Адрес
    String newAddress = null;
    while (true) {
      System.out.print("Новый адрес: ");
      newAddress = scanner.nextLine().trim();

      if (newAddress.isEmpty()) {
        newAddress = null;
        break;
      }

      // Проверяем, не изменился ли адрес
      if (!newAddress.equals(currentRestaurant.getAddress())) {
        if (authService.isRestaurantAddressExists(newAddress)) {
          System.out.println("⚠️  Предупреждение: По этому адресу уже зарегистрирован ресторан!");
          System.out.print("   Вы уверены, что хотите изменить адрес? (да/нет): ");
          String confirm = scanner.nextLine().toLowerCase();
          if (!confirm.equals("да") && !confirm.equals("yes")) {
            continue;
          }
        }
      }
      break;
    }

    System.out.print("Новый тип кухни: ");
    String newCuisineType = scanner.nextLine().trim();
    if (newCuisineType.isEmpty()) newCuisineType = null;

    System.out.print("Новое описание: ");
    String newDescription = scanner.nextLine().trim();
    if (newDescription.isEmpty()) newDescription = null;

    try {
      System.out.println("\n⏳ Обновление данных...");

      Restaurant updatedRestaurant = restaurantService.updateRestaurant(
          currentRestaurant.getId(),
          newName,
          newPhone,
          newAddress,
          newCuisineType,
          newDescription
      );

      if (updatedRestaurant != null) {
        currentRestaurant = updatedRestaurant;
        System.out.println("\n" + "✅".repeat(20));
        System.out.println("        📝 ДАННЫЕ УСПЕШНО ОБНОВЛЕНЫ!        ");
        System.out.println("✅".repeat(20));

        // Проверяем, изменились ли критические данные
        boolean nameChanged = newName != null && !newName.equals(currentRestaurant.getName());
        boolean addressChanged = newAddress != null && !newAddress.equals(currentRestaurant.getAddress());

        if (nameChanged || addressChanged) {
          System.out.println("📋 Изменение названия или адреса требует модерации");
          System.out.println("   ⏰ Статус будет обновлен после проверки администратором");
        }
      } else {
        System.out.println("❌ Ошибка обновления данных");
      }
    } catch (Exception e) {
      System.out.println("❌ Ошибка при обновлении данных: " + e.getMessage());
    }
  }

  private static void manageMenu(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        📊 УПРАВЛЕНИЕ МЕНЮ        ");
    System.out.println("=".repeat(40));
    System.out.println("1. 📋 Показать все блюда");
    System.out.println("2. ✅ Показать доступные блюда");
    System.out.println("3. 📁 Показать категории меню");
    System.out.println("4. ➕ Добавить категорию меню");
    System.out.println("5. ↩️  Назад");

    int choice = InputUtils.readInt(scanner, "Выберите действие (1-5): ");

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
        System.out.println("❌ Неверный выбор!");
    }
  }

  private static void manageDishes(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        🍲 УПРАВЛЕНИЕ БЛЮДАМИ        ");
    System.out.println("=".repeat(40));
    System.out.println("1. ➕ Добавить блюдо");
    System.out.println("2. ❌ Удалить блюдо");
    System.out.println("3. ✏️  Изменить блюдо");
    System.out.println("4. 🔄 Переключить доступность блюда");
    System.out.println("5. ↩️  Назад");

    int choice = InputUtils.readInt(scanner, "Выберите действие (1-5): ");

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
        System.out.println("❌ Неверный выбор!");
    }
  }

  private static void showAllDishes() {
    List<Dish> dishes = menuService.getMenuByRestaurantId(currentRestaurant.getId());
    System.out.println("\n" + "━".repeat(50));
    System.out.println("        📋 ВСЕ БЛЮДА (" + dishes.size() + ")        ");
    System.out.println("━".repeat(50));
    if (dishes.isEmpty()) {
      System.out.println("😔 Блюд нет в меню");
    } else {
      int index = 1;
      for (Dish dish : dishes) {
        String status = dish.getAvailable() ? "✅ Доступно" : "⛔ Недоступно";
        System.out.printf("%2d. %-30s - %8.2f руб. - %s%n",
            index++,
            dish.getName(),
            dish.getPrice().doubleValue(),
            status);
      }
    }
    System.out.println("━".repeat(50));
  }

  private static void showAvailableDishes() {
    List<Dish> dishes = menuService.getAvailableDishes(currentRestaurant.getId());
    System.out.println("\n" + "━".repeat(50));
    System.out.println("        ✅ ДОСТУПНЫЕ БЛЮДА (" + dishes.size() + ")        ");
    System.out.println("━".repeat(50));
    if (dishes.isEmpty()) {
      System.out.println("😔 Нет доступных блюд");
    } else {
      int index = 1;
      for (Dish dish : dishes) {
        System.out.printf("%2d. %-30s - %8.2f руб.%n",
            index++,
            dish.getName(),
            dish.getPrice().doubleValue());
      }
    }
    System.out.println("━".repeat(50));
  }

  private static void showMenuCategories() {
    List<MenuCategory> categories = currentRestaurant.getMenuCategories();
    System.out.println("\n" + "━".repeat(50));
    System.out.println("        📁 КАТЕГОРИИ МЕНЮ (" + categories.size() + ")        ");
    System.out.println("━".repeat(50));
    if (categories.isEmpty()) {
      System.out.println("😔 Категорий нет");
    } else {
      int index = 1;
      for (MenuCategory category : categories) {
        System.out.printf("%2d. %-20s - %s%n",
            index++,
            category.getName(),
            category.getDescription());
      }
    }
    System.out.println("━".repeat(50));
  }

  private static void addMenuCategory(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        ➕ ДОБАВЛЕНИЕ КАТЕГОРИИ МЕНЮ        ");
    System.out.println("=".repeat(40));
    System.out.print("Название категории: ");
    String name = scanner.nextLine().trim();

    if (name.isEmpty()) {
      System.out.println("❌ Ошибка: Название не может быть пустым!");
      return;
    }

    System.out.print("Описание категории: ");
    String description = scanner.nextLine().trim();

    // Здесь нужно вызвать метод сервиса для добавления категории
    // Пока просто добавляем в список ресторана
    MenuCategory category = new MenuCategory();
    category.setName(name);
    category.setDescription(description);
    currentRestaurant.getMenuCategories().add(category);
    System.out.println("✅ Категория \"" + name + "\" добавлена!");
  }

  private static void addDish(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        ➕ ДОБАВЛЕНИЕ БЛЮДА        ");
    System.out.println("=".repeat(40));
    System.out.print("Название блюда: ");
    String name = scanner.nextLine().trim();

    if (name.isEmpty()) {
      System.out.println("❌ Ошибка: Название не может быть пустым!");
      return;
    }

    System.out.print("Описание блюда: ");
    String description = scanner.nextLine().trim();

    BigDecimal price = InputUtils.readBigDecimal(scanner, "Цена (руб.): ");

    Dish dish = new Dish();
    dish.setName(name);
    dish.setDescription(description);
    dish.setPrice(price);
    dish.setAvailable(true);

    Dish addedDish = menuService.addDishToMenu(currentRestaurant.getId(), dish);
    if (addedDish != null) {
      System.out.println("✅ Блюдо \"" + name + "\" успешно добавлено!");
    } else {
      System.out.println("❌ Ошибка добавления блюда");
    }
  }

  private static void removeDish(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        ❌ УДАЛЕНИЕ БЛЮДА        ");
    System.out.println("=".repeat(40));
    showAllDishes();

    List<Dish> dishes = menuService.getMenuByRestaurantId(currentRestaurant.getId());
    if (dishes.isEmpty()) {
      System.out.println("😔 Нет блюд для удаления");
      return;
    }

    long dishId = InputUtils.readLong(scanner, "Введите ID блюда для удаления: ");

    // Проверяем, существует ли такое блюдо
    boolean dishExists = dishes.stream().anyMatch(d -> d.getId() == dishId);
    if (!dishExists) {
      System.out.println("❌ Ошибка: Блюдо с ID " + dishId + " не найдено!");
      return;
    }

    System.out.print("Вы уверены, что хотите удалить это блюдо? (да/нет): ");
    String confirm = scanner.nextLine().toLowerCase();

    if (confirm.equals("да") || confirm.equals("yes")) {
      boolean removed = menuService.removeDishFromMenu(currentRestaurant.getId(), dishId);
      if (removed) {
        System.out.println("✅ Блюдо успешно удалено!");
      } else {
        System.out.println("❌ Ошибка удаления блюда");
      }
    } else {
      System.out.println("❌ Удаление отменено");
    }
  }

  private static void updateDish(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        ✏️  ИЗМЕНЕНИЕ БЛЮДА        ");
    System.out.println("=".repeat(40));
    showAllDishes();

    List<Dish> dishes = menuService.getMenuByRestaurantId(currentRestaurant.getId());
    if (dishes.isEmpty()) {
      System.out.println("😔 Нет блюд для изменения");
      return;
    }

    long dishId = InputUtils.readLong(scanner, "Введите ID блюда для изменения: ");

    // Проверяем, существует ли такое блюдо
    boolean dishExists = dishes.stream().anyMatch(d -> d.getId() == dishId);
    if (!dishExists) {
      System.out.println("❌ Ошибка: Блюдо с ID " + dishId + " не найдено!");
      return;
    }

    System.out.print("Новое название (оставьте пустым чтобы не менять): ");
    String newName = scanner.nextLine().trim();

    System.out.print("Новое описание (оставьте пустым чтобы не менять): ");
    String newDescription = scanner.nextLine().trim();

    System.out.print("Новая цена (оставьте пустым чтобы не менять): ");
    String priceInput = scanner.nextLine().trim();
    BigDecimal newPrice = null;
    if (!priceInput.isEmpty()) {
      newPrice = InputUtils.readBigDecimal(scanner, ""); // Переиспользуем метод
    }

    Dish dish = new Dish();
    dish.setId(dishId);
    if (!newName.isEmpty()) dish.setName(newName);
    if (!newDescription.isEmpty()) dish.setDescription(newDescription);
    if (newPrice != null) dish.setPrice(newPrice);

    boolean updated = menuService.updateDish(currentRestaurant.getId(), dish);
    if (updated) {
      System.out.println("✅ Блюдо успешно обновлено!");
    } else {
      System.out.println("❌ Ошибка обновления блюда");
    }
  }

  private static void toggleDishAvailability(Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        🔄 ИЗМЕНЕНИЕ ДОСТУПНОСТИ БЛЮДА        ");
    System.out.println("=".repeat(40));
    showAllDishes();

    List<Dish> dishes = menuService.getMenuByRestaurantId(currentRestaurant.getId());
    if (dishes.isEmpty()) {
      System.out.println("😔 Нет блюд для изменения");
      return;
    }

    long dishId = InputUtils.readLong(scanner, "Введите ID блюда: ");

    // Проверяем, существует ли такое блюдо
    boolean dishExists = dishes.stream().anyMatch(d -> d.getId() == dishId);
    if (!dishExists) {
      System.out.println("❌ Ошибка: Блюдо с ID " + dishId + " не найдено!");
      return;
    }

    boolean toggled;
    toggled = menuService.toggleDishAvailability(currentRestaurant.getId(), dishId);
    if (toggled) {
      System.out.println("✅ Доступность блюда успешно изменена!");
    } else {
      System.out.println("❌ Ошибка изменения доступности блюда");
    }
  }

  private static void loginRestaurant(AuthService authService, Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        🔑 ВХОД В СИСТЕМУ        ");
    System.out.println("=".repeat(40));
    System.out.print("Email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Пароль: ");
    String password = scanner.nextLine();

    Restaurant restaurant = authService.login(email, password);
    if (restaurant != null) {
      currentRestaurant = authService.getCurrentRestaurant();
      System.out.println("\n" + "✅".repeat(20));
      System.out.println("        🎉 ВХОД ВЫПОЛНЕН УСПЕШНО!        ");
      System.out.println("✅".repeat(20));
      System.out.println("👋 Добро пожаловать, " + restaurant.getName() + "!");
      System.out.println("📋 Статус ресторана: " + restaurant.getStatus());
    } else {
      System.out.println("❌ ОШИБКА ВХОДА: Неверный email или пароль");
    }
  }

  private static void changePassword(AuthService authService, Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        🔐 СМЕНА ПАРОЛЯ        ");
    System.out.println("=".repeat(40));

    System.out.print("Текущий пароль: ");
    String currentPassword = scanner.nextLine();

    String newPassword;
    while (true) {
      System.out.print("Новый пароль (мин. 6 символов): ");
      newPassword = scanner.nextLine();

      if (newPassword.length() < 6) {
        System.out.println("❌ Ошибка: Пароль должен быть не менее 6 символов!");
        continue;
      }
      break;
    }

    System.out.print("Подтверждение нового пароля: ");
    String confirmPassword = scanner.nextLine();

    if (!newPassword.equals(confirmPassword)) {
      System.out.println("❌ Ошибка: Пароли не совпадают!");
      return;
    }

    boolean changed = authService.changePassword(currentRestaurant.getId(), currentPassword, newPassword);
    if (changed) {
      System.out.println("✅ Пароль успешно изменен!");
    } else {
      System.out.println("❌ Ошибка: Неверный текущий пароль");
    }
  }

  private static void resetPassword(AuthService authService, Scanner scanner) {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("        🔓 СБРОС ПАРОЛЯ        ");
    System.out.println("=".repeat(40));

    System.out.print("Введите ваш email: ");
    String email = scanner.nextLine().trim();

    authService.resetPassword(email);
  }

  private static void showRestaurantProfile(Restaurant restaurant) {
    System.out.println("\n" + "━".repeat(50));
    System.out.println("        📋 ПРОФИЛЬ РЕСТОРАНА        ");
    System.out.println("━".repeat(50));
    System.out.println("📍 ID: " + restaurant.getId());
    System.out.println("🏠 Название: " + restaurant.getName());
    System.out.println("📧 Email: " + restaurant.getEmail());
    System.out.println("📱 Телефон: " + restaurant.getPhone());
    System.out.println("📍 Адрес: " + restaurant.getAddress());
    System.out.println("👨‍🍳 Тип кухни: " + restaurant.getCuisineType());
    System.out.println("📝 Описание: " +
        (restaurant.getDescription() != null && !restaurant.getDescription().isEmpty()
            ? restaurant.getDescription()
            : "Не указано"));
    System.out.println("📋 Статус: " + restaurant.getStatus());
    System.out.println("📅 Дата регистрации: " + restaurant.getRegistrationDate());
    if (restaurant.getLastLoginDate() != null) {
      System.out.println("⏰ Последний вход: " + restaurant.getLastLoginDate());
    }
    System.out.println("✅ Email верифицирован: " + (restaurant.getEmailVerified() ? "Да" : "Нет"));
    System.out.println("━".repeat(50));
  }

  private static void logout(AuthService authService) {
    authService.logout();
    currentRestaurant = null;
    System.out.println("\n👋 Выход из системы...");
    System.out.println("✅ Вы успешно вышли из системы.");
  }

  private static boolean isValidEmail(String email) {
    return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
  }

  private static boolean isValidPhone(String phone) {
    return phone != null && phone.matches("^(\\+7|8)\\d{10}$");
  }
}