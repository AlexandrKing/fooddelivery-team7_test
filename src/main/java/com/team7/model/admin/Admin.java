package com.team7.model.admin;

import java.time.LocalDateTime;

public class Admin {
  private Long id;
  private String username;
  private String email;
  private String passwordHash;
  private String fullName;
  private String role;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;
  private Boolean isActive;
  private String permissions;

  public Admin() {}

  public Admin(Long id, String username, String email, String passwordHash,
               String fullName, String role) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.fullName = fullName;
    this.role = role;
    this.isActive = true;
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
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getLastLoginAt() { return lastLoginAt; }
  public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
  public Boolean getIsActive() { return isActive; }
  public void setIsActive(Boolean isActive) { this.isActive = isActive; }
  public String getPermissions() { return permissions; }
  public void setPermissions(String permissions) { this.permissions = permissions; }
}