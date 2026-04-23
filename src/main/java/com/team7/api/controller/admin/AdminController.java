package com.team7.api.controller.admin;

import com.team7.api.dto.admin.AdminDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.admin.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/accounts")
  public ApiSuccessResponse<List<AdminDtos.AccountResponse>> accounts() {
    return ApiSuccessResponse.of(adminService.getAccounts().stream().map(this::toAccountResponse).toList());
  }

  @PatchMapping("/accounts/{id}/active")
  public ApiSuccessResponse<AdminDtos.AccountResponse> setAccountActive(
      @PathVariable Long id,
      @RequestBody AdminDtos.SetAccountActiveRequest request
  ) {
    boolean active = request != null && Boolean.TRUE.equals(request.active());
    return ApiSuccessResponse.of(toAccountResponse(adminService.setAccountActive(id, active)));
  }

  @GetMapping("/orders")
  public ApiSuccessResponse<List<AdminDtos.AdminOrderResponse>> orders() {
    return ApiSuccessResponse.of(adminService.getOrders().stream().map(this::toOrderResponse).toList());
  }

  private AdminDtos.AccountResponse toAccountResponse(AppAccountEntity e) {
    return new AdminDtos.AccountResponse(
        e.getId(),
        e.getEmail(),
        e.getRole().name(),
        e.getIsActive(),
        e.getLinkedUserId(),
        e.getLinkedRestaurantId(),
        e.getLinkedCourierId(),
        e.getLinkedAdminId()
    );
  }

  private AdminDtos.AdminOrderResponse toOrderResponse(OrderEntity e) {
    return new AdminDtos.AdminOrderResponse(
        e.getId(),
        e.getUserId(),
        e.getRestaurantId(),
        e.getStatus(),
        e.getTotalAmount(),
        e.getCreatedAt()
    );
  }
}

