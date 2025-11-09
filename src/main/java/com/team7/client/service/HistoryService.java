package com.team7.client.service;

import com.team7.client.model.Order;

import java.util.List;

public interface HistoryService {
    List<Order> getOrderHistory(String userId);
    Order getOrderById(String orderId);
    Order repeatOrder(String orderId);
}
