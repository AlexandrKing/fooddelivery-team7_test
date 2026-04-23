package com.team7.api.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class CourierReviewDtos {
  private CourierReviewDtos() {
  }

  public record CreateCourierReviewRequest(
      @NotNull(message = "orderId is required")
      @Min(value = 1, message = "orderId must be positive")
      Long orderId,
      @NotNull(message = "rating is required")
      @Min(value = 1, message = "rating must be at least 1")
      @Max(value = 5, message = "rating must be at most 5")
      Integer rating,
      @Size(max = 4000, message = "comment is too long")
      String comment
  ) {
  }

  public record CourierReviewResponse(
      Long id,
      Long orderId,
      Long userId,
      Long courierId,
      Integer rating,
      String comment,
      LocalDateTime createdAt
  ) {
  }

  public record AdminCourierReviewResponse(
      Long id,
      Long orderId,
      Long userId,
      String userLabel,
      Long courierId,
      String courierLabel,
      Integer rating,
      String comment,
      LocalDateTime createdAt
  ) {
  }
}
