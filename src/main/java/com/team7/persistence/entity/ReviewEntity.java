package com.team7.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "restaurant_id")
  private Long restaurantId;

  @Column(name = "courier_id")
  private Long courierId;

  @Column(name = "restaurant_rating")
  private Integer restaurantRating;

  @Column(name = "courier_rating")
  private Integer courierRating;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public ReviewEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(Long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public Long getCourierId() {
    return courierId;
  }

  public void setCourierId(Long courierId) {
    this.courierId = courierId;
  }

  public Integer getRestaurantRating() {
    return restaurantRating;
  }

  public void setRestaurantRating(Integer restaurantRating) {
    this.restaurantRating = restaurantRating;
  }

  public Integer getCourierRating() {
    return courierRating;
  }

  public void setCourierRating(Integer courierRating) {
    this.courierRating = courierRating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
