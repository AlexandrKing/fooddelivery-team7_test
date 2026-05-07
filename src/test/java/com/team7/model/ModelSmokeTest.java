package com.team7.model;

import com.team7.model.client.*;
import com.team7.model.courier.AssignedOrder;
import com.team7.model.courier.Courier;
import com.team7.model.review.Review;
import com.team7.model.restaurant.Dish;
import com.team7.model.restaurant.MenuCategory;
import com.team7.model.restaurant.Restaurant;
import com.team7.model.admin.Admin;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelSmokeTest {

  @Test
  void clientModelsGettersSettersAndEnums() {
    com.team7.model.client.Address a = new com.team7.model.client.Address();
    a.setId(1L);
    a.setLabel("home");
    a.setAddress("street");
    a.setApartment("12");
    assertEquals(1L, a.getId());
    assertEquals("home", a.getLabel());

    User u = new User();
    u.setId(2L);
    u.setName("Alex");
    u.setEmail("a@test");
    u.setPhone("+7");
    u.setPassword("hash");
    u.setRole(UserRole.CLIENT);
    u.setAddresses(List.of(a));
    assertEquals(UserRole.CLIENT, u.getRole());
    assertEquals(1, u.getAddresses().size());
    assertNotNull(u.toString());
    assertEquals(u, u);
    assertEquals(u.hashCode(), u.hashCode());

    CartItem ci = new CartItem(1L, 10L, 2L, 3, "Burger", 100.0);
    assertEquals(3, ci.getQuantity());
    assertEquals(100.0, ci.getPrice());

    Cart c = new Cart();
    c.setId(3L);
    c.setUserId(2L);
    c.setRestaurantId(9L);
    c.setTotalAmount(300.0);
    c.setItems(List.of(ci));
    assertEquals(300.0, c.getTotalAmount());
    assertNotNull(c.toString());

    OrderItem oi = new OrderItem(1L, 10L, "Burger", 100.0, 3);
    assertEquals("Burger", oi.getName());

    Order o = new Order();
    o.setId(4L);
    o.setUserId(2L);
    o.setRestaurantId(9L);
    o.setStatus(OrderStatus.PENDING);
    o.setDeliveryAddress("street");
    o.setDeliveryType(DeliveryType.DELIVERY);
    o.setPaymentMethod(PaymentMethod.CARD);
    o.setPreferredDeliveryTime(LocalDateTime.now());
    o.setTotalAmount(300.0);
    o.setItems(List.of(oi));
    assertEquals(OrderStatus.PENDING, o.getStatus());
    assertEquals(1, o.getItems().size());
    assertNotNull(o.toString());

    com.team7.model.client.Restaurant cr = new com.team7.model.client.Restaurant();
    cr.setId(9L);
    cr.setName("R");
    cr.setLatitude(55.7);
    cr.setLongitude(37.6);
    assertEquals(55.7, cr.getLatitude());

    Menu m = new Menu();
    m.setId(10L);
    m.setRestaurantId(9L);
    m.setName("Burger");
    m.setPrice(100.0);
    m.setAvailable(true);
    m.setCookingTime(15);
    assertTrue(m.getAvailable());

    // enums
    assertEquals(OrderStatus.PENDING, OrderStatus.fromString("pending"));
    assertNull(OrderStatus.fromString("unknown_status"));
    assertEquals(PaymentMethod.CARD, PaymentMethod.valueOf("CARD"));
    assertEquals(DeliveryType.DELIVERY, DeliveryType.valueOf("DELIVERY"));
  }

  @Test
  void restaurantAndCourierModelsBasicSmoke() {
    Restaurant r = new Restaurant(1L, "R", "r@test", "hash", "+7", "addr", "Any");
    assertEquals("PENDING", r.getStatus());
    assertTrue(r.getIsActive());
    assertNotNull(r.getCreatedAt());
    assertNotNull(r.getUpdatedAt());
    assertNotNull(r.getMenu());
    assertNotNull(r.getMenuCategories());
    r.setLatitude(55.7);
    r.setLongitude(37.6);
    assertEquals(55.7, r.getLatitude());

    Dish d = new Dish(2L, "Dish", "Desc", java.math.BigDecimal.valueOf(10.0), "Cat", 1L);
    assertTrue(d.getAvailable());
    assertEquals(15, d.getPreparationTimeMin());
    d.setSpicy(true);
    d.setVegetarian(true);
    assertTrue(d.getSpicy());
    assertTrue(d.getVegetarian());

    MenuCategory cat = new MenuCategory();
    cat.setId(3L);
    cat.setRestaurantId(1L);
    cat.setName("Main");
    assertEquals("Main", cat.getName());

    Courier courier = new Courier(7L, "c", "c@test", "hash", "Courier", "+7");
    assertEquals("offline", courier.getStatus());
    assertNotNull(courier.getCreatedAt());
    assertEquals(java.math.BigDecimal.ZERO, courier.getRating());
    assertEquals(0, courier.getCompletedOrders());
    assertEquals(java.math.BigDecimal.ZERO, courier.getBalance());
    assertTrue(courier.getIsActive());
    courier.setLatitude(55.7);
    courier.setLongitude(37.6);
    courier.setVehicleType("bike");
    courier.setLastLoginAt(LocalDateTime.now());
    courier.setLastActivityAt(LocalDateTime.now());
    courier.setRating(java.math.BigDecimal.valueOf(4.2));
    courier.setCompletedOrders(10);
    courier.setBalance(java.math.BigDecimal.valueOf(100));
    assertEquals("bike", courier.getVehicleType());
    assertEquals(10, courier.getCompletedOrders());

    AssignedOrder ao = new AssignedOrder();
    ao.setId(1L);
    ao.setOrderId(100L);
    ao.setCourierId(7L);
    ao.setRestaurantName("R");
    ao.setCourierName("C");
    ao.setDeliveryAddress("addr");
    ao.setOrderAmount(java.math.BigDecimal.valueOf(200));
    ao.setStatus("ASSIGNED");
    assertEquals("ASSIGNED", ao.getStatus());
    assertEquals("addr", ao.getDeliveryAddress());

    Admin admin = new Admin(1L, "admin", "a@test", "hash", "Admin", "ADMIN");
    assertTrue(admin.getIsActive());
    assertNotNull(admin.getCreatedAt());
    admin.setPermissions("ALL");
    assertEquals("ALL", admin.getPermissions());
    admin.setLastLoginAt(LocalDateTime.now());
    assertNotNull(admin.getLastLoginAt());

    Review review = new Review();
    assertTrue(review.getIsActive());
    assertNotNull(review.getCreatedAt());
    review.setId(1L);
    review.setOrderId(2L);
    review.setUserId(3L);
    review.setRestaurantId(4L);
    review.setCourierId(5L);
    review.setRating(5);
    review.setComment("ok");
    assertEquals(5, review.getRating());

    com.team7.model.restaurant.Address restaurantAddress =
        new com.team7.model.restaurant.Address("Street", "City", "000000");
    assertEquals("City", restaurantAddress.getCity());
  }
}

