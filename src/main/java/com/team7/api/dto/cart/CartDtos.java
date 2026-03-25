package com.team7.api.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class CartDtos {
  private CartDtos() {
  }

  public record AddItemRequest(
      @NotNull(message = "RestaurantId is required")
      @Min(value = 1, message = "RestaurantId must be positive")
      Long restaurantId,
      @NotNull(message = "DishId is required")
      @Min(value = 1, message = "DishId must be positive")
      Long dishId,
      @NotNull(message = "Quantity is required")
      @Min(value = 1, message = "Quantity must be at least 1")
      Integer quantity
  ) {
  }

  public record UpdateItemQuantityRequest(
      @NotNull(message = "Quantity is required")
      @Min(value = 1, message = "Quantity must be at least 1")
      Integer quantity
  ) {
  }

  public record CartItemResponse(
      Long id,
      Long menuItemId,
      Long restaurantId,
      Integer quantity,
      String name,
      Double price
  ) {
  }

  public record CartResponse(
      Long id,
      Long userId,
      List<CartItemResponse> items,
      Double totalAmount,
      Long restaurantId
  ) {
  }
}

