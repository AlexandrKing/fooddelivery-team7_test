package com.team7.model.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

  @Test
  void constructorsSetFieldsAndAvailability() {
    Menu m1 = new Menu(1L, 10L, "Burger", "Tasty", 123.45, true);
    assertEquals(1L, m1.getId());
    assertEquals(10L, m1.getRestaurantId());
    assertEquals("Burger", m1.getName());
    assertEquals("Tasty", m1.getDescription());
    assertEquals(123.45, m1.getPrice());
    assertTrue(m1.isAvailable());

    Menu m2 = new Menu(2L, "Pizza", "Cheesy", 50.0);
    assertEquals(2L, m2.getId());
    assertEquals("Pizza", m2.getName());
    assertEquals("Cheesy", m2.getDescription());
    assertEquals(50.0, m2.getPrice());
    assertTrue(m2.isAvailable());
  }

  @Test
  void getIsAvailableAndSetIsAvailableControlIsAvailableLogic() {
    Menu m = new Menu();

    m.setIsAvailable(null);
    assertFalse(m.isAvailable());

    m.setIsAvailable(false);
    assertFalse(m.isAvailable());

    m.setIsAvailable(true);
    assertTrue(m.isAvailable());
    assertEquals(true, m.getIsAvailable());
  }

  @Test
  void builderMethodsMutateAndSupportChaining() {
    Menu m = new Menu(1L, "Burger", "Tasty", 10.0)
        .withRestaurantId(99L)
        .withCategory("Fast")
        .withImageUrl("img.png");

    assertEquals(99L, m.getRestaurantId());
    assertEquals("Fast", m.getCategory());
    assertEquals("img.png", m.getImageUrl());
    assertSame(m, m.withRestaurantId(1L));
  }

  @Test
  void toStringUsesFormattingAndDoesNotThrowWhenPricePresent() {
    Menu m = new Menu(7L, 3L, "Sushi", "Rolls", 12.5, true);
    String s = m.toString();
    assertNotNull(s);
    assertTrue(s.contains("Menu{id=7"));
    assertTrue(s.contains("available=true"));
  }
}

