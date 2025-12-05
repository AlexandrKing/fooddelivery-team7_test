package com.team7.service.courieradmin;

import com.team7.model.courier.AssignedOrder;
import com.team7.service.config.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourierOrderService {

  public boolean assignOrderToCourier(Long orderId, Long courierId) {
    String sql = "INSERT INTO courier_assigned_orders (courier_id, order_id, status, assigned_at) VALUES (?, ?, 'assigned', CURRENT_TIMESTAMP)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      stmt.setLong(2, orderId);

      int affectedRows = stmt.executeUpdate();

      if (affectedRows > 0) {
        CourierService courierService = new CourierService();
        courierService.updateCourierStatus(courierId, "busy");
        return true;
      }

    } catch (SQLException e) {
      System.err.println("Ошибка назначения заказа курьеру: " + e.getMessage());
    }

    return false;
  }

  public boolean markOrderAsPickedUp(Long orderId) {
    String sql = "UPDATE courier_assigned_orders SET status = 'picked_up', picked_up_at = CURRENT_TIMESTAMP WHERE order_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка отметки заказа как полученного: " + e.getMessage());
    }

    return false;
  }

  public boolean markOrderAsDelivered(Long orderId, String deliveryNotes) {
    String sql = "UPDATE courier_assigned_orders SET status = 'delivered', delivered_at = CURRENT_TIMESTAMP, delivery_notes = ? WHERE order_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, deliveryNotes);
      stmt.setLong(2, orderId);

      int affectedRows = stmt.executeUpdate();

      if (affectedRows > 0) {
        Long courierId = getCourierIdByOrderId(orderId);
        if (courierId != null) {
          CourierService courierService = new CourierService();
          courierService.updateCourierStatus(courierId, "available");
          courierService.incrementCompletedOrders(courierId);

          BigDecimal orderAmount = getOrderAmount(orderId);
          if (orderAmount != null) {
            BigDecimal commission = orderAmount.multiply(new BigDecimal("0.5"));
            courierService.addMoneyToCourier(courierId, commission);
          }
        }
        return true;
      }

    } catch (SQLException e) {
      System.err.println("Ошибка отметки заказа как доставленного: " + e.getMessage());
    }

    return false;
  }

  private Long getCourierIdByOrderId(Long orderId) {
    String sql = "SELECT courier_id FROM courier_assigned_orders WHERE order_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getLong("courier_id");
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения ID курьера: " + e.getMessage());
    }

    return null;
  }

  private BigDecimal getOrderAmount(Long orderId) {
    String sql = "SELECT total_amount FROM client_orders WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, orderId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getBigDecimal("total_amount");
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения суммы заказа: " + e.getMessage());
    }

    return null;
  }

  public List<AssignedOrder> getOrdersByCourierId(Long courierId) {
    List<AssignedOrder> orders = new ArrayList<>();
    String sql = "SELECT cao.*, co.total_amount, co.delivery_address, r.name as restaurant_name " +
        "FROM courier_assigned_orders cao " +
        "JOIN client_orders co ON cao.order_id = co.id " +
        "JOIN restaurant r ON co.restaurant_id = r.id " +
        "WHERE cao.courier_id = ? " +
        "ORDER BY cao.assigned_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        AssignedOrder order = new AssignedOrder();
        order.setId(rs.getLong("id"));
        order.setCourierId(rs.getLong("courier_id"));
        order.setOrderId(rs.getLong("order_id"));
        order.setAssignedAt(rs.getTimestamp("assigned_at").toLocalDateTime());
        order.setPickedUpAt(rs.getTimestamp("picked_up_at") != null ?
            rs.getTimestamp("picked_up_at").toLocalDateTime() : null);
        order.setDeliveredAt(rs.getTimestamp("delivered_at") != null ?
            rs.getTimestamp("delivered_at").toLocalDateTime() : null);
        order.setStatus(rs.getString("status"));
        order.setDeliveryNotes(rs.getString("delivery_notes"));
        order.setOrderAmount(rs.getBigDecimal("total_amount"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setRestaurantName(rs.getString("restaurant_name"));

        orders.add(order);
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения заказов курьера: " + e.getMessage());
    }

    return orders;
  }
}