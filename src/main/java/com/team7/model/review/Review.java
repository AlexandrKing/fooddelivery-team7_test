package com.team7.model.review;

import java.time.LocalDateTime;

public class Review {
  private Long id;
  private Long orderId;
  private Long userId;
  private Long restaurantId;
  private Long courierId;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
  private Boolean isActive;

  public Review() {
    this.isActive = true;
    this.createdAt = LocalDateTime.now();
  }

  // Геттеры и сеттеры
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getRestaurantId() { return restaurantId; }
  public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
  public Long getCourierId() { return courierId; }
  public void setCourierId(Long courierId) { this.courierId = courierId; }
  public Integer getRating() { return rating; }
  public void setRating(Integer rating) { this.rating = rating; }
  public String getComment() { return comment; }
  public void setComment(String comment) { this.comment = comment; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public Boolean getIsActive() { return isActive; }
  public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}