package com.team7.repository.client;

import com.team7.model.client.CartItem;
import com.team7.model.client.DeliveryType;
import com.team7.model.client.PaymentMethod;
import com.team7.persistence.OrderItemJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.OrderStatusHistoryJpaRepository;
import com.team7.persistence.entity.OrderEntity;
import com.team7.persistence.entity.OrderStatusHistoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(OrderRepository.class)
@ActiveProfiles("test")
class OrderRepositoryTransactionTest {

  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private OrderJpaRepository orderJpaRepository;
  @Autowired
  private OrderItemJpaRepository orderItemJpaRepository;
  @Autowired
  private OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

  @Test
  void createOrderWritesOrderItemsAndInitialHistoryInSingleTransaction() {
    OrderRepository.OrderCreationResult created = orderRepository.createOrder(
        1L,
        10L,
        "Lenina 1",
        DeliveryType.DELIVERY,
        LocalDateTime.now().plusHours(1),
        PaymentMethod.CARD,
        1500.0,
        List.of(
            new CartItem(1L, 101L, 10L, 2, "Burger", 500.0),
            new CartItem(2L, 102L, 10L, 1, "Cola", 500.0)
        )
    );

    Long orderId = created.getOrderId();
    OrderEntity order = orderJpaRepository.findById(orderId).orElseThrow();
    assertEquals("PENDING", order.getStatus());
    assertEquals(2, orderItemJpaRepository.findByOrderIdOrderByIdAsc(orderId).size());

    List<OrderStatusHistoryEntity> history = orderStatusHistoryJpaRepository.findAll().stream()
        .filter(h -> h.getOrderId().equals(orderId))
        .sorted(Comparator.comparing(OrderStatusHistoryEntity::getCreatedAt))
        .toList();
    assertEquals(1, history.size());
    assertEquals("PENDING", history.get(0).getStatus());
  }

  @Test
  void cancelOrderUpdatesOrderAndAppendsHistoryEntry() {
    OrderRepository.OrderCreationResult created = orderRepository.createOrder(
        2L,
        20L,
        "Lenina 2",
        DeliveryType.DELIVERY,
        LocalDateTime.now().plusHours(2),
        PaymentMethod.CARD,
        1000.0,
        List.of(new CartItem(3L, 103L, 20L, 1, "Pizza", 1000.0))
    );
    Long orderId = created.getOrderId();

    orderRepository.cancelOrder(orderId);

    OrderEntity order = orderJpaRepository.findById(orderId).orElseThrow();
    assertEquals("CANCELLED", order.getStatus());
    List<OrderStatusHistoryEntity> history = orderStatusHistoryJpaRepository.findAll().stream()
        .filter(h -> h.getOrderId().equals(orderId))
        .toList();
    assertEquals(2, history.size());
    assertEquals(
        List.of("PENDING", "CANCELLED"),
        history.stream().map(OrderStatusHistoryEntity::getStatus).toList()
    );
  }

  @Test
  void cancelOrderRejectsInvalidTransitionAndDoesNotAppendHistory() {
    OrderEntity nonPending = new OrderEntity();
    nonPending.setUserId(3L);
    nonPending.setRestaurantId(30L);
    nonPending.setDeliveryAddress("Addr");
    nonPending.setDeliveryType("DELIVERY");
    nonPending.setDeliveryTime(LocalDateTime.now().plusHours(1));
    nonPending.setPaymentMethod("CARD");
    nonPending.setStatus("READY");
    nonPending.setTotalAmount(500.0);
    nonPending.setCreatedAt(LocalDateTime.now());
    OrderEntity saved = orderJpaRepository.save(nonPending);

    assertThrows(IllegalArgumentException.class, () -> orderRepository.cancelOrder(saved.getId()));

    OrderEntity after = orderJpaRepository.findById(saved.getId()).orElseThrow();
    assertEquals("READY", after.getStatus());
    long historyCount = orderStatusHistoryJpaRepository.findAll().stream()
        .filter(h -> h.getOrderId().equals(saved.getId()))
        .count();
    assertEquals(0, historyCount);
  }
}
