package com.team7.repository.client;

import com.team7.model.client.CartItem;
import com.team7.model.client.DeliveryType;
import com.team7.model.client.Order;
import com.team7.model.client.OrderItem;
import com.team7.model.client.OrderStatus;
import com.team7.model.client.PaymentMethod;
import com.team7.persistence.OrderItemJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.OrderStatusHistoryJpaRepository;
import com.team7.persistence.entity.OrderEntity;
import com.team7.persistence.entity.OrderItemEntity;
import com.team7.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Repository
public class OrderRepository {
  private final TransactionTemplate txTemplate;
  private final OrderJpaRepository orderJpaRepository;
  private final OrderItemJpaRepository orderItemJpaRepository;
  private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

  public OrderRepository(
      PlatformTransactionManager transactionManager,
      OrderJpaRepository orderJpaRepository,
      OrderItemJpaRepository orderItemJpaRepository,
      OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository
  ) {
    this.txTemplate = new TransactionTemplate(requireNonNull(transactionManager));
    this.orderJpaRepository = requireNonNull(orderJpaRepository);
    this.orderItemJpaRepository = requireNonNull(orderItemJpaRepository);
    this.orderStatusHistoryJpaRepository = requireNonNull(orderStatusHistoryJpaRepository);
  }

  public OrderCreationResult createOrder(
      Long userId,
      Long restaurantId,
      String deliveryAddress,
      DeliveryType deliveryType,
      LocalDateTime deliveryTime,
      PaymentMethod paymentMethod,
      Double totalAmount,
      List<CartItem> items
  ) {
    return txTemplate.execute(status -> createOrderJpa(
        userId, restaurantId, deliveryAddress, deliveryType, deliveryTime, paymentMethod, totalAmount, items
    ));
  }

  public Order getOrder(Long orderId) {
    return getOrderJpa(orderId);
  }

  public List<Order> getUserOrders(Long userId) {
    return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toClientOrderWithoutItems)
        .collect(Collectors.toList());
  }

  public Order cancelOrder(Long orderId) {
    return txTemplate.execute(status -> cancelOrderJpa(orderId));
  }

  // --- JPA ---

  private OrderCreationResult createOrderJpa(
      Long userId,
      Long restaurantId,
      String deliveryAddress,
      DeliveryType deliveryType,
      LocalDateTime deliveryTime,
      PaymentMethod paymentMethod,
      Double totalAmount,
      List<CartItem> items
  ) {
    LocalDateTime createdAt = LocalDateTime.now();
    OrderEntity oe = new OrderEntity();
    oe.setUserId(userId);
    oe.setRestaurantId(restaurantId);
    oe.setDeliveryAddress(deliveryAddress);
    oe.setDeliveryType(deliveryType.toString());
    oe.setDeliveryTime(deliveryTime);
    oe.setPaymentMethod(paymentMethod.toString());
    oe.setStatus(OrderStatus.PENDING.toString());
    oe.setTotalAmount(totalAmount);
    oe.setCreatedAt(createdAt);
    OrderEntity saved = orderJpaRepository.save(oe);
    Long orderId = saved.getId();

    List<OrderItemEntity> lineEntities = new ArrayList<>();
    for (CartItem ci : items) {
      OrderItemEntity oi = new OrderItemEntity();
      oi.setOrderId(orderId);
      oi.setDishId(ci.getMenuItemId());
      oi.setName(ci.getName());
      oi.setPrice(ci.getPrice());
      oi.setQuantity(ci.getQuantity());
      lineEntities.add(oi);
    }
    orderItemJpaRepository.saveAll(lineEntities);

    OrderStatusHistoryEntity hist = new OrderStatusHistoryEntity();
    hist.setOrderId(orderId);
    hist.setStatus(OrderStatus.PENDING.toString());
    hist.setCreatedAt(LocalDateTime.now());
    orderStatusHistoryJpaRepository.save(hist);

    return new OrderCreationResult(orderId, saved.getCreatedAt());
  }

  private Order getOrderJpa(Long orderId) {
    OrderEntity e = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    Order order = toClientOrderWithoutItems(e);
    order.setItems(toClientOrderItems(orderItemJpaRepository.findByOrderIdOrderByIdAsc(orderId)));
    return order;
  }

  private Order cancelOrderJpa(Long orderId) {
    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Нельзя отменить заказ в текущем статусе"));
    if (!OrderStatus.PENDING.toString().equals(order.getStatus())) {
      throw new IllegalArgumentException("Нельзя отменить заказ в текущем статусе");
    }
    order.setStatus(OrderStatus.CANCELLED.toString());
    orderJpaRepository.save(order);

    OrderStatusHistoryEntity hist = new OrderStatusHistoryEntity();
    hist.setOrderId(orderId);
    hist.setStatus(OrderStatus.CANCELLED.toString());
    hist.setCreatedAt(LocalDateTime.now());
    orderStatusHistoryJpaRepository.save(hist);

    return getOrderJpa(orderId);
  }

  private Order toClientOrderWithoutItems(OrderEntity e) {
    Order o = new Order();
    o.setId(e.getId());
    o.setUserId(e.getUserId());
    o.setRestaurantId(e.getRestaurantId());
    o.setStatus(parseOrderStatus(e.getStatus()));
    o.setDeliveryAddress(e.getDeliveryAddress());
    o.setDeliveryType(DeliveryType.valueOf(e.getDeliveryType()));
    o.setPaymentMethod(PaymentMethod.valueOf(e.getPaymentMethod()));
    o.setPreferredDeliveryTime(e.getDeliveryTime());
    o.setTotalAmount(e.getTotalAmount());
    o.setCreatedAt(e.getCreatedAt());
    o.setItems(null);
    return o;
  }

  private List<OrderItem> toClientOrderItems(List<OrderItemEntity> lines) {
    if (lines == null || lines.isEmpty()) {
      return Collections.emptyList();
    }
    return lines.stream().map(line -> {
      OrderItem item = new OrderItem();
      item.setId(line.getId());
      item.setMenuItemId(line.getDishId());
      item.setName(line.getName());
      item.setPrice(line.getPrice());
      item.setQuantity(line.getQuantity());
      return item;
    }).collect(Collectors.toList());
  }

  /**
   * Maps DB status string to client enum. Courier and restaurant flows may persist values
   * not present in older enum definitions; aliases and fallbacks keep user history readable.
   */
  private static OrderStatus parseOrderStatus(String raw) {
    if (raw == null || raw.isBlank()) {
      return OrderStatus.PENDING;
    }
    String u = raw.trim().toUpperCase(Locale.ROOT);
    if ("ON_THE_WAY".equals(u)) {
      return OrderStatus.IN_DELIVERY;
    }
    OrderStatus s = OrderStatus.fromString(u);
    if (s != null) {
      return s;
    }
    return OrderStatus.DELIVERING;
  }

  public static final class OrderCreationResult {
    private final Long orderId;
    private final LocalDateTime createdAt;

    public OrderCreationResult(Long orderId, LocalDateTime createdAt) {
      this.orderId = orderId;
      this.createdAt = createdAt;
    }

    public Long getOrderId() {
      return orderId;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }
  }
}
