package com.team7.api.dto.auth;

import com.team7.model.client.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AuthDtos {
  private AuthDtos() {
  }

  public record RegisterRequest(
      @NotBlank(message = "Name is required")
      String name,
      @NotBlank(message = "Email is required")
      @Email(message = "Email must be valid")
      String email,
      @NotBlank(message = "Phone is required")
      String phone,
      @NotBlank(message = "Password is required")
      @Size(min = 6, message = "Password must be at least 6 characters")
      String password,
      @NotBlank(message = "Confirm password is required")
      String confirmPassword
  ) {
  }

  public record LoginRequest(
      @NotBlank(message = "Email is required")
      @Email(message = "Email must be valid")
      String email,
      @NotBlank(message = "Password is required")
      String password
  ) {
  }

  public record UserResponse(
      Long id,
      String name,
      String email,
      String phone,
      List<Address> addresses
  ) {
  }
}

