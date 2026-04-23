package com.team7.service.courier;

import com.team7.persistence.CourierAssignedOrderJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.CourierAssignedOrderEntity;
import com.team7.persistence.entity.OrderEntity;
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
    String st = trimUpper(order.getStatus());
    if (!"PENDING".equals(st) && !"PREPARING".equals(st) && !"READY".equals(st)) {
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
    assignment.setStatus(status);
    if ("PICKED_UP".equalsIgnoreCase(status)) {
      assignment.setPickedUpAt(LocalDateTime.now());
    }
    if ("DELIVERED".equalsIgnoreCase(status)) {
      assignment.setDeliveryTime(LocalDateTime.now());
    }
    CourierAssignedOrderEntity saved = courierAssignedOrderJpaRepository.save(assignment);

    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    order.setStatus(status.toUpperCase());
    orderJpaRepository.save(order);
    return saved;
  }
}

