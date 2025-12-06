package com.team7.model.restaurant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
  private Long id;
  private Long restaurantId;
  private String name;
  private String description;
  private BigDecimal price;
  private String category;
  private Boolean isAvailable;
  private Long menuCategoryId;
  private Integer availableQuantity;
  private Integer preparationTimeMin;
  private Integer calories;
  private Boolean isVegetarian;
  private Boolean isSpicy;
  private String imageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Для обратной совместимости с существующим кодом
  public Boolean getAvailable() {
    return isAvailable;
  }

  public Long getMenuCategoryId() {
    return menuCategoryId;
  }

  public void setMenuCategoryId(Long menuCategoryId) {
    this.menuCategoryId = menuCategoryId;
  }


  public void setAvailable(Boolean available) {
    this.isAvailable = available;
  }

  public Boolean getVegetarian() {
    return isVegetarian;
  }

  public void setVegetarian(Boolean vegetarian) {
    this.isVegetarian = vegetarian;
  }

  public Boolean getSpicy() {
    return isSpicy;
  }

  public void setSpicy(Boolean spicy) {
    this.isSpicy = spicy;
  }

  // Конструктор для обратной совместимости
  public Dish(Long id, String name, String description, BigDecimal price,
              String category, Long restaurantId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.category = category;
    this.restaurantId = restaurantId;
    this.isAvailable = true;
    this.preparationTimeMin = 15;
    this.isVegetarian = false;
    this.isSpicy = false;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // Дополнительные геттеры для удобства
  public Integer getPreparationTimeMin() {
    return preparationTimeMin != null ? preparationTimeMin : 15;
  }

  public void setPreparationTimeMin(Integer preparationTimeMin) {
    this.preparationTimeMin = preparationTimeMin != null ? preparationTimeMin : 15;
  }
}