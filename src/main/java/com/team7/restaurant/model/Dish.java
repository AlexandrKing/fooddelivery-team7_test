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
  private double price;
  private String category;
  private Boolean available;
  private Long restaurantId;
}
