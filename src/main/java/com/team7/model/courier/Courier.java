package com.team7.model.courier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Courier {
  private Long id;
  private String username;
  private String email;
  private String passwordHash;
  private String fullName;
  private String phone;
  private String vehicleType;
  private String status;
  private Double latitude;
  private Double longitude;
  private BigDecimal rating;
  private Integer completedOrders;
  private BigDecimal balance;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;
  private LocalDateTime lastActivityAt;
  private Boolean isActive;

  public Courier() {
    this.rating = BigDecimal.ZERO;
    this.completedOrders = 0;
    this.balance = BigDecimal.ZERO;
    this.status = "offline";
    this.isActive = true;
  }

  public Courier(Long id, String username, String email, String passwordHash,
                 String fullName, String phone) {
    this();
    this.id = id;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.fullName = fullName;
    this.phone = phone;
    this.createdAt = LocalDateTime.now();
  }

  // Геттеры и сеттеры
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getVehicleType() { return vehicleType; }
  public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Double getLatitude() { return latitude; }
  public void setLatitude(Double latitude) { this.latitude = latitude; }
  public Double getLongitude() { return longitude; }
  public void setLongitude(Double longitude) { this.longitude = longitude; }
  public BigDecimal getRating() { return rating; }
  public void setRating(BigDecimal rating) { this.rating = rating; }
  public Integer getCompletedOrders() { return completedOrders; }
  public void setCompletedOrders(Integer completedOrders) { this.completedOrders = completedOrders; }
  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getLastLoginAt() { return lastLoginAt; }
  public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
  public LocalDateTime getLastActivityAt() { return lastActivityAt; }
  public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
  public Boolean getIsActive() { return isActive; }
  public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}