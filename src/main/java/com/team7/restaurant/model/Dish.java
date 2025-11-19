package com.team7.restaurant.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
  private Long id;
  private String name;
  private String description;
  private double price;
  private String category;
  private Boolean available = true;
  private Long restaurantId;
}