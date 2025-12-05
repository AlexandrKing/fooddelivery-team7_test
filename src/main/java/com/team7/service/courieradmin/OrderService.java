package com.team7.service.courieradmin;

import com.team7.service.config.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

  public static class Order {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private Long courierId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private String clientName;
    private String restaurantName;
    private String courierName;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }
  }

  public List<Order> getAllOrders() {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT co.*, cu.full_name as client_name, r.name as restaurant_name, " +
        "cou.full_name as courier_name " +
        "FROM client_orders co " +
        "LEFT JOIN client_users cu ON co.user_id = cu.id " +
        "LEFT JOIN restaurant r ON co.restaurant_id = r.id " +
        "LEFT JOIN courier_users cou ON co.courier_id = cou.id " +
        "ORDER BY co.created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения заказов: " + e.getMessage());
    }

    return orders;
  }

  public List<Order> getAvailableOrders() {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT co.*, cu.full_name as client_name, r.name as restaurant_name " +
        "FROM client_orders co " +
        "LEFT JOIN client_users cu ON co.user_id = cu.id " +
        "LEFT JOIN restaurant r ON co.restaurant_id = r.id " +
        "WHERE co.status = 'NEW' AND co.courier_id IS NULL " +
        "ORDER BY co.created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения доступных заказов: " + e.getMessage());
    }

    return orders;
  }

  public Order getOrderById(Long orderId) {
    String sql = "SELECT co.*, cu.full_name as client_name, r.name as restaurant_name, " +
        "cou.full_name as courier_name " +
        "FROM client_orders co " +
        "LEFT JOIN client_users cu ON co.user_id = cu.id " +
        "LEFT JOIN restaurant r ON co.restaurant_id = r.id " +
        "LEFT JOIN courier_users cou ON co.courier_id = cou.id " +
        "WHERE co.id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToOrder(rs);
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения заказа: " + e.getMessage());
    }

    return null;
  }

  public boolean cancelOrder(Long orderId) {
    String sql = "UPDATE client_orders SET status = 'CANCELLED' WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка отмены заказа: " + e.getMessage());
    }

    return false;
  }

  public boolean assignOrderToCourier(Long orderId, Long courierId) {
    String sql = "UPDATE client_orders SET courier_id = ?, status = 'ASSIGNED' WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      stmt.setLong(2, orderId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка назначения заказа курьеру: " + e.getMessage());
    }

    return false;
  }

  public boolean takeOrder(Long orderId, Long courierId) {
    Order order = getOrderById(orderId);
    if (order == null || !"NEW".equals(order.getStatus()) || order.getCourierId() != null) {
      return false;
    }

    return assignOrderToCourier(orderId, courierId);
  }

  public boolean completeOrder(Long orderId) {
    String sql = "UPDATE client_orders SET status = 'DELIVERED', delivered_at = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка завершения заказа: " + e.getMessage());
    }

    return false;
  }

  public BigDecimal getOrderProfit(Long orderId) {
    Order order = getOrderById(orderId);
    if (order != null && order.getTotalAmount() != null) {
      return order.getTotalAmount().multiply(new BigDecimal("0.20"));
    }
    return BigDecimal.ZERO;
  }

  private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
    Order order = new Order();
    order.setId(rs.getLong("id"));
    order.setUserId(rs.getLong("user_id"));
    order.setRestaurantId(rs.getLong("restaurant_id"));
    order.setCourierId(rs.getLong("courier_id"));
    order.setStatus(rs.getString("status"));
    order.setTotalAmount(rs.getBigDecimal("total_amount"));
    order.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
    order.setDeliveryAddress(rs.getString("delivery_address"));
    order.setNotes(rs.getString("notes"));
    order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    order.setDeliveredAt(rs.getTimestamp("delivered_at") != null ?
        rs.getTimestamp("delivered_at").toLocalDateTime() : null);
    order.setClientName(rs.getString("client_name"));
    order.setRestaurantName(rs.getString("restaurant_name"));
    order.setCourierName(rs.getString("courier_name"));
    return order;
  }
}