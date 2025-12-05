package com.team7.model.courier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AssignedOrder {
  private Long id;
  private Long courierId;
  private Long orderId;
  private LocalDateTime assignedAt;
  private LocalDateTime pickedUpAt;
  private LocalDateTime deliveredAt;
  private String status;
  private String deliveryNotes;
  private BigDecimal orderAmount;
  private String deliveryAddress;
  private String restaurantName;
  private String courierName;

  public AssignedOrder() {}

  // Геттеры и сеттеры
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourierId() { return courierId; }
  public void setCourierId(Long courierId) { this.courierId = courierId; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public LocalDateTime getAssignedAt() { return assignedAt; }
  public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
  public LocalDateTime getPickedUpAt() { return pickedUpAt; }
  public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
  public LocalDateTime getDeliveredAt() { return deliveredAt; }
  public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getDeliveryNotes() { return deliveryNotes; }
  public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
  public BigDecimal getOrderAmount() { return orderAmount; }
  public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
  public String getDeliveryAddress() { return deliveryAddress; }
  public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
  public String getRestaurantName() { return restaurantName; }
  public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
  public String getCourierName() { return courierName; }
  public void setCourierName(String courierName) { this.courierName = courierName; }
}