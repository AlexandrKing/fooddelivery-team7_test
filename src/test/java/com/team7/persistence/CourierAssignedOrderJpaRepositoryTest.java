package com.team7.persistence;

import com.team7.persistence.entity.CourierAssignedOrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class CourierAssignedOrderJpaRepositoryTest {

  @Autowired
  private CourierAssignedOrderJpaRepository repository;

  @Test
  void findsByCourierAndOrderWithoutFalsePositives() {
    CourierAssignedOrderEntity a1 = saveAssignment(10L, 100L, "ASSIGNED", LocalDateTime.now().minusMinutes(5));
    saveAssignment(11L, 100L, "ASSIGNED", LocalDateTime.now().minusMinutes(3));
    saveAssignment(10L, 101L, "ASSIGNED", LocalDateTime.now().minusMinutes(1));

    assertTrue(repository.findByCourierIdAndOrderId(10L, 100L).isPresent());
    assertEquals(a1.getId(), repository.findByCourierIdAndOrderId(10L, 100L).orElseThrow().getId());
    assertFalse(repository.findByCourierIdAndOrderId(12L, 100L).isPresent());
    assertFalse(repository.findByCourierIdAndOrderId(10L, 999L).isPresent());
  }

  @Test
  void supportsAssignmentAndOrderScopedQueries() {
    CourierAssignedOrderEntity a1 = saveAssignment(20L, 200L, "ASSIGNED", LocalDateTime.now().minusHours(2));
    CourierAssignedOrderEntity a2 = saveAssignment(20L, 201L, "ASSIGNED", LocalDateTime.now().minusHours(1));
    saveAssignment(21L, 202L, "ASSIGNED", LocalDateTime.now().minusMinutes(30));

    List<CourierAssignedOrderEntity> byCourier = repository.findByCourierIdOrderByAssignedAtDesc(20L);
    assertEquals(2, byCourier.size());
    assertEquals(a2.getId(), byCourier.get(0).getId());
    assertEquals(a1.getId(), byCourier.get(1).getId());

    assertTrue(repository.existsByOrderId(200L));
    assertFalse(repository.existsByOrderId(999L));
    assertEquals(1, repository.findByOrderId(201L).stream().count());
    assertEquals(2, repository.findByOrderIdIn(List.of(200L, 202L)).size());
  }

  private CourierAssignedOrderEntity saveAssignment(
      Long courierId,
      Long orderId,
      String status,
      LocalDateTime assignedAt
  ) {
    CourierAssignedOrderEntity e = new CourierAssignedOrderEntity();
    e.setCourierId(courierId);
    e.setOrderId(orderId);
    e.setStatus(status);
    e.setAssignedAt(assignedAt);
    return repository.save(e);
  }
}
