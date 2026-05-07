package com.team7.repository.client;

import com.team7.model.client.CartItem;
import com.team7.model.client.DeliveryType;
import com.team7.model.client.OrderStatus;
import com.team7.model.client.PaymentMethod;
import com.team7.persistence.OrderItemJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.OrderStatusHistoryJpaRepository;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(OrderRepository.class)
@ActiveProfiles("test")
class OrderRepositoryJpaTest {

  @Autowired
  private OrderRepository repo;

  @Autowired
  private OrderJpaRepository orderJpaRepository;
  @Autowired
  private OrderItemJpaRepository orderItemJpaRepository;
  @Autowired
  private OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

  @Test
  void createAndGetOrderCoversItemsAndHistory() {
    LocalDateTime dt = LocalDateTime.now().plusHours(1);
    List<CartItem> items = List.of(new CartItem(1L, 101L, 2L, 2, "Burger", 100.0));

    OrderRepository.OrderCreationResult res = repo.createOrder(
        1L,
        2L,
        "Addr",
        DeliveryType.DELIVERY,
        dt,
        PaymentMethod.CARD,
        200.0,
        items
    );
    assertNotNull(res.getOrderId());

    var loaded = repo.getOrder(res.getOrderId());
    assertEquals(1L, loaded.getUserId());
    assertEquals(2L, loaded.getRestaurantId());
    assertEquals(OrderStatus.PENDING, loaded.getStatus());
    assertEquals(1, loaded.getItems().size());

    assertEquals(
        1,
        orderStatusHistoryJpaRepository.findAll().stream().filter(h -> res.getOrderId().equals(h.getOrderId())).count()
    );
  }

  @Test
  void cancelOrderValidatesStatusAndWritesHistory() {
    // create an order directly in DB
    OrderEntity oe = new OrderEntity();
    oe.setUserId(1L);
    oe.setRestaurantId(2L);
    oe.setDeliveryAddress("Addr");
    oe.setDeliveryType("DELIVERY");
    oe.setPaymentMethod("CARD");
    oe.setStatus("PENDING");
    oe.setTotalAmount(100.0);
    oe.setCreatedAt(LocalDateTime.now());
    OrderEntity saved = orderJpaRepository.save(oe);

    var cancelled = repo.cancelOrder(saved.getId());
    assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    assertTrue(orderStatusHistoryJpaRepository.findAll().stream()
        .filter(h -> saved.getId().equals(h.getOrderId()))
        .anyMatch(h -> "CANCELLED".equals(h.getStatus())));

    // already cancelled -> forbidden
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.cancelOrder(saved.getId()));
    assertEquals("Нельзя отменить заказ в текущем статусе", ex.getMessage());
  }

  @Test
  void getOrderThrowsWhenNotFound() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.getOrder(999L));
    assertEquals("Заказ не найден", ex.getMessage());
  }
}

