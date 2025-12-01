package com.team7.client.service;

import com.team7.client.model.Order;
import java.util.List;

public class HistoryServiceImpl implements HistoryService {
    private final OrderService orderService;

    public HistoryServiceImpl() {
        this.orderService = new OrderServiceImpl();
    }

    @Override
    public List<Order> getOrderHistory(Long userId) {
        return orderService.getUserOrders(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Override
    public Order repeatOrder(Long orderId) {
        return orderService.repeatOrder(orderId);
    }
}
