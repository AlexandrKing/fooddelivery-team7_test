package com.team7.api.controller;

import com.team7.api.dto.auth.AuthDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.persistence.AdminUserJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AdminUserEntity;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.CourierUserEntity;
import com.team7.persistence.entity.UserEntity;
import jakarta.validation.Valid;
import com.team7.service.client.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final AuthenticationManager authenticationManager;
  private final AppAccountJpaRepository appAccountJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final CourierUserJpaRepository courierUserJpaRepository;
  private final AdminUserJpaRepository adminUserJpaRepository;

  public AuthController(
      AuthService authService,
      AuthenticationManager authenticationManager,
      AppAccountJpaRepository appAccountJpaRepository,
      UserJpaRepository userJpaRepository,
      CourierUserJpaRepository courierUserJpaRepository,
      AdminUserJpaRepository adminUserJpaRepository
  ) {
    this.authService = authService;
    this.authenticationManager = authenticationManager;
    this.appAccountJpaRepository = appAccountJpaRepository;
    this.userJpaRepository = userJpaRepository;
    this.courierUserJpaRepository = courierUserJpaRepository;
    this.adminUserJpaRepository = adminUserJpaRepository;
  }

  @PostMapping("/register")
  public ApiSuccessResponse<AuthDtos.UserResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
    authService.register(
        request.name(),
        request.email(),
        request.phone(),
        request.password(),
        request.confirmPassword()
    );
    return ApiSuccessResponse.of(buildPrincipalResponse(request.email()));
  }

  @PostMapping("/login")
  public ApiSuccessResponse<AuthDtos.UserResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
    return ApiSuccessResponse.of(buildPrincipalResponse(request.email()));
  }

  @PostMapping("/logout")
  public ApiSuccessResponse<String> logout() {
    authService.logout();
    return ApiSuccessResponse.of("ok", "Logged out");
  }

  @GetMapping("/me")
  public ApiSuccessResponse<AuthDtos.UserResponse> me() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalArgumentException("Пользователь не авторизован");
    }
    return ApiSuccessResponse.of(buildPrincipalResponse(authentication.getName()));
  }

  private AuthDtos.UserResponse buildPrincipalResponse(String email) {
    AppAccountEntity account = appAccountJpaRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Учетная запись не найдена"));

    String role = account.getRole().name();
    if ("USER".equals(role)) {
      UserEntity user = account.getLinkedUserId() == null
          ? userJpaRepository.findByEmail(email).orElse(null)
          : userJpaRepository.findById(account.getLinkedUserId()).orElse(null);
      if (user == null) {
        throw new IllegalArgumentException("Профиль пользователя не найден");
      }
      return new AuthDtos.UserResponse(
          user.getId(),
          user.getFullName(),
          user.getEmail(),
          user.getPhone(),
          List.of(),
          role,
          account.getId(),
          account.getLinkedUserId(),
          account.getLinkedRestaurantId(),
          account.getLinkedCourierId(),
          account.getLinkedAdminId()
      );
    }

    if ("RESTAURANT".equals(role)) {
      return new AuthDtos.UserResponse(
          account.getLinkedRestaurantId(),
          "Restaurant Account",
          account.getEmail(),
          null,
          List.of(),
          role,
          account.getId(),
          account.getLinkedUserId(),
          account.getLinkedRestaurantId(),
          account.getLinkedCourierId(),
          account.getLinkedAdminId()
      );
    }

    if ("COURIER".equals(role)) {
      CourierUserEntity courier = account.getLinkedCourierId() == null
          ? null
          : courierUserJpaRepository.findById(account.getLinkedCourierId()).orElse(null);
      return new AuthDtos.UserResponse(
          account.getLinkedCourierId(),
          courier != null ? courier.getFullName() : "Courier Account",
          account.getEmail(),
          courier != null ? courier.getPhone() : null,
          List.of(),
          role,
          account.getId(),
          account.getLinkedUserId(),
          account.getLinkedRestaurantId(),
          account.getLinkedCourierId(),
          account.getLinkedAdminId()
      );
    }

    AdminUserEntity admin = account.getLinkedAdminId() == null
        ? null
        : adminUserJpaRepository.findById(account.getLinkedAdminId()).orElse(null);
    return new AuthDtos.UserResponse(
        account.getLinkedAdminId(),
        admin != null ? admin.getFullName() : "Admin Account",
        account.getEmail(),
        null,
        List.of(),
        role,
        account.getId(),
        account.getLinkedUserId(),
        account.getLinkedRestaurantId(),
        account.getLinkedCourierId(),
        account.getLinkedAdminId()
    );
  }

}

