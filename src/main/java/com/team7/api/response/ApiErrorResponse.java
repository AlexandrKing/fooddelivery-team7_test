package com.team7.api.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
    boolean success,
    int status,
    String error,
    String message,
    List<String> details,
    LocalDateTime timestamp
) {
  public static ApiErrorResponse of(int status, String error, String message) {
    return new ApiErrorResponse(false, status, error, message, List.of(), LocalDateTime.now());
  }

  public static ApiErrorResponse of(int status, String error, String message, List<String> details) {
    return new ApiErrorResponse(false, status, error, message, details, LocalDateTime.now());
  }
}

