package com.team7.persistence;

import com.team7.persistence.entity.OrderStatusHistoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class OrderStatusHistoryJpaRepositoryTest {

  @Autowired
  private OrderStatusHistoryJpaRepository repository;

  @Test
  void savesAndReadsHistoryForSpecificOrderWithoutCrossOrderLeak() {
    saveHistory(100L, "PENDING", LocalDateTime.now().minusMinutes(30));
    saveHistory(100L, "PREPARING", LocalDateTime.now().minusMinutes(20));
    saveHistory(100L, "READY", LocalDateTime.now().minusMinutes(10));
    saveHistory(200L, "PENDING", LocalDateTime.now().minusMinutes(5));

    List<OrderStatusHistoryEntity> order100 = repository.findAll().stream()
        .filter(h -> h.getOrderId().equals(100L))
        .sorted(Comparator.comparing(OrderStatusHistoryEntity::getCreatedAt))
        .toList();

    assertEquals(3, order100.size());
    assertEquals("PENDING", order100.get(0).getStatus());
    assertEquals("PREPARING", order100.get(1).getStatus());
    assertEquals("READY", order100.get(2).getStatus());

    List<OrderStatusHistoryEntity> order200 = repository.findAll().stream()
        .filter(h -> h.getOrderId().equals(200L))
        .toList();
    assertEquals(1, order200.size());
  }

  private void saveHistory(Long orderId, String status, LocalDateTime createdAt) {
    OrderStatusHistoryEntity e = new OrderStatusHistoryEntity();
    e.setOrderId(orderId);
    e.setStatus(status);
    e.setCreatedAt(createdAt);
    repository.save(e);
  }
}
