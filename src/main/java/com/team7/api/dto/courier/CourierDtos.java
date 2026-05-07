package com.team7.api.dto.courier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

  public record BalanceResponse(BigDecimal balance) {
  }

  public record TransactionResponse(
      Long id,
      Long courierId,
      Long orderId,
      BigDecimal amount,
      String type,
      LocalDateTime createdAt
  ) {
  }

  public record TransactionPageResponse(
      List<TransactionResponse> content,
      int page,
      int size,
      long totalElements,
      int totalPages,
      boolean last
  ) {
  }

  public record StatsResponse(
      BigDecimal balance,
      BigDecimal earnedToday,
      BigDecimal earnedThisWeek
  ) {
  }
}

