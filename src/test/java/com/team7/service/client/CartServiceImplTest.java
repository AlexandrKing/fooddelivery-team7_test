package com.team7.service.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import com.team7.repository.client.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

  @Mock
  private CartRepository cartRepository;

  private CartServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CartServiceImpl(cartRepository);
  }

  @Test
  void getCartReturnsRepositoryCart() {
    Cart expected = cart(1L, List.of(), 0.0);
    given(cartRepository.getCart(1L)).willReturn(expected);

    Cart result = service.getCart(1L);

    assertEquals(1L, result.getUserId());
    assertEquals(0.0, result.getTotalAmount());
  }

  @Test
  void addItemDelegatesToRepositoryForNewDish() {
    Cart updated = cart(1L, List.of(new CartItem(10L, 100L, 2L, 1, "Burger", 250.0)), 250.0);
    given(cartRepository.addItem(1L, 2L, 100L, 1)).willReturn(updated);

    Cart result = service.addItem(1L, 2L, 100L, 1);

    assertEquals(1, result.getItems().size());
    assertEquals(1, result.getItems().get(0).getQuantity());
  }

  @Test
  void addItemDelegatesToRepositoryForExistingDishQuantityIncrease() {
    Cart updated = cart(1L, List.of(new CartItem(10L, 100L, 2L, 3, "Burger", 250.0)), 750.0);
    given(cartRepository.addItem(1L, 2L, 100L, 2)).willReturn(updated);

    Cart result = service.addItem(1L, 2L, 100L, 2);

    assertEquals(3, result.getItems().get(0).getQuantity());
  }

  @Test
  void updateItemQuantityDelegatesWhenQuantityPositive() {
    Cart updated = cart(1L, List.of(new CartItem(10L, 100L, 2L, 4, "Burger", 250.0)), 1000.0);
    given(cartRepository.updateItemQuantity(1L, 10L, 4)).willReturn(updated);

    Cart result = service.updateItemQuantity(1L, 10L, 4);

    assertEquals(4, result.getItems().get(0).getQuantity());
    verify(cartRepository).updateItemQuantity(1L, 10L, 4);
  }

  @Test
  void updateItemQuantityRemovesItemWhenQuantityZeroOrNegative() {
    Cart updated = cart(1L, List.of(), 0.0);
    given(cartRepository.removeItem(1L, 10L)).willReturn(updated);

    Cart result = service.updateItemQuantity(1L, 10L, 0);

    assertEquals(0, result.getItems().size());
    verify(cartRepository).removeItem(1L, 10L);
  }

  @Test
  void removeItemDelegatesToRepository() {
    Cart updated = cart(1L, List.of(), 0.0);
    given(cartRepository.removeItem(1L, 10L)).willReturn(updated);

    Cart result = service.removeItem(1L, 10L);

    assertEquals(0, result.getItems().size());
  }

  @Test
  void clearCartDelegatesToRepository() {
    Cart updated = cart(1L, List.of(), 0.0);
    given(cartRepository.clearCart(1L)).willReturn(updated);

    Cart result = service.clearCart(1L);

    assertEquals(0.0, result.getTotalAmount());
    verify(cartRepository).clearCart(1L);
  }

  @Test
  void addItemPropagatesDishNotFoundError() {
    given(cartRepository.addItem(1L, 2L, 999L, 1)).willThrow(new IllegalArgumentException("Блюдо не найдено: id=999"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.addItem(1L, 2L, 999L, 1)
    );

    assertEquals("Блюдо не найдено: id=999", ex.getMessage());
  }

  @Test
  void updateItemQuantityPropagatesMissingCartOrItemErrors() {
    given(cartRepository.updateItemQuantity(1L, 404L, 2)).willThrow(new IllegalArgumentException("Элемент корзины не найден"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateItemQuantity(1L, 404L, 2)
    );

    assertEquals("Элемент корзины не найден", ex.getMessage());
  }

  @Test
  void updateItemQuantityWithNullQuantityReturnsControlledDomainError() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateItemQuantity(1L, 10L, null)
    );
    assertEquals("Quantity is required", ex.getMessage());
  }

  private static Cart cart(Long userId, List<CartItem> items, Double totalAmount) {
    Cart cart = new Cart();
    cart.setUserId(userId);
    cart.setItems(items);
    cart.setTotalAmount(totalAmount);
    cart.setRestaurantId(2L);
    return cart;
  }
}
