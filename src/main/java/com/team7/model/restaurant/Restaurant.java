package com.team7.model.restaurant;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
  private LocalDateTime registrationDate;
  private LocalDateTime lastLoginDate;
  private Boolean emailVerified;
  private List<Dish> menu = new ArrayList<>();
  private List<MenuCategory> menuCategories = new ArrayList<>();

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
    this.registrationDate = LocalDateTime.now();
    this.emailVerified = false;
    this.menu = new ArrayList<>();
    this.menuCategories = new ArrayList<>();
  }
}
