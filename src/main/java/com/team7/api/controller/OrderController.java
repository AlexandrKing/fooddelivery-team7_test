package com.team7.api.controller;

import com.team7.api.dto.order.OrderDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.Order;
import com.team7.model.client.OrderItem;
import jakarta.validation.Valid;
import com.team7.service.client.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ApiSuccessResponse<OrderDtos.OrderResponse> createOrder(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
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
  public ApiSuccessResponse<OrderDtos.OrderResponse> getOrder(@PathVariable Long orderId) {
    return ApiSuccessResponse.of(toOrderResponse(orderService.getOrder(orderId)));
  }

  @GetMapping("/user/{userId}")
  public ApiSuccessResponse<List<OrderDtos.OrderResponse>> getUserOrders(@PathVariable Long userId) {
    return ApiSuccessResponse.of(orderService.getUserOrders(userId).stream().map(this::toOrderResponse).toList());
  }

  @PostMapping("/{orderId}/cancel")
  public ApiSuccessResponse<OrderDtos.OrderResponse> cancelOrder(@PathVariable Long orderId) {
    return ApiSuccessResponse.of(toOrderResponse(orderService.cancelOrder(orderId)));
  }

  @PostMapping("/{orderId}/repeat")
  public ApiSuccessResponse<OrderDtos.OrderResponse> repeatOrder(@PathVariable Long orderId) {
    return ApiSuccessResponse.of(toOrderResponse(orderService.repeatOrder(orderId)));
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
        order.getCreatedAt()
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

