package com.team7.service.client;

import com.team7.model.client.Order;
import com.team7.model.client.OrderStatus;
import java.util.List;

public class OrderTrackingServiceImpl implements OrderTrackingService {
    private final OrderService orderService;

    public OrderTrackingServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Order getOrderStatus(Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        // В реальной реализации нужно обновлять статус в БД
        Order order = orderService.getOrder(orderId);
        order.setStatus(status);
        return order;
    }

    @Override
    public List<OrderStatus> getStatusHistory(Long orderId) {
        // В реальной реализации нужно получать историю из БД
        return List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING, OrderStatus.DELIVERED);
    }
}