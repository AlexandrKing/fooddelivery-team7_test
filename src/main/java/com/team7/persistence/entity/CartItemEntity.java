package com.team7.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "cart_id", nullable = false)
  private Long cartId;

  @Column(name = "dish_id", nullable = false)
  private Long dishId;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price_at_time", nullable = false)
  private Double priceAtTime;

  public CartItemEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCartId() {
    return cartId;
  }

  public void setCartId(Long cartId) {
    this.cartId = cartId;
  }

  public Long getDishId() {
    return dishId;
  }

  public void setDishId(Long dishId) {
    this.dishId = dishId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Double getPriceAtTime() {
    return priceAtTime;
  }

  public void setPriceAtTime(Double priceAtTime) {
    this.priceAtTime = priceAtTime;
  }
}
