package com.team7.model.restaurant;

import com.team7.service.restaurant.MenuService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
  private Long id;
  private String name;
  private String email;
  private String password;
  private String phone;
  private String address;
  private String cuisineType;
  private String description;
  private String status;
  private Boolean isActive;
  private Boolean emailVerified;
  private BigDecimal rating;
  private Integer deliveryTime;
  private BigDecimal minOrderAmount;
  private LocalDateTime registrationDate;
  private LocalDateTime lastLoginDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<Dish> menu = new ArrayList<>();
  private List<MenuCategory> menuCategories = new ArrayList<>();

  public List<MenuCategory> getMenuCategories() {
    return menuCategories;
  }
  public void setMenuCategories(List<MenuCategory> menuCategories) {
    this.menuCategories = menuCategories;
  }
  public void loadMenuCategories() {
    MenuService menuService = new MenuService();
    List<MenuCategory> categories = menuService.getMenuCategoriesByRestaurantId(this.id);
    this.menuCategories = categories;
  }

  public Restaurant(Long id, String name, String email, String password, String phone,
                    String address, String cuisineType) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.address = address;
    this.cuisineType = cuisineType;
    this.status = "PENDING";
    this.isActive = true;
    this.emailVerified = false;
    this.rating = BigDecimal.ZERO;
    this.deliveryTime = 30;
    this.minOrderAmount = BigDecimal.ZERO;
    this.registrationDate = LocalDateTime.now();
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.menu = new ArrayList<>();
    this.menuCategories = new ArrayList<>();
  }

  // Геттеры для совместимости с существующим кодом
  public String getCuisineType() {
    return cuisineType;
  }

  public void setCuisineType(String cuisineType) {
    this.cuisineType = cuisineType;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }
}