package com.team7.api.dto.restaurant;

public final class RestaurantDtos {
  private RestaurantDtos() {
  }

  public record RestaurantResponse(
      Long id,
      String name,
      String address,
      String cuisineType,
      Double rating,
      Integer deliveryTime,
      Double minOrderAmount,
      Boolean isActive
  ) {
  }

  public record MenuItemResponse(
      Long id,
      Long restaurantId,
      String name,
      String description,
      Double price,
      Boolean available,
      String category,
      Integer calories,
      Double weight,
      String imageUrl,
      Integer cookingTime
  ) {
  }
}

