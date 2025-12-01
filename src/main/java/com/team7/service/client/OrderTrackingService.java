package com.team7.service.client;

import com.team7.model.client.Order;
import com.team7.model.client.OrderStatus;

import java.util.List;

public interface OrderTrackingService {
    Order getOrderStatus(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    List<OrderStatus> getStatusHistory(Long orderId);
}
