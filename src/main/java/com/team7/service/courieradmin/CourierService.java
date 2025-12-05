package com.team7.service.courieradmin;

import com.team7.model.courier.Courier;
import com.team7.service.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourierService {

  public Courier login(String username, String password) {
    String sql = "SELECT * FROM courier_users WHERE username = ? AND is_active = TRUE";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        String storedHash = rs.getString("password_hash");

        if (BCrypt.checkpw(password, storedHash)) {
          Courier courier = mapResultSetToCourier(rs);
          updateLastLogin(courier.getId());
          return courier;
        }
      }

    } catch (SQLException e) {
      System.err.println("Ошибка при входе курьера: " + e.getMessage());
    }

    return null;
  }

  private void updateLastLogin(Long courierId) {
    String sql = "UPDATE courier_users SET last_login_at = CURRENT_TIMESTAMP, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      stmt.executeUpdate();

    } catch (SQLException e) {
      System.err.println("Ошибка обновления времени входа: " + e.getMessage());
    }
  }

  public boolean registerCourier(String username, String password, String fullName,
                                 String email, String phone, String vehicleType) {
    String sql = "INSERT INTO courier_users (username, password_hash, full_name, email, phone, vehicle_type, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      if (isUsernameExists(username)) {
        throw new IllegalArgumentException("Логин уже занят");
      }
      if (isEmailExists(email)) {
        throw new IllegalArgumentException("Email уже зарегистрирован");
      }

      String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

      stmt.setString(1, username);
      stmt.setString(2, hashedPassword);
      stmt.setString(3, fullName);
      stmt.setString(4, email);
      stmt.setString(5, phone);
      stmt.setString(6, vehicleType != null ? vehicleType : "bicycle");
      stmt.setString(7, "offline");

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка регистрации курьера: " + e.getMessage());
    }

    return false;
  }

  private boolean isUsernameExists(String username) {
    String sql = "SELECT COUNT(*) FROM courier_users WHERE username = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }

    } catch (SQLException e) {
      System.err.println("Ошибка проверки логина: " + e.getMessage());
    }

    return false;
  }

  private boolean isEmailExists(String email) {
    String sql = "SELECT COUNT(*) FROM courier_users WHERE email = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }

    } catch (SQLException e) {
      System.err.println("Ошибка проверки email: " + e.getMessage());
    }

    return false;
  }

  public List<Courier> getAllCouriers() {
    List<Courier> couriers = new ArrayList<>();
    String sql = "SELECT * FROM courier_users ORDER BY created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        couriers.add(mapResultSetToCourier(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения курьеров: " + e.getMessage());
    }

    return couriers;
  }

  public List<Courier> getAvailableCouriers() {
    List<Courier> couriers = new ArrayList<>();
    String sql = "SELECT * FROM courier_users WHERE status = 'available' AND is_active = TRUE ORDER BY rating DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        couriers.add(mapResultSetToCourier(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения доступных курьеров: " + e.getMessage());
    }

    return couriers;
  }

  public Courier getCourierById(Long id) {
    String sql = "SELECT * FROM courier_users WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToCourier(rs);
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения курьера: " + e.getMessage());
    }

    return null;
  }

  public boolean updateCourierStatus(Long courierId, String status) {
    String sql = "UPDATE courier_users SET status = ?, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, status);
      stmt.setLong(2, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка обновления статуса курьера: " + e.getMessage());
    }

    return false;
  }

  public boolean updateCourierLocation(Long courierId, String location) {
    String sql = "UPDATE courier_users SET current_location = ?, last_activity_at = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, location);
      stmt.setLong(2, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка обновления локации курьера: " + e.getMessage());
    }

    return false;
  }

  public boolean addMoneyToCourier(Long courierId, BigDecimal amount) {
    String sql = "UPDATE courier_users SET balance = balance + ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setBigDecimal(1, amount);
      stmt.setLong(2, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка пополнения баланса курьера: " + e.getMessage());
    }

    return false;
  }

  public boolean incrementCompletedOrders(Long courierId) {
    String sql = "UPDATE courier_users SET completed_orders = completed_orders + 1 WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка обновления счетчика заказов: " + e.getMessage());
    }

    return false;
  }

  public boolean blockCourier(Long courierId) {
    String sql = "UPDATE courier_users SET is_active = FALSE, status = 'offline' WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка блокировки курьера: " + e.getMessage());
    }

    return false;
  }

  public boolean unblockCourier(Long courierId) {
    String sql = "UPDATE courier_users SET is_active = TRUE WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);

      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка разблокировки курьера: " + e.getMessage());
    }

    return false;
  }

  public boolean addCommissionToAll(Long orderId) {
    String sql = "UPDATE courier_users SET balance = balance + 10 WHERE status = 'available' AND is_active = TRUE";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement()) {

      int count = stmt.executeUpdate(sql);
      System.out.println("Начислено 10 руб. " + count + " активным курьерам за заказ " + orderId);
      return count > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка начисления комиссии всем курьерам: " + e.getMessage());
    }

    return false;
  }

  public boolean addCommissionToCourier(Long orderId, Long courierId) {
    try {
      String orderSql = "SELECT total_amount FROM client_orders WHERE id = ?";
      BigDecimal orderAmount = null;

      try (Connection conn = DatabaseConfig.getConnection();
           PreparedStatement stmt = conn.prepareStatement(orderSql)) {

        stmt.setLong(1, orderId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
          orderAmount = rs.getBigDecimal("total_amount");
        }
      }

      if (orderAmount != null) {
        BigDecimal commission = orderAmount.multiply(new BigDecimal("0.50"));

        String updateSql = "UPDATE courier_users SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

          stmt.setBigDecimal(1, commission);
          stmt.setLong(2, courierId);

          int affected = stmt.executeUpdate();
          if (affected > 0) {
            System.out.println("Начислено " + commission + " руб. курьеру #" + courierId + " за заказ " + orderId);
            return true;
          }
        }
      }

    } catch (SQLException e) {
      System.err.println("Ошибка начисления комиссии курьеру: " + e.getMessage());
    }

    return false;
  }

  private Courier mapResultSetToCourier(ResultSet rs) throws SQLException {
    Courier courier = new Courier();
    courier.setId(rs.getLong("id"));
    courier.setUsername(rs.getString("username"));
    courier.setEmail(rs.getString("email"));
    courier.setPasswordHash(rs.getString("password_hash"));
    courier.setFullName(rs.getString("full_name"));
    courier.setPhone(rs.getString("phone"));
    courier.setVehicleType(rs.getString("vehicle_type"));
    courier.setStatus(rs.getString("status"));
    courier.setCurrentLocation(rs.getString("current_location"));
    courier.setRating(rs.getBigDecimal("rating"));
    courier.setCompletedOrders(rs.getInt("completed_orders"));
    courier.setBalance(rs.getBigDecimal("balance"));
    courier.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    courier.setLastLoginAt(rs.getTimestamp("last_login_at") != null ?
        rs.getTimestamp("last_login_at").toLocalDateTime() : null);
    courier.setLastActivityAt(rs.getTimestamp("last_activity_at") != null ?
        rs.getTimestamp("last_activity_at").toLocalDateTime() : null);
    courier.setIsActive(rs.getBoolean("is_active"));
    return courier;
  }
}