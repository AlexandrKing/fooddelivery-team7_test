package com.team7.restaurant;

import com.team7.restaurant.service.AuthService;
import com.team7.restaurant.service.RestaurantService;
import com.team7.restaurant.model.Restaurant;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    AuthService authService = new AuthService();
    RestaurantService restaurantService = new RestaurantService();

    System.out.println("=== ТЕСТИРОВАНИЕ МОДУЛЯ РЕСТОРАНА ===");

    System.out.println("1. ТЕСТОВЫЕ РЕСТОРАНЫ:");
    List<Restaurant> restaurants = AuthService.getAllRestaurants();
    restaurants.forEach(r -> {
      System.out.println(" - " + r.getName() + " | " + r.getEmail() + " | " + r.getStatus());
    });

    System.out.println("2. ТЕСТ РЕГИСТРАЦИИ:");
    Restaurant newRestaurant = authService.registerRestaurant(
        "Burger King", "burger@king.com", "king123",
        "+79161112233", "ул. Гагарина, 5", "FAST_FOOD"
    );
    System.out.println("Зарегистрирован: " + newRestaurant.getName() + " (ID: " + newRestaurant.getId() + ")");

    System.out.println("3. ТЕСТ ВХОДА:");
    Restaurant loggedIn = authService.login("mario@pizza.com", "password123");
    if (loggedIn != null) {
      System.out.println("Успешный вход: " + loggedIn.getName());
    } else {
      System.out.println("Ошибка входа");
    }

    System.out.println("4. ТЕСТ ИЗМЕНЕНИЯ ДАННЫХ:");
    Restaurant updated = restaurantService.updateRestaurant(
        1L, "Mario's Premium Pizza", "+79169990000",
        "ул. Пушкина, 10 (новый корпус)", "ITALIAN", "Лучшая пицца в городе!"
    );
    if (updated != null) {
      System.out.println("Обновлен: " + updated.getName() + " | " + updated.getPhone());
    }

    System.out.println("5. ТЕСТ СМЕНЫ ПАРОЛЯ:");
    Boolean passwordChanged = authService.changePassword(1L, "password123", "newPassword123");
    System.out.println("Пароль изменен: " + (passwordChanged ? "УСПЕХ" : "ОШИБКА"));

    System.out.println("6. ФИНАЛЬНЫЙ СПИСОК РЕСТОРАНОВ:");
    AuthService.getAllRestaurants().forEach(r -> {
      System.out.println(" - " + r.getName() + " | " + r.getPhone() + " | " + r.getCuisineType());
    });
  }
}
