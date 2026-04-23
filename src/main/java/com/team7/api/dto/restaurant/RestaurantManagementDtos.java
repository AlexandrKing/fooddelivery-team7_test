package com.team7.api.dto.restaurant;

import java.time.LocalDateTime;

public final class RestaurantManagementDtos {
  private RestaurantManagementDtos() {
  }

  public record RestaurantOrderResponse(
      Long id,
      Long userId,
      Long restaurantId,
      String status,
      Double totalAmount,
      LocalDateTime createdAt
  ) {
  }

  public record UpdateOrderStatusRequest(String status) {
  }

  public record UpsertDishRequest(
      String name,
      String description,
      Double price,
      Boolean available,
      String category,
      Integer calories,
      String imageUrl,
      Integer preparationTimeMin
  ) {
  }

  public record RestaurantDishResponse(
      Long id,
      Long restaurantId,
      String name,
      String description,
      Double price,
      Boolean available,
      String category,
      Integer calories,
      String imageUrl,
      Integer preparationTimeMin
  ) {
  }
}

