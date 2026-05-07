package com.team7.service.courier;

import com.team7.config.AppProperties;
import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.CourierTransactionJpaRepository;
import com.team7.persistence.CourierUserJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.CourierTransactionEntity;
import com.team7.persistence.entity.CourierUserEntity;
import com.team7.persistence.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

  @Mock
  private CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  @Mock
  private OrderJpaRepository orderJpaRepository;
  @Mock
  private CourierUserJpaRepository courierUserJpaRepository;
  @Mock
  private CourierTransactionJpaRepository courierTransactionJpaRepository;

  private CourierService service;
  private AppProperties appProperties;

  @BeforeEach
  void setUp() {
    appProperties = new AppProperties();
    service = new CourierService(
        courierAssignedOrderJpaRepository,
        orderJpaRepository,
        courierUserJpaRepository,
        courierTransactionJpaRepository,
        appProperties
    );
  }

  @Test
  void deliveryFeeComesFromAppProperties() {
    appProperties.setDeliveryFee(BigDecimal.valueOf(123.45));
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setId(1L);
    assignment.setCourierId(9L);
    assignment.setOrderId(100L);
    assignment.setStatus("DELIVERING");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 100L)).willReturn(Optional.of(assignment));
    given(courierAssignedOrderJpaRepository.save(any(CourierAssignedOrderEntity.class))).willAnswer(a -> a.getArgument(0));
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setStatus("DELIVERING");
    given(orderJpaRepository.findById(100L)).willReturn(Optional.of(order));
    given(orderJpaRepository.save(any(OrderEntity.class))).willAnswer(a -> a.getArgument(0));
    CourierUserEntity courier = new CourierUserEntity();
    courier.setId(9L);
    courier.setBalance(BigDecimal.ZERO);
    given(courierTransactionJpaRepository.existsByOrderId(100L)).willReturn(false);
    given(courierUserJpaRepository.findById(9L)).willReturn(Optional.of(courier));

    service.updateAssignedOrderStatus(9L, 100L, "DELIVERED");

    assertEquals(0, BigDecimal.valueOf(123.45).compareTo(courier.getBalance()));
  }

  @Test
  void listMethodsReturnRepositoryData() {
    given(courierAssignedOrderJpaRepository.findByCourierIdOrderByAssignedAtDesc(9L)).willReturn(List.of());
    given(orderJpaRepository.findAvailableForCourierAssignment()).willReturn(List.of());
    assertEquals(0, service.getAssignedOrders(9L).size());
    assertEquals(0, service.getAvailableDeliveryOrders().size());
  }

  @Test
  void balanceTransactionsAndStatsUseRepositoryValuesAndFallbacks() {
    CourierUserEntity courier = new CourierUserEntity();
    courier.setBalance(BigDecimal.valueOf(300));
    given(courierUserJpaRepository.findById(9L)).willReturn(Optional.of(courier));
    given(courierUserJpaRepository.findById(404L)).willReturn(Optional.empty());

    CourierTransactionEntity transaction = new CourierTransactionEntity();
    transaction.setAmount(BigDecimal.valueOf(100));
    given(courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(9L)).willReturn(List.of(transaction));
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<CourierTransactionEntity> page = new PageImpl<>(List.of(transaction), pageRequest, 1);
    given(courierTransactionJpaRepository.findByCourierIdOrderByCreatedAtDesc(9L, pageRequest)).willReturn(page);
    given(courierTransactionJpaRepository.sumAmountByCourierIdSince(any(Long.class), any())).willReturn(null);

    assertEquals(0, BigDecimal.valueOf(300).compareTo(service.getBalance(9L)));
    assertEquals(0, BigDecimal.ZERO.compareTo(service.getBalance(404L)));
    assertEquals(1, service.getTransactions(9L).size());
    assertEquals(1, service.getTransactions(9L, pageRequest).getTotalElements());

    CourierService.CourierStats stats = service.getStats(9L);
    assertEquals(0, BigDecimal.valueOf(300).compareTo(stats.balance()));
    assertEquals(0, BigDecimal.ZERO.compareTo(stats.earnedToday()));
    assertEquals(0, BigDecimal.ZERO.compareTo(stats.earnedThisWeek()));
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
    CourierUserEntity courier = new CourierUserEntity();
    courier.setId(9L);
    courier.setBalance(BigDecimal.ZERO);
    given(courierTransactionJpaRepository.existsByOrderId(100L)).willReturn(false);
    given(courierUserJpaRepository.findById(9L)).willReturn(Optional.of(courier));

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

  @Test
  void deliveredToDeliveredReturnsAssignmentWithoutSavingOrAccruingAgain() {
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setCourierId(9L);
    assignment.setOrderId(100L);
    assignment.setStatus("DELIVERED");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 100L)).willReturn(Optional.of(assignment));

    CourierAssignedOrderEntity result = service.updateAssignedOrderStatus(9L, 100L, "DELIVERED");

    assertEquals(assignment, result);
    verify(courierAssignedOrderJpaRepository, never()).save(any());
    verify(orderJpaRepository, never()).findById(any());
    verify(courierTransactionJpaRepository, never()).existsByOrderId(any());
  }

  @Test
  void deliveredAccrualSkipsExistingTransactionAndFailsWhenCourierMissing() {
    CourierAssignedOrderEntity assigned = new CourierAssignedOrderEntity();
    assigned.setCourierId(9L);
    assigned.setOrderId(100L);
    assigned.setStatus("DELIVERING");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 100L)).willReturn(Optional.of(assigned));
    given(courierAssignedOrderJpaRepository.save(any(CourierAssignedOrderEntity.class))).willAnswer(a -> a.getArgument(0));
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setStatus("DELIVERING");
    given(orderJpaRepository.findById(100L)).willReturn(Optional.of(order));
    given(orderJpaRepository.save(any(OrderEntity.class))).willAnswer(a -> a.getArgument(0));
    given(courierTransactionJpaRepository.existsByOrderId(100L)).willReturn(true);

    service.updateAssignedOrderStatus(9L, 100L, "DELIVERED");

    verify(courierUserJpaRepository, never()).save(any());
    verify(courierTransactionJpaRepository, never()).save(any());

    CourierAssignedOrderEntity missingCourierAssignment = new CourierAssignedOrderEntity();
    missingCourierAssignment.setCourierId(9L);
    missingCourierAssignment.setOrderId(101L);
    missingCourierAssignment.setStatus("DELIVERING");
    given(courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(9L, 101L))
        .willReturn(Optional.of(missingCourierAssignment));
    OrderEntity secondOrder = new OrderEntity();
    secondOrder.setId(101L);
    secondOrder.setStatus("DELIVERING");
    given(orderJpaRepository.findById(101L)).willReturn(Optional.of(secondOrder));
    given(courierTransactionJpaRepository.existsByOrderId(101L)).willReturn(false);
    given(courierUserJpaRepository.findById(9L)).willReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.updateAssignedOrderStatus(9L, 101L, "DELIVERED"));
  }
}
