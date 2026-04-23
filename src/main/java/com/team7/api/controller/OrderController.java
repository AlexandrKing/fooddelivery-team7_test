package com.team7.api.controller;

import com.team7.api.dto.order.OrderDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.Order;
import com.team7.model.client.OrderItem;
import com.team7.persistence.UserJpaRepository;
import com.team7.repository.client.UserSecurityRepository;
import jakarta.validation.Valid;
import com.team7.service.client.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;
  private final UserSecurityRepository userSecurityRepository;
  private final UserJpaRepository userJpaRepository;

  public OrderController(
      OrderService orderService,
      UserSecurityRepository userSecurityRepository,
      UserJpaRepository userJpaRepository
  ) {
    this.orderService = orderService;
    this.userSecurityRepository = userSecurityRepository;
    this.userJpaRepository = userJpaRepository;
  }

  @PostMapping
  public ApiSuccessResponse<OrderDtos.OrderResponse> createOrder(
      @Valid @RequestBody OrderDtos.CreateOrderRequest request,
      Authentication authentication
  ) {
    enforceUserOwnership(authentication, request.userId());
    Order order = orderService.createOrder(
        request.userId(),
        request.restaurantId(),
        request.deliveryAddress(),
        request.deliveryType(),
        request.deliveryTime(),
        request.paymentMethod()
    );
    return ApiSuccessResponse.of(toOrderResponse(order));
  }

  @GetMapping("/{orderId}")
  public ApiSuccessResponse<OrderDtos.OrderResponse> getOrder(@PathVariable Long orderId, Authentication authentication) {
    Order order = orderService.getOrder(orderId);
    enforceUserOwnership(authentication, order.getUserId());
    return ApiSuccessResponse.of(toOrderResponse(order));
  }

  @GetMapping("/user/{userId}")
  public ApiSuccessResponse<List<OrderDtos.OrderResponse>> getUserOrders(@PathVariable Long userId, Authentication authentication) {
    enforceUserOwnership(authentication, userId);
    return ApiSuccessResponse.of(orderService.getUserOrders(userId).stream().map(this::toOrderResponse).toList());
  }

  @PostMapping("/{orderId}/cancel")
  public ApiSuccessResponse<OrderDtos.OrderResponse> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
    Order existing = orderService.getOrder(orderId);
    enforceUserOwnership(authentication, existing.getUserId());
    return ApiSuccessResponse.of(toOrderResponse(orderService.cancelOrder(orderId)));
  }

  @PostMapping("/{orderId}/repeat")
  public ApiSuccessResponse<OrderDtos.OrderResponse> repeatOrder(@PathVariable Long orderId, Authentication authentication) {
    Order existing = orderService.getOrder(orderId);
    enforceUserOwnership(authentication, existing.getUserId());
    return ApiSuccessResponse.of(toOrderResponse(orderService.repeatOrder(orderId)));
  }

  private void enforceUserOwnership(Authentication authentication, Long targetUserId) {
    if (!isUserRole(authentication)) {
      return;
    }
    Long currentUserId = resolveCurrentUserId(authentication);
    if (currentUserId == null || !currentUserId.equals(targetUserId)) {
      throw new IllegalArgumentException("Доступ к заказу запрещён");
    }
  }

  private boolean isUserRole(Authentication authentication) {
    if (authentication == null || authentication.getAuthorities() == null) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
  }

  private Long resolveCurrentUserId(Authentication authentication) {
    String email = authentication.getName();
    UserSecurityRepository.SecurityUserRecord rec = userSecurityRepository.findByEmail(email);
    if (rec == null) {
      throw new IllegalArgumentException("Учётная запись не найдена");
    }
    if (rec.linkedUserId() != null) {
      return rec.linkedUserId();
    }
    if ("USER".equals(rec.role())) {
      return userJpaRepository.findByEmail(email)
          .map(u -> u.getId())
          .orElseThrow(() -> new IllegalArgumentException("Профиль пользователя не найден"));
    }
    throw new IllegalArgumentException("Профиль пользователя не привязан к аккаунту");
  }

  private OrderDtos.OrderResponse toOrderResponse(Order order) {
    List<OrderDtos.OrderItemResponse> items = order.getItems() == null
        ? List.of()
        : order.getItems().stream().map(this::toOrderItemResponse).toList();

    return new OrderDtos.OrderResponse(
        order.getId(),
        order.getUserId(),
        order.getRestaurantId(),
        items,
        order.getStatus(),
        order.getDeliveryAddress(),
        order.getDeliveryType(),
        order.getPaymentMethod(),
        order.getPreferredDeliveryTime(),
        order.getTotalAmount(),
        order.getCreatedAt(),
        order.getCourierId()
    );
  }

  private OrderDtos.OrderItemResponse toOrderItemResponse(OrderItem item) {
    return new OrderDtos.OrderItemResponse(
        item.getId(),
        item.getMenuItemId(),
        item.getName(),
        item.getPrice(),
        item.getQuantity()
    );
  }
}

