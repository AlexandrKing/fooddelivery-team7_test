package com.team7.service.courier;

import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.order.OrderStatusTransitionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CourierService {
  private final CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository;
  private final OrderJpaRepository orderJpaRepository;

  public CourierService(
      CourierAssignedOrderJpaRepository courierAssignedOrderJpaRepository,
      OrderJpaRepository orderJpaRepository
  ) {
    this.courierAssignedOrderJpaRepository = courierAssignedOrderJpaRepository;
    this.orderJpaRepository = orderJpaRepository;
  }

  public List<CourierAssignedOrderEntity> getAssignedOrders(Long courierId) {
    return courierAssignedOrderJpaRepository.findByCourierIdOrderByAssignedAtDesc(courierId);
  }

  public List<OrderEntity> getAvailableDeliveryOrders() {
    return orderJpaRepository.findAvailableForCourierAssignment();
  }

  @Transactional
  public CourierAssignedOrderEntity claimOrder(Long courierId, Long orderId) {
    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    if (!"DELIVERY".equalsIgnoreCase(trimUpper(order.getDeliveryType()))) {
      throw new IllegalArgumentException("Заказ не на доставку");
    }
    if (!OrderStatusTransitionPolicy.canClaimFromOrderStatus(order.getStatus())) {
      throw new IllegalArgumentException("Заказ недоступен для назначения");
    }
    if (courierAssignedOrderJpaRepository.existsByOrderId(orderId)) {
      throw new IllegalArgumentException("Заказ уже назначен курьеру");
    }
    CourierAssignedOrderEntity assignment = new CourierAssignedOrderEntity();
    assignment.setCourierId(courierId);
    assignment.setOrderId(orderId);
    assignment.setStatus("ASSIGNED");
    assignment.setAssignedAt(LocalDateTime.now());
    return courierAssignedOrderJpaRepository.save(assignment);
  }

  private static String trimUpper(String s) {
    return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
  }

  @Transactional
  public CourierAssignedOrderEntity updateAssignedOrderStatus(Long courierId, Long orderId, String status) {
    CourierAssignedOrderEntity assignment = courierAssignedOrderJpaRepository.findByCourierIdAndOrderId(courierId, orderId)
        .orElseThrow(() -> new IllegalArgumentException("Assigned order not found"));
    String nextStatus = OrderStatusTransitionPolicy.validateCourierTransition(assignment.getStatus(), status);
    assignment.setStatus(nextStatus);
    if ("PICKED_UP".equals(nextStatus)) {
      assignment.setPickedUpAt(LocalDateTime.now());
    }
    if ("DELIVERED".equals(nextStatus)) {
      assignment.setDeliveryTime(LocalDateTime.now());
    }
    CourierAssignedOrderEntity saved = courierAssignedOrderJpaRepository.save(assignment);

    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    order.setStatus(nextStatus);
    orderJpaRepository.save(order);
    return saved;
  }
}

