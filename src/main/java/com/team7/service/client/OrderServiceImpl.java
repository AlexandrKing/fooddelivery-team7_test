package com.team7.service.client;

import com.team7.model.client.*;
import org.springframework.stereotype.Service;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.repository.client.OrderRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;

    public OrderServiceImpl(
        CartService cartService,
        OrderRepository orderRepository,
        CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository
    ) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.courierAssignedOrderJpaRepository = courierAssignedOrderJpaRepository;
    }

    @Override
    public Order createOrder(Long userId, Long restaurantId, String deliveryAddress,
                             DeliveryType deliveryType, LocalDateTime deliveryTime,
                             PaymentMethod paymentMethod) {

        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        OrderRepository.OrderCreationResult created = orderRepository.createOrder(
            userId,
            restaurantId,
            deliveryAddress,
            deliveryType,
            deliveryTime,
            paymentMethod,
            cart.getTotalAmount(),
            cart.getItems()
        );

        // Оркестрация: создание заказа + очистка корзины
        cartService.clearCart(userId);

        Order order = new Order();
        order.setId(created.getOrderId());
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryType(deliveryType);
        order.setPreferredDeliveryTime(deliveryTime);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(created.getCreatedAt());
        order.setTotalAmount(cart.getTotalAmount());
        return order;
    }

    @Override
    public Order getOrder(Long orderId) {
        Order order = orderRepository.getOrder(orderId);
        attachCourierIds(List.of(order));
        return order;
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.getUserOrders(userId);
        attachCourierIds(orders);
        return orders;
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.cancelOrder(orderId);
        attachCourierIds(List.of(order));
        return order;
    }

    @Override
    public Order repeatOrder(Long orderId) {
        Order originalOrder = getOrder(orderId);
        return createOrder(
            originalOrder.getUserId(),
            originalOrder.getRestaurantId(),
            originalOrder.getDeliveryAddress(),
            originalOrder.getDeliveryType(),
            LocalDateTime.now().plusHours(1),
            originalOrder.getPaymentMethod()
        );
    }

    private void attachCourierIds(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> ids = orders.stream().map(Order::getId).filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        List<CourierAssignedOrderEntity> assigned = courierAssignedOrderJpaRepository.findByOrderIdIn(ids);
        Map<Long, Long> byOrder = new HashMap<>();
        for (CourierAssignedOrderEntity a : assigned) {
            byOrder.putIfAbsent(a.getOrderId(), a.getCourierId());
        }
        for (Order o : orders) {
            if (o.getId() != null) {
                o.setCourierId(byOrder.get(o.getId()));
            }
        }
    }
}