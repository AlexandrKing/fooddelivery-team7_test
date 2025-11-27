package com.team7.client.service;

import com.team7.client.model.Order;

import java.util.List;

public interface HistoryService {
    List<Order> getOrderHistory(Long userId);
    Order getOrderById(Long orderId);
    Order repeatOrder(Long orderId);
}
