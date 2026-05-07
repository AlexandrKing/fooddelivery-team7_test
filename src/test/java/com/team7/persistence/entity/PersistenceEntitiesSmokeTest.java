package com.team7.persistence.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceEntitiesSmokeTest {

  @Test
  void entitiesAreConstructibleAndAcceptBasicSetters() {
    // Many entities have protected no-args ctors (JPA style). Use reflection to instantiate.
    UserEntity user = newInstance(UserEntity.class);
    user.setId(1L);
    user.setFullName("Alex");
    user.setEmail("a@test");
    user.setPhone("+7");
    user.setPassword("hash");
    user.setIsActive(true);
    assertEquals("Alex", user.getFullName());

    RestaurantEntity restaurant = newInstance(RestaurantEntity.class);
    restaurant.setId(2L);
    restaurant.setName("R");
    restaurant.setAddress("addr");
    restaurant.setLatitude(55.7);
    restaurant.setLongitude(37.6);
    restaurant.setIsActive(true);
    assertEquals(55.7, restaurant.getLatitude());

    DishEntity dish = newInstance(DishEntity.class);
    dish.setId(3L);
    dish.setRestaurantId(2L);
    dish.setName("Dish");
    dish.setPrice(10.0);
    dish.setIsAvailable(true);
    assertTrue(dish.getIsAvailable());

    CartEntity cart = newInstance(CartEntity.class);
    cart.setId(4L);
    cart.setUserId(1L);
    cart.setRestaurantId(2L);
    cart.setTotalAmount(100.0);
    assertEquals(100.0, cart.getTotalAmount());

    CartItemEntity cartItem = newInstance(CartItemEntity.class);
    cartItem.setId(5L);
    cartItem.setCartId(4L);
    cartItem.setDishId(3L);
    cartItem.setQuantity(2);
    cartItem.setPriceAtTime(10.0);
    assertEquals(2, cartItem.getQuantity());

    OrderEntity order = newInstance(OrderEntity.class);
    order.setId(6L);
    order.setUserId(1L);
    order.setRestaurantId(2L);
    order.setDeliveryAddress("addr");
    order.setDeliveryType("DELIVERY");
    order.setPaymentMethod("CARD");
    order.setStatus("PENDING");
    order.setTotalAmount(200.0);
    order.setCreatedAt(LocalDateTime.now());
    assertEquals("PENDING", order.getStatus());

    OrderItemEntity orderItem = newInstance(OrderItemEntity.class);
    orderItem.setId(7L);
    orderItem.setOrderId(6L);
    orderItem.setDishId(3L);
    orderItem.setName("Dish");
    orderItem.setPrice(10.0);
    orderItem.setQuantity(2);
    assertEquals(2, orderItem.getQuantity());

    OrderStatusHistoryEntity hist = newInstance(OrderStatusHistoryEntity.class);
    hist.setId(8L);
    hist.setOrderId(6L);
    hist.setStatus("PENDING");
    hist.setCreatedAt(LocalDateTime.now());
    assertEquals("PENDING", hist.getStatus());

    CourierUserEntity courier = newInstance(CourierUserEntity.class);
    courier.setId(9L);
    courier.setEmail("c@test");
    courier.setUsername("c");
    courier.setIsActive(true);
    assertEquals("c", courier.getUsername());

    CourierAssignedOrderEntity assigned = newInstance(CourierAssignedOrderEntity.class);
    assigned.setId(10L);
    assigned.setCourierId(9L);
    assigned.setOrderId(6L);
    assigned.setStatus("ASSIGNED");
    assertEquals(9L, assigned.getCourierId());

    ReviewEntity review = newInstance(ReviewEntity.class);
    review.setId(11L);
    review.setOrderId(6L);
    review.setUserId(1L);
    review.setComment("ok");
    review.setCreatedAt(LocalDateTime.now());
    assertEquals("ok", review.getComment());

    AddressEntity addr = newInstance(AddressEntity.class);
    addr.setId(12L);
    addr.setUserId(1L);
    addr.setLabel("home");
    addr.setAddress("street");
    assertEquals("street", addr.getAddress());

    AdminUserEntity admin = newInstance(AdminUserEntity.class);
    admin.setId(13L);
    admin.setEmail("admin@test");
    admin.setUsername("admin");
    admin.setRole("ADMIN");
    assertEquals("ADMIN", admin.getRole());

    AppAccountEntity account = newInstance(AppAccountEntity.class);
    account.setId(14L);
    account.setEmail("u@test");
    account.setPasswordHash("hash");
    account.setRole(AppRole.USER);
    account.setIsActive(true);
    account.setCreatedAt(LocalDateTime.now());
    account.setUpdatedAt(LocalDateTime.now());
    assertEquals(AppRole.USER, account.getRole());
  }

  private static <T> T newInstance(Class<T> type) {
    try {
      Constructor<T> c = type.getDeclaredConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (Exception e) {
      throw new AssertionError("Failed to instantiate " + type.getName(), e);
    }
  }
}

