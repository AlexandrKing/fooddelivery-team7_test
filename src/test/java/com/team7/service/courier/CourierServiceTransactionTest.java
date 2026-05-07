package com.team7.service.courier;

import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.CourierTransactionJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.CourierUserEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(CourierService.class)
@ActiveProfiles("test")
class CourierServiceTransactionTest {

  @Autowired
  private CourierService courierService;
  @Autowired
  private OrderJpaRepository orderJpaRepository;
  @Autowired
  private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  @Autowired
  private CourierUserJpaRepository courierUserJpaRepository;
  @Autowired
  private CourierTransactionJpaRepository courierTransactionJpaRepository;

  @Test
  void claimOrderCreatesAssignmentAndUpdateStatusSyncsOrder() {
    OrderEntity order = saveOrder("DELIVERY", "READY");
    Long courierId = saveCourier(BigDecimal.ZERO).getId();

    CourierAssignedOrderEntity claimed = courierService.claimOrder(courierId, order.getId());
    assertEquals(courierId, claimed.getCourierId());
    assertEquals("ASSIGNED", claimed.getStatus());
    assertNotNull(claimed.getAssignedAt());

    CourierAssignedOrderEntity updated = courierService.updateAssignedOrderStatus(courierId, order.getId(), "PICKED_UP");
    assertEquals("PICKED_UP", updated.getStatus());
    assertNotNull(updated.getPickedUpAt());
    assertEquals("PICKED_UP", orderJpaRepository.findById(order.getId()).orElseThrow().getStatus());

    CourierAssignedOrderEntity delivering = courierService.updateAssignedOrderStatus(courierId, order.getId(), "DELIVERING");
    assertEquals("DELIVERING", delivering.getStatus());
    assertEquals("DELIVERING", orderJpaRepository.findById(order.getId()).orElseThrow().getStatus());

    CourierAssignedOrderEntity delivered = courierService.updateAssignedOrderStatus(courierId, order.getId(), "DELIVERED");
    assertEquals("DELIVERED", delivered.getStatus());
    assertNotNull(delivered.getDeliveryTime());
    assertEquals("DELIVERED", orderJpaRepository.findById(order.getId()).orElseThrow().getStatus());
    assertEquals(0, BigDecimal.valueOf(100.00).compareTo(courierUserJpaRepository.findById(courierId).orElseThrow().getBalance()));
    assertEquals(1, courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(courierId).size());
  }

  @Test
  void deliveredAccrualIsIdempotentForAlreadyDeliveredStatus() {
    OrderEntity order = saveOrder("DELIVERY", "READY");
    Long courierId = saveCourier(BigDecimal.ZERO).getId();
    courierService.claimOrder(courierId, order.getId());
    courierService.updateAssignedOrderStatus(courierId, order.getId(), "PICKED_UP");
    courierService.updateAssignedOrderStatus(courierId, order.getId(), "DELIVERING");

    courierService.updateAssignedOrderStatus(courierId, order.getId(), "DELIVERED");
    courierService.updateAssignedOrderStatus(courierId, order.getId(), "DELIVERED");

    assertEquals(0, BigDecimal.valueOf(100.00).compareTo(courierUserJpaRepository.findById(courierId).orElseThrow().getBalance()));
    assertEquals(1, courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(courierId).size());
  }

  @Test
  void updateAssignedOrderStatusForWrongCourierDoesNotCorruptOrder() {
    OrderEntity order = saveOrder("DELIVERY", "READY");
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setCourierId(60L);
    assignment.setOrderId(order.getId());
    assignment.setStatus("ASSIGNED");
    assignment.setAssignedAt(LocalDateTime.now());
    courierAssignedOrderJpaRepository.save(assignment);

    assertThrows(
        IllegalArgumentException.class,
        () -> courierService.updateAssignedOrderStatus(61L, order.getId(), "DELIVERED")
    );

    assertEquals("READY", orderJpaRepository.findById(order.getId()).orElseThrow().getStatus());
  }

  @Test
  void invalidTransitionDoesNotPartiallyUpdateAssignmentOrOrder() {
    OrderEntity order = saveOrder("DELIVERY", "READY");
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setCourierId(70L);
    assignment.setOrderId(order.getId());
    assignment.setStatus("ASSIGNED");
    assignment.setAssignedAt(LocalDateTime.now());
    CourierAssignedOrderEntity savedAssignment = courierAssignedOrderJpaRepository.save(assignment);

    assertThrows(
        IllegalArgumentException.class,
        () -> courierService.updateAssignedOrderStatus(70L, order.getId(), "DELIVERED")
    );

    CourierAssignedOrderEntity after = courierAssignedOrderJpaRepository.findById(savedAssignment.getId()).orElseThrow();
    assertEquals("ASSIGNED", after.getStatus());
    assertEquals(null, after.getDeliveryTime());
    assertEquals("READY", orderJpaRepository.findById(order.getId()).orElseThrow().getStatus());
  }

  private OrderEntity saveOrder(String deliveryType, String status) {
    OrderEntity e = new OrderEntity();
    e.setUserId(1L);
    e.setRestaurantId(1L);
    e.setDeliveryAddress("Address");
    e.setDeliveryType(deliveryType);
    e.setDeliveryTime(LocalDateTime.now().plusHours(1));
    e.setPaymentMethod("CARD");
    e.setStatus(status);
    e.setTotalAmount(500.0);
    e.setCreatedAt(LocalDateTime.now());
    return orderJpaRepository.save(e);
  }

  private CourierUserEntity saveCourier(BigDecimal balance) {
    CourierUserEntity courier = new CourierUserEntity();
    String suffix = String.valueOf(System.nanoTime());
    courier.setUsername("courier" + suffix);
    courier.setEmail("courier" + suffix + "@test.local");
    courier.setFullName("Courier " + suffix);
    courier.setPhone("+7999" + suffix.substring(Math.max(0, suffix.length() - 7)));
    courier.setStatus("online");
    courier.setBalance(balance);
    courier.setIsActive(Boolean.TRUE);
    return courierUserJpaRepository.save(courier);
  }
}
