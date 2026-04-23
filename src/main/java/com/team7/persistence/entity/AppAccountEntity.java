package com.team7.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_accounts")
public class AppAccountEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private AppRole role;

  @Column(name = "linked_user_id")
  private Long linkedUserId;

  @Column(name = "linked_restaurant_id")
  private Long linkedRestaurantId;

  @Column(name = "linked_courier_id")
  private Long linkedCourierId;

  @Column(name = "linked_admin_id")
  private Long linkedAdminId;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public AppAccountEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public AppRole getRole() {
    return role;
  }

  public void setRole(AppRole role) {
    this.role = role;
  }

  public Long getLinkedUserId() {
    return linkedUserId;
  }

  public void setLinkedUserId(Long linkedUserId) {
    this.linkedUserId = linkedUserId;
  }

  public Long getLinkedRestaurantId() {
    return linkedRestaurantId;
  }

  public void setLinkedRestaurantId(Long linkedRestaurantId) {
    this.linkedRestaurantId = linkedRestaurantId;
  }

  public Long getLinkedCourierId() {
    return linkedCourierId;
  }

  public void setLinkedCourierId(Long linkedCourierId) {
    this.linkedCourierId = linkedCourierId;
  }

  public Long getLinkedAdminId() {
    return linkedAdminId;
  }

  public void setLinkedAdminId(Long linkedAdminId) {
    this.linkedAdminId = linkedAdminId;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean active) {
    isActive = active;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

