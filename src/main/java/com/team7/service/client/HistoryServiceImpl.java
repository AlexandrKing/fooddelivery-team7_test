package com.team7.service.client;

import com.team7.model.client.Order;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {
    private final OrderService orderService;

    public HistoryServiceImpl(OrderService orderService) {
        this.orderService = orderService;
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