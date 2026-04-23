package com.team7.api.dto.courier;

import java.time.LocalDateTime;

public final class CourierDtos {
  private CourierDtos() {
  }

  public record AssignedOrderResponse(
      Long assignmentId,
      Long courierId,
      Long orderId,
      String status,
      LocalDateTime assignedAt,
      LocalDateTime pickedUpAt,
      LocalDateTime deliveredAt
  ) {
  }

  public record UpdateCourierOrderStatusRequest(String status) {
  }

  public record AvailableOrderResponse(
      Long id,
      Long userId,
      Long restaurantId,
      String status,
      Double totalAmount,
      String deliveryAddress,
      LocalDateTime createdAt
  ) {
  }
}

