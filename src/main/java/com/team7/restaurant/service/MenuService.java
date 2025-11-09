package com.team7.restaurant.service;

import com.team7.restaurant.api.MenuOperations;
import com.team7.restaurant.model.Dish;
import com.team7.restaurant.model.MenuCategory;
import com.team7.restaurant.model.Restaurant;
import java.util.List;
import java.util.ArrayList;

public class MenuService implements MenuOperations {
  private static Long nextDishId = 1L;
  private static Long nextCategoryId = 1L;

  public MenuCategory createCategory(Long restaurantId, String name, String description) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      MenuCategory category = new MenuCategory(nextCategoryId++, name, description, restaurantId);
      restaurant.getMenuCategories().add(category);
      return category;
    }
    return null;
  }

  public void deleteCategory(Long restaurantId, Long categoryId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      restaurant.getMenuCategories().removeIf(category -> category.getId().equals(categoryId));
    }
  }

  public List<MenuCategory> getCategoriesByRestaurantId(Long restaurantId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    return restaurant != null ? restaurant.getMenuCategories() : new ArrayList<>();
  }

  public void addDishToCategory(Long restaurantId, Long categoryId, Dish dish) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      for (MenuCategory category : restaurant.getMenuCategories()) {
        if (category.getId().equals(categoryId)) {
          dish.setId(nextDishId++);
          dish.setRestaurantId(restaurantId);
          category.addDish(dish);
          restaurant.getMenu().add(dish);
          break;
        }
      }
    }
  }
  @Override
  public Dish addDishToMenu(Long restaurantId, Dish dish) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      dish.setId(nextDishId++);
      dish.setRestaurantId(restaurantId);
      restaurant.getMenu().add(dish);
      return dish;
    }
    return null;
  }

  @Override
  public void removeDishFromMenu(Long restaurantId, Long dishId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      restaurant.getMenu().removeIf(dish -> dish.getId().equals(dishId));
      // Также удаляем из всех категорий
      for (MenuCategory category : restaurant.getMenuCategories()) {
        category.removeDish(dishId);
      }
    }
  }

  @Override
  public void updateDish(Long restaurantId, Dish updatedDish) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      for (Dish dish : restaurant.getMenu()) {
        if (dish.getId().equals(updatedDish.getId())) {
          dish.setName(updatedDish.getName());
          dish.setDescription(updatedDish.getDescription());
          dish.setPrice(updatedDish.getPrice());
          dish.setCategory(updatedDish.getCategory());
          break;
        }
      }
    }
  }

  @Override
  public void toggleDishAvailability(Long restaurantId, Long dishId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      for (Dish dish : restaurant.getMenu()) {
        if (dish.getId().equals(dishId)) {
          dish.setAvailable(!dish.getAvailable());
          break;
        }
      }
    }
  }

  @Override
  public List<Dish> getMenuByRestaurantId(Long restaurantId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    return restaurant != null ? restaurant.getMenu() : new ArrayList<>();
  }

  @Override
  public List<Dish> getAvailableDishes(Long restaurantId) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);
    if (restaurant != null) {
      List<Dish> availableDishes = new ArrayList<>();
      for (Dish dish : restaurant.getMenu()) {
        if (dish.getAvailable()) {
          availableDishes.add(dish);
        }
      }
      return availableDishes;
    }
    return new ArrayList<>();
  }
}
