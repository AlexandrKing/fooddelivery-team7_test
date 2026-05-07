package com.team7.service.client;

import com.team7.model.client.Order;
import com.team7.model.client.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTrackingServiceImplTest {

    @Mock
    private OrderService orderService;

    private OrderTrackingServiceImpl trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new OrderTrackingServiceImpl(orderService);
    }

    @Test
    void getOrderStatusReturnsOrderWithCurrentStatus() {
        Order order = order(15L, OrderStatus.IN_DELIVERY);
        when(orderService.getOrder(15L)).thenReturn(order);

        Order result = trackingService.getOrderStatus(15L);

        assertSame(order, result);
        assertEquals(OrderStatus.IN_DELIVERY, result.getStatus());
        verify(orderService).getOrder(15L);
    }

    @Test
    void getOrderStatusPropagatesNotFoundError() {
        when(orderService.getOrder(404L)).thenThrow(new IllegalArgumentException("Order not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> trackingService.getOrderStatus(404L)
        );

        assertEquals("Order not found", error.getMessage());
        verify(orderService).getOrder(404L);
    }

    @Test
    void updateOrderStatusChangesStatusOnLoadedOrder() {
        Order order = order(15L, OrderStatus.PREPARING);
        when(orderService.getOrder(15L)).thenReturn(order);

        Order result = trackingService.updateOrderStatus(15L, OrderStatus.DELIVERED);

        assertSame(order, result);
        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        verify(orderService).getOrder(15L);
    }

    @Test
    void updateOrderStatusPropagatesNotFoundError() {
        when(orderService.getOrder(404L)).thenThrow(new IllegalArgumentException("Order not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> trackingService.updateOrderStatus(404L, OrderStatus.CANCELLED)
        );

        assertEquals("Order not found", error.getMessage());
        verify(orderService).getOrder(404L);
    }

    @Test
    void getStatusHistoryReturnsDefaultLifecycleStatuses() {
        List<OrderStatus> result = trackingService.getStatusHistory(15L);

        assertEquals(
            List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING, OrderStatus.DELIVERED),
            result
        );
    }

    private static Order order(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(10L);
        order.setStatus(status);
        return order;
    }
}
