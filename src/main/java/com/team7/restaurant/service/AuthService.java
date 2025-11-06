package com.team7.restaurant.service;

import com.team7.restaurant.api.AuthOperations;
import com.team7.restaurant.model.Restaurant;
import java.util.*;

public class AuthService implements AuthOperations {
  private static Map<Long, Restaurant> restaurants = new HashMap<>();
  private static Long nextId = 1L;

  static {
    initializeTestData();
  }

  private static void initializeTestData() {
    Restaurant restaurant1 = new Restaurant(nextId++, "Mario's Pizza", "mario@pizza.com",
        "password123", "+79161234567", "ул. Пушкина, 10", "ITALIAN");
    restaurant1.setStatus("ACTIVE");
    restaurant1.setEmailVerified(true);
    restaurants.put(restaurant1.getId(), restaurant1);

    Restaurant restaurant2 = new Restaurant(nextId++, "Tokyo Sushi", "tokyo@sushi.com",
        "password456", "+79167654321", "ул. Лермонтова, 25", "JAPANESE");
    restaurant2.setStatus("ACTIVE");
    restaurant2.setEmailVerified(true);
    restaurants.put(restaurant2.getId(), restaurant2);

    Restaurant restaurant3 = new Restaurant(nextId++, "Бабушкина Кухня", "babushka@food.com",
        "password789", "+79169998877", "пр. Мира, 15", "RUSSIAN");
    restaurant3.setStatus("PENDING");
    restaurant3.setEmailVerified(false);
    restaurants.put(restaurant3.getId(), restaurant3);
  }

  @Override
  public Restaurant registerRestaurant(String name, String email, String password,
                                       String phone, String address, String cuisineType) {
    for (Restaurant restaurant : restaurants.values()) {
      if (restaurant.getEmail().equals(email)) {
        throw new IllegalArgumentException("Email уже используется");
      }
    }

    Restaurant restaurant = new Restaurant(nextId++, name, email, password, phone, address, cuisineType);
    restaurants.put(restaurant.getId(), restaurant);
    return restaurant;
  }

  @Override
  public Restaurant login(String email, String password) {
    return restaurants.values().stream()
        .filter(r -> r.getEmail().equals(email) && r.getPassword().equals(password))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Boolean changePassword(Long restaurantId, String currentPassword, String newPassword) {
    Restaurant restaurant = restaurants.get(restaurantId);
    if (restaurant != null && restaurant.getPassword().equals(currentPassword)) {
      restaurant.setPassword(newPassword);
      return true;
    }
    return false;
  }

  @Override
  public void resetPassword(String email) {
    System.out.println("Запрос на сброс пароля для: " + email);
  }

  public static List<Restaurant> getAllRestaurants() {
    return new ArrayList<>(restaurants.values());
  }

  public static Restaurant getRestaurantById(Long id) {
    return restaurants.get(id);
  }
}
