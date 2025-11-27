package com.team7.restaurant.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private String category;
  private Boolean available;
  private Long restaurantId;

  public Dish(Long id, String name, String description, BigDecimal price,
              String category, Long restaurantId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.category = category;
    this.restaurantId = restaurantId;
    this.available = true;
  }
}
