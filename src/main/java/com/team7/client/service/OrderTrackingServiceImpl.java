package com.team7.client.service;

import com.team7.client.model.Order;
import com.team7.client.model.OrderStatus;
import java.util.*;

public class OrderTrackingServiceImpl implements OrderTrackingService {
    private static final Map<Long, List<OrderStatus>> STATUS_HISTORY = new HashMap<>();
    private final OrderService orderService;

    public OrderTrackingServiceImpl() {
        this.orderService = new OrderServiceImpl();
    }

    @Override
    public Order getOrderStatus(Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderService.getOrder(orderId);
        order.setStatus(status);

        if (!STATUS_HISTORY.containsKey(orderId)) {
            STATUS_HISTORY.put(orderId, new ArrayList<>());
        }

        List<OrderStatus> history = STATUS_HISTORY.get(orderId);
        history.add(status);

        return order;
    }

    @Override
    public List<OrderStatus> getStatusHistory(Long orderId) {
        if (!STATUS_HISTORY.containsKey(orderId)) {
            return new ArrayList<>();
        }

        return STATUS_HISTORY.get(orderId);
    }
}