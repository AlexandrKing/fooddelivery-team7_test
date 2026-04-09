package com.team7.repository.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import com.team7.persistence.CartItemJpaRepository;
import com.team7.persistence.CartJpaRepository;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.entity.CartEntity;
import com.team7.persistence.entity.CartItemEntity;
import com.team7.persistence.entity.DishEntity;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Repository
public class CartRepository {
  private final TransactionTemplate txTemplate;
  private final CartJpaRepository cartJpaRepository;
  private final CartItemJpaRepository cartItemJpaRepository;
  private final DishJpaRepository dishJpaRepository;

  public CartRepository(
      PlatformTransactionManager transactionManager,
      CartJpaRepository cartJpaRepository,
      CartItemJpaRepository cartItemJpaRepository,
      DishJpaRepository dishJpaRepository
  ) {
    this.txTemplate = new TransactionTemplate(requireNonNull(transactionManager));
    this.cartJpaRepository = requireNonNull(cartJpaRepository);
    this.cartItemJpaRepository = requireNonNull(cartItemJpaRepository);
    this.dishJpaRepository = requireNonNull(dishJpaRepository);
  }

  public Cart getCart(Long userId) {
    return getCartJpa(userId);
  }

  public Cart addItem(Long userId, Long restaurantId, Long dishId, Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("Количество должно быть больше 0");
    }
    return txTemplate.execute(status -> addItemJpa(userId, restaurantId, dishId, quantity));
  }

  public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("Количество должно быть больше 0");
    }
    return txTemplate.execute(status -> updateItemQuantityJpa(userId, itemId, quantity));
  }

  public Cart removeItem(Long userId, Long itemId) {
    return txTemplate.execute(status -> removeItemJpa(userId, itemId));
  }

  public Cart clearCart(Long userId) {
    return txTemplate.execute(status -> clearCartJpa(userId));
  }

  // --- JPA ---

  private Cart getCartJpa(Long userId) {
    Optional<CartEntity> oc = cartJpaRepository.findFirstByUserIdOrderByIdAsc(userId);
    if (oc.isEmpty()) {
      Cart empty = new Cart();
      empty.setUserId(userId);
      empty.setItems(Collections.emptyList());
      empty.setTotalAmount(0.0);
      return empty;
    }
    CartEntity ce = oc.get();
    Cart cart = toClientCart(ce);
    List<CartItemEntity> lines = cartItemJpaRepository.findByCartIdOrderByIdAsc(ce.getId());
    cart.setItems(toClientCartItems(lines));
    return cart;
  }

  private Cart addItemJpa(Long userId, Long restaurantId, Long dishId, Integer quantity) {
    CartEntity cart = cartJpaRepository.findFirstByUserIdOrderByIdAsc(userId).orElse(null);
    Long cartId;
    if (cart == null) {
      CartEntity newCart = new CartEntity();
      newCart.setUserId(userId);
      newCart.setRestaurantId(restaurantId);
      newCart.setTotalAmount(0.0);
      cart = cartJpaRepository.save(newCart);
      cartId = cart.getId();
    } else {
      cartId = cart.getId();
    }

    Optional<CartItemEntity> existing = cartItemJpaRepository.findByCartIdAndDishId(cartId, dishId);
    if (existing.isPresent()) {
      CartItemEntity e = existing.get();
      e.setQuantity(e.getQuantity() + quantity);
      cartItemJpaRepository.save(e);
    } else {
      double price = dishJpaRepository.findById(dishId)
          .map(DishEntity::getPrice)
          .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено: id=" + dishId));
      CartItemEntity ni = new CartItemEntity();
      ni.setCartId(cartId);
      ni.setDishId(dishId);
      ni.setQuantity(quantity);
      ni.setPriceAtTime(price);
      cartItemJpaRepository.save(ni);
    }
    refreshCartTotalJpa(cartId);
    return getCartJpa(userId);
  }

  private Cart updateItemQuantityJpa(Long userId, Long itemId, Integer quantity) {
    Long cartId = cartJpaRepository.findFirstByUserIdOrderByIdAsc(userId)
        .map(CartEntity::getId)
        .orElse(null);
    if (cartId == null) {
      throw new IllegalArgumentException("Корзина не найдена");
    }
    CartItemEntity line = cartItemJpaRepository.findByIdAndCartId(itemId, cartId)
        .orElseThrow(() -> new IllegalArgumentException("Элемент корзины не найден"));
    line.setQuantity(quantity);
    cartItemJpaRepository.save(line);
    refreshCartTotalJpa(cartId);
    return getCartJpa(userId);
  }

  private Cart removeItemJpa(Long userId, Long itemId) {
    Long cartId = cartJpaRepository.findFirstByUserIdOrderByIdAsc(userId)
        .map(CartEntity::getId)
        .orElse(null);
    if (cartId == null) {
      throw new IllegalArgumentException("Корзина не найдена");
    }
    CartItemEntity line = cartItemJpaRepository.findByIdAndCartId(itemId, cartId)
        .orElseThrow(() -> new IllegalArgumentException("Элемент корзины не найден"));
    cartItemJpaRepository.delete(line);
    refreshCartTotalJpa(cartId);
    return getCartJpa(userId);
  }

  private Cart clearCartJpa(Long userId) {
    Optional<CartEntity> oc = cartJpaRepository.findFirstByUserIdOrderByIdAsc(userId);
    if (oc.isEmpty()) {
      Cart empty = new Cart();
      empty.setUserId(userId);
      empty.setItems(Collections.emptyList());
      empty.setTotalAmount(0.0);
      return empty;
    }
    Long cartId = oc.get().getId();
    cartItemJpaRepository.deleteByCartId(cartId);
    CartEntity cart = oc.get();
    cart.setTotalAmount(0.0);
    cartJpaRepository.save(cart);
    return getCartJpa(userId);
  }

  private void refreshCartTotalJpa(Long cartId) {
    List<CartItemEntity> items = cartItemJpaRepository.findByCartIdOrderByIdAsc(cartId);
    double sum = items.stream()
        .mapToDouble(i -> i.getQuantity() * i.getPriceAtTime())
        .sum();
    CartEntity cart = cartJpaRepository.findById(cartId)
        .orElseThrow(() -> new IllegalStateException("Корзина не найдена: id=" + cartId));
    cart.setTotalAmount(sum);
    cartJpaRepository.save(cart);
  }

  private static Cart toClientCart(CartEntity e) {
    Cart c = new Cart();
    c.setId(e.getId());
    c.setUserId(e.getUserId());
    c.setRestaurantId(e.getRestaurantId());
    c.setTotalAmount(e.getTotalAmount() != null ? e.getTotalAmount() : 0.0);
    return c;
  }

  private List<CartItem> toClientCartItems(List<CartItemEntity> lines) {
    if (lines.isEmpty()) {
      return Collections.emptyList();
    }
    Set<Long> dishIds = lines.stream().map(CartItemEntity::getDishId).collect(Collectors.toSet());
    List<DishEntity> dishes = dishJpaRepository.findAllById(dishIds);
    Map<Long, String> names = new HashMap<>();
    for (DishEntity d : dishes) {
      names.put(d.getId(), d.getName());
    }
    return lines.stream().map(line -> {
      CartItem item = new CartItem();
      item.setId(line.getId());
      item.setMenuItemId(line.getDishId());
      item.setQuantity(line.getQuantity());
      item.setPrice(line.getPriceAtTime());
      item.setName(names.getOrDefault(line.getDishId(), "Блюдо #" + line.getDishId()));
      return item;
    }).collect(Collectors.toList());
  }

}
