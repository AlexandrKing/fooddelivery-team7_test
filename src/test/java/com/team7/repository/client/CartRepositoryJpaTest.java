package com.team7.repository.client;

import com.team7.persistence.CartItemJpaRepository;
import com.team7.persistence.CartJpaRepository;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.entity.DishEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CartRepository.class)
@ActiveProfiles("test")
class CartRepositoryJpaTest {

  @Autowired
  private CartRepository repo;

  @Autowired
  private CartJpaRepository cartJpaRepository;
  @Autowired
  private CartItemJpaRepository cartItemJpaRepository;
  @Autowired
  private DishJpaRepository dishJpaRepository;

  @Test
  void getCartReturnsEmptyWhenNoCartExists() {
    var cart = repo.getCart(1L);
    assertEquals(1L, cart.getUserId());
    assertNotNull(cart.getItems());
    assertEquals(0, cart.getItems().size());
    assertEquals(0.0, cart.getTotalAmount());
  }

  @Test
  void addItemCreatesCartAndAddsNewLineAndUpdatesTotal() {
    DishEntity dish = new DishEntity();
    dish.setRestaurantId(10L);
    dish.setName("Burger");
    dish.setPrice(100.0);
    dish.setIsAvailable(true);
    DishEntity savedDish = dishJpaRepository.save(dish);

    var cart = repo.addItem(1L, 10L, savedDish.getId(), 2);
    assertEquals(1L, cart.getUserId());
    assertEquals(10L, cart.getRestaurantId());
    assertEquals(1, cart.getItems().size());
    assertEquals(200.0, cart.getTotalAmount());
    assertEquals("Burger", cart.getItems().get(0).getName());
  }

  @Test
  void addItemIncrementsQuantityWhenDishAlreadyInCart() {
    DishEntity dish = new DishEntity();
    dish.setRestaurantId(10L);
    dish.setName("Burger");
    dish.setPrice(50.0);
    dish.setIsAvailable(true);
    DishEntity savedDish = dishJpaRepository.save(dish);

    repo.addItem(1L, 10L, savedDish.getId(), 1);
    var cart = repo.addItem(1L, 10L, savedDish.getId(), 2);
    assertEquals(1, cart.getItems().size());
    assertEquals(3, cart.getItems().get(0).getQuantity());
    assertEquals(150.0, cart.getTotalAmount());
  }

  @Test
  void addItemThrowsOnInvalidQuantityOrMissingDish() {
    assertThrows(IllegalArgumentException.class, () -> repo.addItem(1L, 10L, 1L, 0));
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> repo.addItem(1L, 10L, 999L, 1)
    );
    assertTrue(ex.getMessage().contains("Блюдо не найдено"));
  }

  @Test
  void updateRemoveAndClearCoverNotFoundAndHappyPaths() {
    DishEntity dish = new DishEntity();
    dish.setRestaurantId(10L);
    dish.setName("Burger");
    dish.setPrice(10.0);
    dish.setIsAvailable(true);
    DishEntity savedDish = dishJpaRepository.save(dish);

    var cart = repo.addItem(1L, 10L, savedDish.getId(), 1);
    Long itemId = cart.getItems().get(0).getId();

    var updated = repo.updateItemQuantity(1L, itemId, 3);
    assertEquals(3, updated.getItems().get(0).getQuantity());
    assertEquals(30.0, updated.getTotalAmount());

    var removed = repo.removeItem(1L, itemId);
    assertEquals(0, removed.getItems().size());
    assertEquals(0.0, removed.getTotalAmount());

    var cleared = repo.clearCart(1L);
    assertEquals(0, cleared.getItems().size());

    assertThrows(IllegalArgumentException.class, () -> repo.updateItemQuantity(2L, 1L, 1));
    assertThrows(IllegalArgumentException.class, () -> repo.removeItem(2L, 1L));
  }
}

