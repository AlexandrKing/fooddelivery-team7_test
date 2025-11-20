package com.team7.client.service;

import com.team7.client.model.Order;
import com.team7.client.model.OrderStatus;

import java.util.List;

public interface OrderTrackingService {
    Order getOrderStatus(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    List<OrderStatus> getStatusHistory(Long orderId);
}
