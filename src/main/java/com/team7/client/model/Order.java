package com.team7.client.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private List<OrderItem> items;
    private OrderStatus status;
    private String deliveryAddress;
    private DeliveryType deliveryType;
    private PaymentMethod paymentMethod;
    private LocalDateTime preferredDeliveryTime;
    private Double totalAmount;
    private LocalDateTime createdAt;
}
