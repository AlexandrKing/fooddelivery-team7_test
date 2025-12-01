package com.team7.service.client;

import com.team7.model.client.Order;

import java.util.List;

public interface HistoryService {
    List<Order> getOrderHistory(Long userId);
    Order getOrderById(Long orderId);
    Order repeatOrder(Long orderId);
}
