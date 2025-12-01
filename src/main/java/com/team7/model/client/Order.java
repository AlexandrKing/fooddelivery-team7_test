package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
