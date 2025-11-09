package com.team7.restaurant.api;

import com.team7.restaurant.model.Dish;
import java.util.List;

public interface MenuOperations {
  Dish addDishToMenu(Long restaurantId, Dish dish);
  void removeDishFromMenu(Long restaurantId, Long dishId);
  void updateDish(Long restaurantId, Dish dish);
  void toggleDishAvailability(Long restaurantId, Long dishId);
  List<Dish> getMenuByRestaurantId(Long restaurantId);
  List<Dish> getAvailableDishes(Long restaurantId);
}
