package com.team7.api.response;

import java.time.LocalDateTime;

public record ApiSuccessResponse<T>(
    boolean success,
    T data,
    String message,
    LocalDateTime timestamp
) {
  public static <T> ApiSuccessResponse<T> of(T data) {
    return new ApiSuccessResponse<>(true, data, null, LocalDateTime.now());
  }

  public static <T> ApiSuccessResponse<T> of(T data, String message) {
    return new ApiSuccessResponse<>(true, data, message, LocalDateTime.now());
  }
}

