package com.team7.api.dto.admin;

import java.time.LocalDateTime;

public final class AdminDtos {
  private AdminDtos() {
  }

  public record AccountResponse(
      Long id,
      String email,
      String role,
      Boolean active,
      Long linkedUserId,
      Long linkedRestaurantId,
      Long linkedCourierId,
      Long linkedAdminId
  ) {
  }

  public record SetAccountActiveRequest(Boolean active) {
  }

  public record AdminOrderResponse(
      Long id,
      Long userId,
      Long restaurantId,
      String status,
      Double totalAmount,
      LocalDateTime createdAt
  ) {
  }
}

