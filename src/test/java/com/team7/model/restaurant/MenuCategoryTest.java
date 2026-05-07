package com.team7.model.restaurant;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MenuCategoryTest {

  @Test
  void constructorsInitializeCreatedAtAndDishesCollection() {
    MenuCategory c1 = new MenuCategory(1L, "Fast", "Quick", 10L);
    assertNotNull(c1.getCreatedAt());
    assertEquals(0, c1.getDishes().size());
    assertEquals(10L, c1.getRestaurantId());

    MenuCategory c2 = new MenuCategory();
    assertNotNull(c2.getDishes());
  }

  @Test
  void addDishAppendsToCollection() {
    MenuCategory c = new MenuCategory();
    Dish d = new Dish();
    d.setId(5L);
    d.setRestaurantId(1L);
    d.setName("Burger");
    d.setPrice(java.math.BigDecimal.TEN);
    d.setIsAvailable(true);

    c.addDish(d);
    assertEquals(1, c.getDishes().size());
    assertEquals(5L, c.getDishes().get(0).getId());
  }

  @Test
  void removeDishRemovesMatchingIdAndKeepsOthers() {
    MenuCategory c = new MenuCategory();

    Dish keep = new Dish();
    keep.setId(1L);
    keep.setPrice(java.math.BigDecimal.TEN);
    keep.setIsAvailable(true);

    Dish remove = new Dish();
    remove.setId(2L);
    remove.setPrice(java.math.BigDecimal.ONE);
    remove.setIsAvailable(true);

    c.addDish(keep);
    c.addDish(remove);

    c.removeDish(2L);
    assertEquals(1, c.getDishes().size());
    assertEquals(1L, c.getDishes().get(0).getId());
  }

  @Test
  void removeDishDoesNothingWhenDishIdNotFound() {
    MenuCategory c = new MenuCategory();
    Dish d = new Dish();
    d.setId(1L);
    d.setPrice(java.math.BigDecimal.TEN);
    d.setIsAvailable(true);
    c.addDish(d);

    c.removeDish(999L);
    assertEquals(1, c.getDishes().size());
  }

  @Test
  void removeDishHandlesEmptyDishesCollection() {
    MenuCategory c = new MenuCategory();
    assertNotNull(c.getDishes());
    assertDoesNotThrow(() -> c.removeDish(1L));
    assertTrue(c.getDishes().isEmpty());
  }
}

