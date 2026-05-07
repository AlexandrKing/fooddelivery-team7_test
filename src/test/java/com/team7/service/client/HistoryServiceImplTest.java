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
class HistoryServiceImplTest {

    @Mock
    private OrderService orderService;

    private HistoryServiceImpl historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryServiceImpl(orderService);
    }

    @Test
    void getOrderHistoryReturnsOrdersFromOrderService() {
        Order pendingOrder = order(1L, OrderStatus.PENDING);
        Order deliveredOrder = order(2L, OrderStatus.DELIVERED);
        when(orderService.getUserOrders(10L)).thenReturn(List.of(pendingOrder, deliveredOrder));

        List<Order> result = historyService.getOrderHistory(10L);

        assertEquals(List.of(pendingOrder, deliveredOrder), result);
        verify(orderService).getUserOrders(10L);
    }

    @Test
    void getOrderHistoryReturnsEmptyListWhenUserHasNoOrders() {
        when(orderService.getUserOrders(10L)).thenReturn(List.of());

        List<Order> result = historyService.getOrderHistory(10L);

        assertEquals(List.of(), result);
        verify(orderService).getUserOrders(10L);
    }

    @Test
    void getOrderByIdReturnsOrder() {
        Order order = order(5L, OrderStatus.DELIVERING);
        when(orderService.getOrder(5L)).thenReturn(order);

        Order result = historyService.getOrderById(5L);

        assertSame(order, result);
        verify(orderService).getOrder(5L);
    }

    @Test
    void getOrderByIdPropagatesNotFoundError() {
        when(orderService.getOrder(404L)).thenThrow(new IllegalArgumentException("Order not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> historyService.getOrderById(404L)
        );

        assertEquals("Order not found", error.getMessage());
        verify(orderService).getOrder(404L);
    }

    @Test
    void repeatOrderReturnsRepeatedOrder() {
        Order repeatedOrder = order(6L, OrderStatus.PENDING);
        when(orderService.repeatOrder(5L)).thenReturn(repeatedOrder);

        Order result = historyService.repeatOrder(5L);

        assertSame(repeatedOrder, result);
        verify(orderService).repeatOrder(5L);
    }

    @Test
    void repeatOrderPropagatesOrderServiceError() {
        when(orderService.repeatOrder(404L)).thenThrow(new IllegalArgumentException("Order not found"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> historyService.repeatOrder(404L)
        );

        assertEquals("Order not found", error.getMessage());
        verify(orderService).repeatOrder(404L);
    }

    private static Order order(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(10L);
        order.setStatus(status);
        return order;
    }
}
