package com.team7.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "courier_assigned_orders")
public class CourierAssignedOrderEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "courier_id", nullable = false)
  private Long courierId;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "status")
  private String status;

  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;

  @Column(name = "picked_up_at")
  private LocalDateTime pickedUpAt;

  @Column(name = "delivery_time")
  private LocalDateTime deliveryTime;

  @Column(name = "delivery_notes")
  private String deliveryNotes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCourierId() {
    return courierId;
  }

  public void setCourierId(Long courierId) {
    this.courierId = courierId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(LocalDateTime assignedAt) {
    this.assignedAt = assignedAt;
  }

  public LocalDateTime getPickedUpAt() {
    return pickedUpAt;
  }

  public void setPickedUpAt(LocalDateTime pickedUpAt) {
    this.pickedUpAt = pickedUpAt;
  }

  public LocalDateTime getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(LocalDateTime deliveryTime) {
    this.deliveryTime = deliveryTime;
  }

  public String getDeliveryNotes() {
    return deliveryNotes;
  }

  public void setDeliveryNotes(String deliveryNotes) {
    this.deliveryNotes = deliveryNotes;
  }
}

