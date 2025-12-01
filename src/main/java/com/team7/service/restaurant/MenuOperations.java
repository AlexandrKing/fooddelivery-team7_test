package com.team7.service.restaurant;

import com.team7.model.restaurant.Dish;
import java.util.List;

public interface MenuOperations {
  Dish addDishToMenu(Long restaurantId, Dish dish);
  boolean removeDishFromMenu(Long restaurantId, Long dishId);
  boolean updateDish(Long restaurantId, Dish dish);
  boolean toggleDishAvailability(Long restaurantId, Long dishId);
  List<Dish> getMenuByRestaurantId(Long restaurantId);
  List<Dish> getAvailableDishes(Long restaurantId);
}
