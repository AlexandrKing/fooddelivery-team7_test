package com.team7.client.service;

import com.team7.client.model.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderServiceImpl implements OrderService {
    private static final Map<Long, Order> ORDERS = new HashMap<>();
    private static Long orderIdCounter = 1L;
    private static Long orderItemIdCounter = 1L;

    private final CartService cartService;

    public OrderServiceImpl() {
        this.cartService = new CartServiceImpl();
    }

    @Override
    public Order createOrder(Long userId, Long restaurantId, String deliveryAddress,
                             DeliveryType deliveryType, LocalDateTime deliveryTime,
                             PaymentMethod paymentMethod) {
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        Order order = new Order();
        order.setId(orderIdCounter++);
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryType(deliveryType);
        order.setPreferredDeliveryTime(deliveryTime);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(orderItemIdCounter++);
            orderItem.setMenuItemId(cartItem.getMenuItemId());
            orderItem.setName("Товар " + cartItem.getMenuItemId());
            orderItem.setPrice(100.0);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        order.setTotalAmount(cart.getTotalAmount());

        ORDERS.put(order.getId(), order);

        cartService.clearCart(userId);

        return order;
    }

    @Override
    public Order getOrder(Long orderId) {
        Order order = ORDERS.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ не найден");
        }
        return order;
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return ORDERS.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .toList();
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Нельзя отменить заказ в статусе: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

    @Override
    public Order repeatOrder(Long orderId) {
        Order originalOrder = getOrder(orderId);

        Order newOrder = new Order();
        newOrder.setId(orderIdCounter++);
        newOrder.setUserId(originalOrder.getUserId());
        newOrder.setRestaurantId(originalOrder.getRestaurantId());
        newOrder.setDeliveryAddress(originalOrder.getDeliveryAddress());
        newOrder.setDeliveryType(originalOrder.getDeliveryType());
        newOrder.setPreferredDeliveryTime(LocalDateTime.now().plusHours(1));
        newOrder.setPaymentMethod(originalOrder.getPaymentMethod());
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setItems(new ArrayList<>(originalOrder.getItems()));
        newOrder.setTotalAmount(originalOrder.getTotalAmount());

        ORDERS.put(newOrder.getId(), newOrder);
        return newOrder;
    }
}