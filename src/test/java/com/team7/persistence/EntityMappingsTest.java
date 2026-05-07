package com.team7.persistence;

import com.team7.model.client.Address;
import com.team7.model.client.Menu;
import com.team7.model.client.Review;
import com.team7.model.client.User;
import com.team7.persistence.entity.AddressEntity;
import com.team7.persistence.entity.DishEntity;
import com.team7.persistence.entity.ReviewEntity;
import com.team7.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityMappingsTest {

  @Test
  void dishEntityMappingsMapsNullDefaults() {
    DishEntity e = new DishEntity();
    e.setId(1L);
    e.setRestaurantId(2L);
    e.setName("Burger");
    e.setDescription("Desc");
    e.setPrice(null);
    e.setIsAvailable(null);
    e.setCategory("Fast");
    e.setCalories(700);
    e.setImageUrl("img");
    e.setPreparationTimeMin(15);

    Menu m = DishEntityMappings.toMenu(e);
    assertEquals(1L, m.getId());
    assertEquals(2L, m.getRestaurantId());
    assertEquals("Burger", m.getName());
    assertEquals("Desc", m.getDescription());
    assertEquals(0.0, m.getPrice());
    assertTrue(m.getAvailable());
    assertEquals("Fast", m.getCategory());
    assertEquals(700, m.getCalories());
    assertEquals("img", m.getImageUrl());
    assertEquals(15, m.getCookingTime());
  }

  @Test
  void addressEntityMappingsMapsFields() {
    AddressEntity e = new AddressEntity();
    e.setId(10L);
    e.setLabel("home");
    e.setAddress("Street 1");
    e.setApartment("12");

    Address a = AddressEntityMappings.toDto(e);
    assertEquals(10L, a.getId());
    assertEquals("home", a.getLabel());
    assertEquals("Street 1", a.getAddress());
    assertEquals("12", a.getApartment());
  }

  @Test
  void userEntityMappingsMapsFieldsAndClearsRole() {
    UserEntity e = new UserEntity();
    e.setId(5L);
    e.setFullName("Alex");
    e.setEmail("a@test");
    e.setPhone("+7");
    e.setPassword("hash");

    User u = UserEntityMappings.toClientUser(e);
    assertEquals(5L, u.getId());
    assertEquals("Alex", u.getName());
    assertEquals("a@test", u.getEmail());
    assertEquals("+7", u.getPhone());
    assertEquals("hash", u.getPassword());
    assertNull(u.getRole());
  }

  @Test
  void reviewEntityMappingsMapsAllFields() {
    ReviewEntity e = new ReviewEntity();
    e.setId(1L);
    e.setOrderId(2L);
    e.setUserId(3L);
    e.setRestaurantId(4L);
    e.setCourierId(5L);
    e.setRestaurantRating(4);
    e.setCourierRating(5);
    e.setComment("ok");
    LocalDateTime ts = LocalDateTime.now();
    e.setCreatedAt(ts);

    Review r = ReviewEntityMappings.toDto(e);
    assertEquals(1L, r.getId());
    assertEquals(2L, r.getOrderId());
    assertEquals(3L, r.getUserId());
    assertEquals(4L, r.getRestaurantId());
    assertEquals(5L, r.getCourierId());
    assertEquals(4, r.getRestaurantRating());
    assertEquals(5, r.getCourierRating());
    assertEquals("ok", r.getComment());
    assertEquals(ts, r.getCreatedAt());
  }
}

