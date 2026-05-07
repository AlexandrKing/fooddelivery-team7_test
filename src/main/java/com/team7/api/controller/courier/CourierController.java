package com.team7.api.controller.courier;

import com.team7.api.dto.courier.CourierDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.CourierTransactionEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.courier.CourierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courier")
public class CourierController {
  private final CourierService courierService;
  private final AppAccountJpaRepository appAccountJpaRepository;

  public CourierController(CourierService courierService, AppAccountJpaRepository appAccountJpaRepository) {
    this.courierService = courierService;
    this.appAccountJpaRepository = appAccountJpaRepository;
  }

  @GetMapping("/orders/assigned")
  public ApiSuccessResponse<List<CourierDtos.AssignedOrderResponse>> assigned(Authentication authentication) {
    Long courierId = resolveCourierId(authentication);
    return ApiSuccessResponse.of(
        courierService.getAssignedOrders(courierId).stream().map(this::toAssignedOrderResponse).toList()
    );
  }

  @GetMapping("/orders/available")
  public ApiSuccessResponse<List<CourierDtos.AvailableOrderResponse>> available() {
    return ApiSuccessResponse.of(
        courierService.getAvailableDeliveryOrders().stream().map(this::toAvailableOrderResponse).toList()
    );
  }

  @GetMapping("/me/balance")
  public ApiSuccessResponse<CourierDtos.BalanceResponse> balance(Authentication authentication) {
    Long courierId = resolveCourierId(authentication);
    return ApiSuccessResponse.of(new CourierDtos.BalanceResponse(courierService.getBalance(courierId)));
  }

  @GetMapping("/me/transactions")
  public ApiSuccessResponse<CourierDtos.TransactionPageResponse> transactions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Authentication authentication
  ) {
    Long courierId = resolveCourierId(authentication);
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
    Page<CourierTransactionEntity> transactions = courierService.getTransactions(courierId, pageable);
    return ApiSuccessResponse.of(new CourierDtos.TransactionPageResponse(
        transactions.getContent().stream().map(this::toTransactionResponse).toList(),
        transactions.getNumber(),
        transactions.getSize(),
        transactions.getTotalElements(),
        transactions.getTotalPages(),
        transactions.isLast()
    ));
  }

  @GetMapping("/me/stats")
  public ApiSuccessResponse<CourierDtos.StatsResponse> stats(Authentication authentication) {
    Long courierId = resolveCourierId(authentication);
    CourierService.CourierStats stats = courierService.getStats(courierId);
    return ApiSuccessResponse.of(
        new CourierDtos.StatsResponse(stats.balance(), stats.earnedToday(), stats.earnedThisWeek())
    );
  }

  @PostMapping("/orders/{orderId}/claim")
  public ApiSuccessResponse<CourierDtos.AssignedOrderResponse> claim(
      @PathVariable Long orderId,
      Authentication authentication
  ) {
    Long courierId = resolveCourierId(authentication);
    CourierAssignedOrderEntity entity = courierService.claimOrder(courierId, orderId);
    return ApiSuccessResponse.of(toAssignedOrderResponse(entity));
  }

  @PatchMapping("/orders/{orderId}/status")
  public ApiSuccessResponse<CourierDtos.AssignedOrderResponse> updateStatus(
      @PathVariable Long orderId,
      @RequestBody CourierDtos.UpdateCourierOrderStatusRequest request,
      Authentication authentication
  ) {
    if (request == null || request.status() == null || request.status().isBlank()) {
      throw new IllegalArgumentException("Status is required");
    }
    Long courierId = resolveCourierId(authentication);
    CourierAssignedOrderEntity entity =
        courierService.updateAssignedOrderStatus(courierId, orderId, request.status().trim());
    return ApiSuccessResponse.of(toAssignedOrderResponse(entity));
  }

  private Long resolveCourierId(Authentication authentication) {
    String email = authentication.getName();
    AppAccountEntity account = appAccountJpaRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    if (account.getLinkedCourierId() == null) {
      throw new IllegalArgumentException("Courier profile is not linked");
    }
    return account.getLinkedCourierId();
  }

  private CourierDtos.AssignedOrderResponse toAssignedOrderResponse(CourierAssignedOrderEntity e) {
    return new CourierDtos.AssignedOrderResponse(
        e.getId(),
        e.getCourierId(),
        e.getOrderId(),
        e.getStatus(),
        e.getAssignedAt(),
        e.getPickedUpAt(),
        e.getDeliveryTime()
    );
  }

  private CourierDtos.AvailableOrderResponse toAvailableOrderResponse(OrderEntity o) {
    return new CourierDtos.AvailableOrderResponse(
        o.getId(),
        o.getUserId(),
        o.getRestaurantId(),
        o.getStatus(),
        o.getTotalAmount(),
        o.getDeliveryAddress(),
        o.getCreatedAt()
    );
  }

  private CourierDtos.TransactionResponse toTransactionResponse(CourierTransactionEntity e) {
    return new CourierDtos.TransactionResponse(
        e.getId(),
        e.getCourierId(),
        e.getOrderId(),
        e.getAmount(),
        e.getType(),
        e.getCreatedAt()
    );
  }
}

