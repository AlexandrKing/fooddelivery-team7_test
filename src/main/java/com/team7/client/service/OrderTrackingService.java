package com.team7.client.service;

import com.team7.client.model.Order;
import com.team7.client.model.OrderStatus;

import java.util.List;

public interface OrderTrackingService {
    Order getOrderStatus(String orderId);
    Order updateOrderStatus(String orderId, OrderStatus status);
    List<OrderStatus> getStatusHistory(String orderId);
}
