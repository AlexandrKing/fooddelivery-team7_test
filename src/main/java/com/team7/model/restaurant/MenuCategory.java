package com.team7.model.restaurant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory {
  private Long id;
  private String name;
  private String description;
  private Long restaurantId;
  private LocalDateTime createdAt;
  private List<Dish> dishes = new ArrayList<>();

  public MenuCategory(Long id, String name, String description, Long restaurantId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.restaurantId = restaurantId;
    this.createdAt = LocalDateTime.now();
    this.dishes = new ArrayList<>();
  }

  public void addDish(Dish dish) {
    this.dishes.add(dish);
  }

  public void removeDish(Long dishId) {
    this.dishes.removeIf(dish -> dish.getId().equals(dishId));
  }
}