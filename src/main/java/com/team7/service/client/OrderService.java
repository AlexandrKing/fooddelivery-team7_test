package com.team7.service.client;

import com.team7.model.client.DeliveryType;
import com.team7.model.client.Order;
import com.team7.model.client.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, Long restaurantId, String deliveryAddress,
                      DeliveryType deliveryType, LocalDateTime deliveryTime,
                      PaymentMethod paymentMethod);
    Order getOrder(Long orderId);
    List<Order> getUserOrders(Long userId);
    Order cancelOrder(Long orderId);
    Order repeatOrder(Long orderId);
}
