package com.team7.api.dto.order;

import com.team7.model.client.DeliveryType;
import com.team7.model.client.OrderStatus;
import com.team7.model.client.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {
  private OrderDtos() {
  }

  public record CreateOrderRequest(
      @NotNull(message = "UserId is required")
      @Min(value = 1, message = "UserId must be positive")
      Long userId,
      @NotNull(message = "RestaurantId is required")
      @Min(value = 1, message = "RestaurantId must be positive")
      Long restaurantId,
      @NotBlank(message = "Delivery address is required")
      String deliveryAddress,
      @NotNull(message = "Delivery type is required")
      DeliveryType deliveryType,
      @NotNull(message = "Delivery time is required")
      @Future(message = "Delivery time must be in the future")
      LocalDateTime deliveryTime,
      @NotNull(message = "Payment method is required")
      PaymentMethod paymentMethod
  ) {
  }

  public record OrderItemResponse(
      Long id,
      Long menuItemId,
      String name,
      Double price,
      Integer quantity
  ) {
  }

  public record OrderResponse(
      Long id,
      Long userId,
      Long restaurantId,
      List<OrderItemResponse> items,
      OrderStatus status,
      String deliveryAddress,
      DeliveryType deliveryType,
      PaymentMethod paymentMethod,
      LocalDateTime preferredDeliveryTime,
      Double totalAmount,
      LocalDateTime createdAt,
      Long courierId
  ) {
  }
}

