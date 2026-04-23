package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import com.team7.model.client.DeliveryType;
import com.team7.model.client.Order;
import com.team7.model.client.OrderStatus;
import com.team7.model.client.PaymentMethod;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.repository.client.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock
  private CartService cartService;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;

  private OrderServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new OrderServiceImpl(cartService, orderRepository, courierAssignedOrderJpaRepository);
  }

  @Test
  void createOrderSuccessMovesItemsAndClearsCart() {
    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setRestaurantId(2L);
    cart.setTotalAmount(900.0);
    cart.setItems(List.of(new CartItem(10L, 100L, 2L, 2, "Burger", 450.0)));
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(2);
    LocalDateTime createdAt = LocalDateTime.now();
    given(cartService.getCart(1L)).willReturn(cart);
    given(orderRepository.createOrder(
        eq(1L), eq(2L), eq("Lenina 10"), eq(DeliveryType.DELIVERY), eq(deliveryTime), eq(PaymentMethod.CARD), eq(900.0), eq(cart.getItems())
    )).willReturn(new OrderRepository.OrderCreationResult(77L, createdAt));

    Order result = service.createOrder(1L, 2L, "Lenina 10", DeliveryType.DELIVERY, deliveryTime, PaymentMethod.CARD);

    assertEquals(77L, result.getId());
    assertEquals(900.0, result.getTotalAmount());
    assertEquals(OrderStatus.PENDING, result.getStatus());
    verify(cartService).clearCart(1L);
  }

  @Test
  void createOrderFailsWhenCartIsEmpty() {
    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setItems(List.of());
    given(cartService.getCart(1L)).willReturn(cart);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.createOrder(1L, 2L, "Addr", DeliveryType.DELIVERY, LocalDateTime.now().plusHours(1), PaymentMethod.CARD)
    );

    assertEquals("Корзина пуста", ex.getMessage());
  }

  @Test
  void createOrderPropagatesRepositoryErrorsForMissingEntities() {
    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setItems(List.of(new CartItem(10L, 999L, 2L, 1, "Unknown", 10.0)));
    cart.setTotalAmount(10.0);
    given(cartService.getCart(1L)).willReturn(cart);
    given(orderRepository.createOrder(any(), any(), any(), any(), any(), any(), any(), any()))
        .willThrow(new IllegalArgumentException("Ресторан не найден"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.createOrder(1L, 2L, "Addr", DeliveryType.DELIVERY, LocalDateTime.now().plusHours(1), PaymentMethod.CARD)
    );

    assertEquals("Ресторан не найден", ex.getMessage());
  }

  @Test
  void getOrderAttachesCourierIdWhenAssigned() {
    Order order = baseOrder(50L, 1L, 2L);
    given(orderRepository.getOrder(50L)).willReturn(order);
    CourierAssignedOrderEntity assigned = new CourierAssignedOrderEntity();
    assigned.setOrderId(50L);
    assigned.setCourierId(7L);
    given(courierAssignedOrderJpaRepository.findByOrderIdIn(List.of(50L))).willReturn(List.of(assigned));

    Order result = service.getOrder(50L);

    assertEquals(7L, result.getCourierId());
  }

  @Test
  void getUserOrdersAttachesCourierIdsForEachOrder() {
    Order o1 = baseOrder(10L, 1L, 2L);
    Order o2 = baseOrder(11L, 1L, 2L);
    given(orderRepository.getUserOrders(1L)).willReturn(List.of(o1, o2));
    CourierAssignedOrderEntity assigned = new CourierAssignedOrderEntity();
    assigned.setOrderId(11L);
    assigned.setCourierId(9L);
    given(courierAssignedOrderJpaRepository.findByOrderIdIn(List.of(10L, 11L))).willReturn(List.of(assigned));

    List<Order> result = service.getUserOrders(1L);

    assertEquals(2, result.size());
    assertEquals(null, result.get(0).getCourierId());
    assertEquals(9L, result.get(1).getCourierId());
  }

  @Test
  void cancelOrderDelegatesAndReturnsUpdatedStatus() {
    Order cancelled = baseOrder(12L, 1L, 2L);
    cancelled.setStatus(OrderStatus.CANCELLED);
    given(orderRepository.cancelOrder(12L)).willReturn(cancelled);
    given(courierAssignedOrderJpaRepository.findByOrderIdIn(List.of(12L))).willReturn(List.of());

    Order result = service.cancelOrder(12L);

    assertEquals(OrderStatus.CANCELLED, result.getStatus());
  }

  @Test
  void cancelOrderPropagatesStatusConflictError() {
    given(orderRepository.cancelOrder(12L)).willThrow(new IllegalArgumentException("Нельзя отменить заказ в текущем статусе"));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.cancelOrder(12L));

    assertEquals("Нельзя отменить заказ в текущем статусе", ex.getMessage());
  }

  @Test
  void repeatOrderUsesOriginalOrderData() {
    Order original = baseOrder(40L, 1L, 3L);
    original.setDeliveryAddress("Nevsky 1");
    original.setDeliveryType(DeliveryType.PICKUP);
    original.setPaymentMethod(PaymentMethod.CASH);
    given(orderRepository.getOrder(40L)).willReturn(original);
    given(courierAssignedOrderJpaRepository.findByOrderIdIn(List.of(40L))).willReturn(List.of());

    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setItems(List.of(new CartItem(10L, 100L, 3L, 1, "Pizza", 500.0)));
    cart.setTotalAmount(500.0);
    given(cartService.getCart(1L)).willReturn(cart);
    given(orderRepository.createOrder(eq(1L), eq(3L), eq("Nevsky 1"), eq(DeliveryType.PICKUP), any(LocalDateTime.class), eq(PaymentMethod.CASH), eq(500.0), eq(cart.getItems())))
        .willReturn(new OrderRepository.OrderCreationResult(41L, LocalDateTime.now()));

    Order repeated = service.repeatOrder(40L);

    assertNotNull(repeated);
    assertEquals(41L, repeated.getId());
  }

  @Test
  void getOrderPropagatesNotFoundError() {
    given(orderRepository.getOrder(404L)).willThrow(new IllegalArgumentException("Заказ не найден"));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.getOrder(404L));

    assertEquals("Заказ не найден", ex.getMessage());
  }

  private static Order baseOrder(Long id, Long userId, Long restaurantId) {
    Order order = new Order();
    order.setId(id);
    order.setUserId(userId);
    order.setRestaurantId(restaurantId);
    order.setStatus(OrderStatus.PENDING);
    order.setDeliveryType(DeliveryType.DELIVERY);
    order.setPaymentMethod(PaymentMethod.CARD);
    order.setDeliveryAddress("Address");
    order.setTotalAmount(1000.0);
    order.setCreatedAt(LocalDateTime.now());
    return order;
  }
}
