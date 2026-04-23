package com.team7.service.courier;

import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

  @Mock
  private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  @Mock
  private OrderJpaRepository orderJpaRepository;

  private CourierService service;

  @BeforeEach
  void setUp() {
    service = new CourierService(courierAssignedOrderJpaRepository, orderJpaRepository);
  }

  @Test
  void listMethodsReturnRepositoryData() {
    given(courierAssignedOrderJpaRepository.findByCourierIdOrderByAssignedAtDesc(9L)).willReturn(List.of());
    given(orderJpaRepository.findAvailableForCourierAssignment()).willReturn(List.of());
    assertEquals(0, service.getAssignedOrders(9L).size());
    assertEquals(0, service.getAvailableDeliveryOrders().size());
  }

  @Test
  void claimOrderHappyPathAndRestrictions() {
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setDeliveryType("DELIVERY");
    order.setStatus("READY");
    given(orderJpaRepository.findById(100L)).willReturn(Optional.of(order));
    given(courierAssignedOrderJpaRepository.existsByOrderId(100L)).willReturn(false);
    given(courierAssignedOrderJpaRepository.save(any(CourierAssignedOrderEntity.class)))
        .willAnswer(a -> a.getArgument(0));

    CourierAssignedOrderEntity claimed = service.claimOrder(9L, 100L);
    assertEquals(9L, claimed.getCourierId());
    assertEquals("ASSIGNED", claimed.getStatus());
    assertNotNull(claimed.getAssignedAt());

    given(orderJpaRepository.findById(404L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.claimOrder(9L, 404L));

    OrderEntity pickup = new OrderEntity();
    pickup.setId(101L);
    pickup.setDeliveryType("PICKUP");
    pickup.setStatus("READY");
    given(orderJpaRepository.findById(101L)).willReturn(Optional.of(pickup));
    assertThrows(IllegalArgumentException.class, () -> service.claimOrder(9L, 101L));

    OrderEntity wrongStatus = new OrderEntity();
    wrongStatus.setId(102L);
    wrongStatus.setDeliveryType("DELIVERY");
    wrongStatus.setStatus("CANCELLED");
    given(orderJpaRepository.findById(102L)).willReturn(Optional.of(wrongStatus));
    assertThrows(IllegalArgumentException.class, () -> service.claimOrder(9L, 102L));

    OrderEntity alreadyAssigned = new OrderEntity();
    alreadyAssigned.setId(103L);
    alreadyAssigned.setDeliveryType("DELIVERY");
    alreadyAssigned.setStatus("READY");
    given(orderJpaRepository.findById(103L)).willReturn(Optional.of(alreadyAssigned));
    given(courierAssignedOrderJpaRepository.existsByOrderId(103L)).willReturn(true);
    assertThrows(IllegalArgumentException.class, () -> service.claimOrder(9L, 103L));
  }

  @Test
  void updateAssignedOrderStatusHappyPathAndErrors() {
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setId(1L);
    assignment.setCourierId(9L);
    assignment.setOrderId(100L);
    assignment.setStatus("ASSIGNED");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 100L)).willReturn(Optional.of(assignment));
    given(courierAssignedOrderJpaRepository.save(any(CourierAssignedOrderEntity.class))).willAnswer(a -> a.getArgument(0));
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setStatus("READY");
    given(orderJpaRepository.findById(100L)).willReturn(Optional.of(order));
    given(orderJpaRepository.save(any(OrderEntity.class))).willAnswer(a -> a.getArgument(0));

    CourierAssignedOrderEntity updated = service.updateAssignedOrderStatus(9L, 100L, "PICKED_UP");
    assertEquals("PICKED_UP", updated.getStatus());
    assertNotNull(updated.getPickedUpAt());
    assertEquals("PICKED_UP", order.getStatus());

    CourierAssignedOrderEntity inDelivery = service.updateAssignedOrderStatus(9L, 100L, "DELIVERING");
    assertEquals("DELIVERING", inDelivery.getStatus());
    assertEquals("DELIVERING", order.getStatus());

    CourierAssignedOrderEntity delivered = service.updateAssignedOrderStatus(9L, 100L, "DELIVERED");
    assertNotNull(delivered.getDeliveryTime());
    assertEquals("DELIVERED", order.getStatus());

    assignment.setStatus("ASSIGNED");
    IllegalArgumentException invalidTransition = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateAssignedOrderStatus(9L, 100L, "DELIVERED")
    );
    assertTrue(invalidTransition.getMessage().contains("Недопустимый переход статуса курьера"));
    assertTrue(invalidTransition.getMessage().contains("ASSIGNED -> DELIVERED"));

    IllegalArgumentException invalidStatus = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateAssignedOrderStatus(9L, 100L, "READY")
    );
    assertTrue(invalidStatus.getMessage().contains("Недопустимый переход статуса курьера"));
    assertTrue(invalidStatus.getMessage().contains("ASSIGNED -> READY"));

    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 404L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.updateAssignedOrderStatus(9L, 404L, "DELIVERED"));

    CourierAssignedOrderEntity orphanAssignment = new CourierAssignedOrderEntity();
    orphanAssignment.setCourierId(9L);
    orphanAssignment.setOrderId(500L);
    orphanAssignment.setStatus("DELIVERING");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 500L)).willReturn(Optional.of(orphanAssignment));
    given(orderJpaRepository.findById(500L)).willReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.updateAssignedOrderStatus(9L, 500L, "DELIVERED"));
  }
}
