package com.team7.persistence;

import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class OrderJpaRepositoryTest {

  @Autowired
  private OrderJpaRepository orderJpaRepository;
  @Autowired
  private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;

  @Test
  void supportsUserRestaurantAdminSelectionsWithExpectedSorting() {
    OrderEntity u1Old = saveOrder(1L, 10L, "DELIVERY", "PENDING", LocalDateTime.now().minusHours(3));
    OrderEntity u1New = saveOrder(1L, 11L, "DELIVERY", "READY", LocalDateTime.now().minusHours(1));
    OrderEntity u2 = saveOrder(2L, 10L, "DELIVERY", "PENDING", LocalDateTime.now().minusHours(2));

    List<OrderEntity> byUser = orderJpaRepository.findByUserIdOrderByCreatedAtDesc(1L);
    assertEquals(2, byUser.size());
    assertEquals(u1New.getId(), byUser.get(0).getId());
    assertEquals(u1Old.getId(), byUser.get(1).getId());

    List<OrderEntity> byRestaurant = orderJpaRepository.findByRestaurantIdOrderByCreatedAtDesc(10L);
    assertEquals(2, byRestaurant.size());
    assertEquals(u2.getId(), byRestaurant.get(0).getId());
    assertEquals(u1Old.getId(), byRestaurant.get(1).getId());

    List<OrderEntity> all = orderJpaRepository.findAllByOrderByCreatedAtDesc();
    assertEquals(3, all.size());
    assertEquals(u1New.getId(), all.get(0).getId());
  }

  @Test
  void findsAvailableForCourierAssignmentUsingStatusDeliveryAndAssignmentFilters() {
    OrderEntity pendingDelivery = saveOrder(1L, 20L, "DELIVERY", "PENDING", LocalDateTime.now().minusMinutes(50));
    OrderEntity preparingDelivery = saveOrder(2L, 20L, " DELIVERY ", "PREPARING", LocalDateTime.now().minusMinutes(40));
    OrderEntity readyDelivery = saveOrder(3L, 20L, "delivery", " ready ", LocalDateTime.now().minusMinutes(30));
    OrderEntity pickedUpDelivery = saveOrder(4L, 20L, "DELIVERY", "PICKED_UP", LocalDateTime.now().minusMinutes(20));
    OrderEntity pickupReady = saveOrder(5L, 20L, "PICKUP", "READY", LocalDateTime.now().minusMinutes(10));

    CourierAssignedOrderEntity assigned = new CourierAssignedOrderEntity();
    assigned.setCourierId(99L);
    assigned.setOrderId(readyDelivery.getId());
    assigned.setStatus("ASSIGNED");
    assigned.setAssignedAt(LocalDateTime.now().minusMinutes(5));
    courierAssignedOrderJpaRepository.save(assigned);

    List<OrderEntity> available = orderJpaRepository.findAvailableForCourierAssignment();
    assertEquals(2, available.size());
    assertEquals(pendingDelivery.getId(), available.get(0).getId());
    assertEquals(preparingDelivery.getId(), available.get(1).getId());

    // excluded by query conditions:
    // - readyDelivery assigned already
    // - pickedUpDelivery status not in allowed set
    // - pickupReady delivery type is not DELIVERY
    assertEquals(0, available.stream().filter(o -> o.getId().equals(readyDelivery.getId())).count());
    assertEquals(0, available.stream().filter(o -> o.getId().equals(pickedUpDelivery.getId())).count());
    assertEquals(0, available.stream().filter(o -> o.getId().equals(pickupReady.getId())).count());
  }

  private OrderEntity saveOrder(
      Long userId,
      Long restaurantId,
      String deliveryType,
      String status,
      LocalDateTime createdAt
  ) {
    OrderEntity e = new OrderEntity();
    e.setUserId(userId);
    e.setRestaurantId(restaurantId);
    e.setDeliveryAddress("Address");
    e.setDeliveryType(deliveryType);
    e.setDeliveryTime(createdAt.plusHours(1));
    e.setPaymentMethod("CARD");
    e.setStatus(status);
    e.setTotalAmount(1000.0);
    e.setCreatedAt(createdAt);
    return orderJpaRepository.save(e);
  }
}
