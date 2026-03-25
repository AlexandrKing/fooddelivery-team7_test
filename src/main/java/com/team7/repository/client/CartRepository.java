package com.team7.repository.client;

import com.team7.model.client.Cart;
import com.team7.model.client.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import static java.util.Objects.requireNonNull;

@Repository
public class CartRepository {
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate txTemplate;

  private static final RowMapper<Cart> CART_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
    Cart cart = new Cart();
    cart.setId(rs.getLong("id"));
    cart.setUserId(rs.getLong("user_id"));
    cart.setRestaurantId(rs.getLong("restaurant_id"));
    cart.setTotalAmount(rs.getDouble("total_amount"));
    return cart;
  };

  private static final RowMapper<CartItem> CART_ITEM_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
    CartItem item = new CartItem();
    item.setId(rs.getLong("id"));
    item.setMenuItemId(rs.getLong("dish_id"));
    item.setQuantity(rs.getInt("quantity"));
    item.setName(rs.getString("dish_name"));
    item.setPrice(rs.getDouble("price_at_time"));
    return item;
  };

  /**
   * Spring constructor.
   */
  public CartRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
    this.txTemplate = new TransactionTemplate(requireNonNull(transactionManager));
  }

  /**
   * No-arg fallback constructor for legacy {@code userstory/*} code.
   */
  // TODO(legacy-cleanup): remove this fallback constructor in Wave 2.
  @Deprecated(forRemoval = false, since = "1.1")
  public CartRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
    PlatformTransactionManager txManager = new DataSourceTransactionManager(ds);
    this.txTemplate = new TransactionTemplate(txManager);
  }

  public Cart getCart(Long userId) {
    Cart cart = findCartByUserId(userId);
    if (cart == null) {
      Cart empty = new Cart();
      empty.setUserId(userId);
      empty.setItems(Collections.emptyList());
      empty.setTotalAmount(0.0);
      return empty;
    }

    List<CartItem> items = findCartItems(cart.getId());
    cart.setItems(items);
    return cart;
  }

  public Cart addItem(Long userId, Long restaurantId, Long dishId, Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("Количество должно быть больше 0");
    }

    return txTemplate.execute(status -> {
      Long cartId = findCartIdByUserId(userId);
      if (cartId == null) {
        cartId = createNewCartInDb(userId, restaurantId);
      }

      CartItemExisting item = findCartItem(cartId, dishId);
      if (item != null) {
        int newQty = item.quantity + quantity;
        updateItemQuantityInDb(cartId, item.itemId, newQty);
      } else {
        double currentPrice = getCurrentDishPrice(dishId);
        addNewItemToCart(cartId, dishId, quantity, currentPrice);
      }

      updateCartTotalAmount(cartId);
      return getCart(userId);
    });
  }

  public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("Количество должно быть больше 0");
    }

    return txTemplate.execute(status -> {
      Long cartId = findCartIdByUserId(userId);
      if (cartId == null) {
        throw new IllegalArgumentException("Корзина не найдена");
      }

      int affected = jdbcTemplate.update(
          "UPDATE cart_items SET quantity = ? WHERE id = ? AND cart_id = ?",
          quantity, itemId, cartId
      );
      if (affected == 0) {
        throw new IllegalArgumentException("Элемент корзины не найден");
      }

      updateCartTotalAmount(cartId);
      return getCart(userId);
    });
  }

  public Cart removeItem(Long userId, Long itemId) {
    return txTemplate.execute(status -> {
      Long cartId = findCartIdByUserId(userId);
      if (cartId == null) {
        throw new IllegalArgumentException("Корзина не найдена");
      }

      jdbcTemplate.update(
          "DELETE FROM cart_items WHERE id = ? AND cart_id = ?",
          itemId, cartId
      );

      updateCartTotalAmount(cartId);
      return getCart(userId);
    });
  }

  public Cart clearCart(Long userId) {
    return txTemplate.execute(status -> {
      Long cartId = findCartIdByUserId(userId);
      if (cartId == null) {
        Cart empty = new Cart();
        empty.setUserId(userId);
        empty.setItems(Collections.emptyList());
        empty.setTotalAmount(0.0);
        return empty;
      }

      jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", cartId);
      jdbcTemplate.update("UPDATE carts SET total_amount = 0 WHERE id = ?", cartId);
      return getCart(userId);
    });
  }

  private Cart findCartByUserId(Long userId) {
    List<Cart> carts = jdbcTemplate.query(
        "SELECT id, user_id, restaurant_id, total_amount FROM carts WHERE user_id = ?",
        CART_ROW_MAPPER,
        userId
    );
    return carts.isEmpty() ? null : carts.get(0);
  }

  private Long findCartIdByUserId(Long userId) {
    List<Long> ids = jdbcTemplate.query(
        "SELECT id FROM carts WHERE user_id = ?",
        (rs, rowNum) -> rs.getLong("id"),
        userId
    );
    return ids.isEmpty() ? null : ids.get(0);
  }

  private List<CartItem> findCartItems(Long cartId) {
    return jdbcTemplate.query(
        "SELECT ci.id, ci.dish_id, ci.quantity, ci.price_at_time, d.name AS dish_name " +
            "FROM cart_items ci " +
            "JOIN dishes d ON ci.dish_id = d.id " +
            "WHERE ci.cart_id = ?",
        CART_ITEM_ROW_MAPPER,
        cartId
    );
  }

  private long createNewCartInDb(Long userId, Long restaurantId) {
    String sql = "INSERT INTO carts (user_id, restaurant_id, total_amount) VALUES (?, ?, 0)";
    KeyHolder keyHolder = new GeneratedKeyHolder();

    PreparedStatementCreator psc = con -> {
      PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setLong(2, restaurantId);
      return ps;
    };

    jdbcTemplate.update(psc, keyHolder);
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new IllegalStateException("Не удалось получить ID новой корзины");
    }
    return key.longValue();
  }

  private double getCurrentDishPrice(Long dishId) {
    Double price = jdbcTemplate.queryForObject(
        "SELECT price FROM dishes WHERE id = ?",
        Double.class,
        dishId
    );
    if (price == null) {
      throw new IllegalArgumentException("Блюдо не найдено: id=" + dishId);
    }
    return price;
  }

  private void addNewItemToCart(Long cartId, Long dishId, Integer quantity, double priceAtTime) {
    jdbcTemplate.update(
        "INSERT INTO cart_items (cart_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)",
        cartId, dishId, quantity, priceAtTime
    );
  }

  private CartItemExisting findCartItem(Long cartId, Long dishId) {
    List<CartItemExisting> items = jdbcTemplate.query(
        "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND dish_id = ?",
        (rs, rowNum) -> new CartItemExisting(rs.getLong("id"), rs.getInt("quantity")),
        cartId, dishId
    );
    return items.isEmpty() ? null : items.get(0);
  }

  private void updateItemQuantityInDb(Long cartId, Long itemId, int newQuantity) {
    int affected = jdbcTemplate.update(
        "UPDATE cart_items SET quantity = ? WHERE id = ? AND cart_id = ?",
        newQuantity, itemId, cartId
    );
    if (affected == 0) {
      throw new IllegalArgumentException("Элемент корзины не найден");
    }
  }

  private void updateCartTotalAmount(Long cartId) {
    jdbcTemplate.update(
        "UPDATE carts SET total_amount = (" +
            "SELECT COALESCE(SUM(ci.quantity * ci.price_at_time), 0) " +
            "FROM cart_items ci WHERE ci.cart_id = ?" +
            ") WHERE id = ?",
        cartId, cartId
    );
  }

  private static final class CartItemExisting {
    private final long itemId;
    private final int quantity;

    private CartItemExisting(long itemId, int quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
    }
  }
}

