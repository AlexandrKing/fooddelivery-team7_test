package com.team7.api.controller;

import com.team7.api.dto.auth.AuthDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.User;
import jakarta.validation.Valid;
import com.team7.service.client.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ApiSuccessResponse<AuthDtos.UserResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
    User user = authService.register(
        request.name(),
        request.email(),
        request.phone(),
        request.password(),
        request.confirmPassword()
    );
    return ApiSuccessResponse.of(toUserResponse(user));
  }

  @PostMapping("/login")
  public ApiSuccessResponse<AuthDtos.UserResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
    User user = authService.login(request.email(), request.password());
    return ApiSuccessResponse.of(toUserResponse(user));
  }

  @PostMapping("/logout")
  public ApiSuccessResponse<String> logout() {
    authService.logout();
    return ApiSuccessResponse.of("ok", "Logged out");
  }

  @GetMapping("/me")
  public ApiSuccessResponse<AuthDtos.UserResponse> me() {
    User user = authService.getCurrentUser();
    if (user == null) {
      throw new IllegalArgumentException("Пользователь не авторизован");
    }
    return ApiSuccessResponse.of(toUserResponse(user));
  }

  private AuthDtos.UserResponse toUserResponse(User user) {
    return new AuthDtos.UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getAddresses()
    );
  }
}

