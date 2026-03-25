package com.team7.repository.client;

import com.team7.model.client.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import static java.util.Objects.requireNonNull;

@Repository
public class OrderRepository {
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate txTemplate;

  private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> {
    Order order = new Order();
    order.setId(rs.getLong("id"));
    order.setUserId(rs.getLong("user_id"));
    order.setRestaurantId(rs.getLong("restaurant_id"));
    order.setStatus(OrderStatus.valueOf(rs.getString("status")));
    order.setDeliveryAddress(rs.getString("delivery_address"));
    order.setDeliveryType(DeliveryType.valueOf(rs.getString("delivery_type")));
    order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
    Timestamp preferredTs = rs.getTimestamp("delivery_time");
    order.setPreferredDeliveryTime(preferredTs != null ? preferredTs.toLocalDateTime() : null);
    Timestamp createdTs = rs.getTimestamp("created_at");
    order.setCreatedAt(createdTs != null ? createdTs.toLocalDateTime() : null);
    order.setTotalAmount(rs.getDouble("total_amount"));
    return order;
  };

  public OrderRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
    this.txTemplate = new TransactionTemplate(requireNonNull(transactionManager));
  }

  /**
   * No-arg fallback constructor for legacy {@code userstory/*} code.
   */
  // TODO(legacy-cleanup): remove this fallback constructor in Wave 2.
  @Deprecated(forRemoval = false, since = "1.1")
  public OrderRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
    PlatformTransactionManager txManager = new DataSourceTransactionManager(ds);
    this.txTemplate = new TransactionTemplate(txManager);
  }

  public OrderCreationResult createOrder(
      Long userId,
      Long restaurantId,
      String deliveryAddress,
      DeliveryType deliveryType,
      LocalDateTime deliveryTime,
      PaymentMethod paymentMethod,
      Double totalAmount,
      List<CartItem> items
  ) {
    return txTemplate.execute(status -> {
      String orderSql = "INSERT INTO orders (user_id, restaurant_id, delivery_address, " +
          "delivery_type, delivery_time, payment_method, status, total_amount) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
          "RETURNING id, created_at";

      Map<String, Object> orderRow = jdbcTemplate.queryForMap(
          orderSql,
          userId,
          restaurantId,
          deliveryAddress,
          deliveryType.toString(),
          Timestamp.valueOf(deliveryTime),
          paymentMethod.toString(),
          OrderStatus.PENDING.toString(),
          totalAmount
      );

      long orderId = ((Number) orderRow.get("id")).longValue();
      Timestamp createdAtTs = (Timestamp) orderRow.get("created_at");
      LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

      // Insert order items
      String itemSql = "INSERT INTO order_items (order_id, dish_id, name, price, quantity) " +
          "VALUES (?, ?, ?, ?, ?)";

      jdbcTemplate.batchUpdate(itemSql, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
          CartItem ci = items.get(i);
          ps.setLong(1, orderId);
          ps.setLong(2, ci.getMenuItemId()); // dish_id
          ps.setString(3, ci.getName());
          ps.setDouble(4, ci.getPrice());
          ps.setInt(5, ci.getQuantity());
        }

        @Override
        public int getBatchSize() {
          return items.size();
        }
      });

      // Add history
      String historySql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?)";
      jdbcTemplate.update(historySql, orderId, OrderStatus.PENDING.toString());

      return new OrderCreationResult(orderId, createdAt);
    });
  }

  public Order getOrder(Long orderId) {
    try {
      Order order = jdbcTemplate.queryForObject(
          "SELECT * FROM orders WHERE id = ?",
          ORDER_ROW_MAPPER,
          orderId
      );
      order.setItems(getOrderItems(orderId));
      return order;
    } catch (EmptyResultDataAccessException e) {
      throw new IllegalArgumentException("Заказ не найден");
    }
  }

  public List<Order> getUserOrders(Long userId) {
    return jdbcTemplate.query(
        "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC",
        ORDER_ROW_MAPPER,
        userId
    );
  }

  public Order cancelOrder(Long orderId) {
    return txTemplate.execute(status -> {
      String updateSql = "UPDATE orders SET status = ? WHERE id = ? AND status = ?";
      int rows = jdbcTemplate.update(updateSql, OrderStatus.CANCELLED.toString(), orderId, OrderStatus.PENDING.toString());
      if (rows > 0) {
        String historySql = "INSERT INTO order_status_history (order_id, status) VALUES (?, ?)";
        jdbcTemplate.update(historySql, orderId, OrderStatus.CANCELLED.toString());
        return getOrder(orderId);
      }
      throw new IllegalArgumentException("Нельзя отменить заказ в текущем статусе");
    });
  }

  private List<OrderItem> getOrderItems(Long orderId) {
    List<OrderItem> items = jdbcTemplate.query(
        "SELECT * FROM order_items WHERE order_id = ?",
        (rs, rowNum) -> {
          OrderItem item = new OrderItem();
          item.setId(rs.getLong("id"));
          item.setMenuItemId(rs.getLong("dish_id"));
          item.setName(rs.getString("name"));
          item.setPrice(rs.getDouble("price"));
          item.setQuantity(rs.getInt("quantity"));
          return item;
        },
        orderId
    );
    return items == null ? Collections.emptyList() : items;
  }

  public static final class OrderCreationResult {
    private final Long orderId;
    private final LocalDateTime createdAt;

    public OrderCreationResult(Long orderId, LocalDateTime createdAt) {
      this.orderId = orderId;
      this.createdAt = createdAt;
    }

    public Long getOrderId() {
      return orderId;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }
  }
}

