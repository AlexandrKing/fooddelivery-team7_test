package com.team7.client.service;

import com.team7.client.model.DeliveryType;
import com.team7.client.model.Order;
import com.team7.client.model.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(String userId, String restaurantId, String deliveryAddress,
                      DeliveryType deliveryType, LocalDateTime deliveryTime,
                      PaymentMethod paymentMethod);
    Order getOrder(String orderId);
    List<Order> getUserOrders(String userId);
    Order cancelOrder(String orderId);
    Order repeatOrder(String orderId);
}
