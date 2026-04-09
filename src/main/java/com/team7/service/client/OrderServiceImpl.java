package com.team7.service.client;

import com.team7.model.client.*;
import org.springframework.stereotype.Service;
import com.team7.repository.client.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
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
        return orderRepository.getOrder(orderId);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.getUserOrders(userId);
    }

    @Override
    public Order cancelOrder(Long orderId) {
        return orderRepository.cancelOrder(orderId);
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
}