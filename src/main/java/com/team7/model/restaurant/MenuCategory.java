package com.team7.model.restaurant;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory {
  private Long id;
  private String name;
  private String description;
  private List<Dish> dishes = new ArrayList<>();
  private Long restaurantId;

  public MenuCategory(Long id, String name, String description, Long restaurantId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.restaurantId = restaurantId;
    this.dishes = new ArrayList<>();
  }

  public void addDish(Dish dish) {
    this.dishes.add(dish);
  }

  public void removeDish(Long dishId) {
    this.dishes.removeIf(dish -> dish.getId().equals(dishId));
  }
}
