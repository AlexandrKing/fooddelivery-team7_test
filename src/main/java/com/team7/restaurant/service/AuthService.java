package com.team7.restaurant.service;

import com.team7.restaurant.api.AuthOperations;
import com.team7.restaurant.config.DatabaseConfig;
import com.team7.restaurant.model.Restaurant;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthService implements AuthOperations {
  private Restaurant currentRestaurant = null;

  // ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ ПРОВЕРКИ УНИКАЛЬНОСТИ
  public boolean isEmailExists(String email) {
    String sql = "SELECT COUNT(*) FROM restaurant WHERE email = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean isPhoneExists(String phone) {
    String sql = "SELECT COUNT(*) FROM restaurant WHERE phone = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, phone);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public Restaurant registerRestaurant(String name, String email, String password,
                                       String phone, String address, String cuisineType) {
    if (!isValidPhone(phone)) {
      throw new IllegalArgumentException("Неверный формат телефона");
    }

    // ПРОВЕРКА УНИКАЛЬНОСТИ ПЕРЕД РЕГИСТРАЦИЕЙ
    if (isEmailExists(email)) {
      throw new IllegalArgumentException("Email уже используется");
    }

    if (isPhoneExists(phone)) {
      throw new IllegalArgumentException("Телефон уже используется");
    }

    String sql = "INSERT INTO restaurant (name, email, password, phone, address, cuisine_type, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING') RETURNING *";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, name);
      stmt.setString(2, email);
      stmt.setString(3, password);
      stmt.setString(4, phone);
      stmt.setString(5, address);
      stmt.setString(6, cuisineType);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToRestaurant(rs);
      }
    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) {
        throw new IllegalArgumentException("Email или телефон уже используются");
      }
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Restaurant login(String email, String password) {
    String sql = "SELECT * FROM restaurant WHERE email = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      stmt.setString(2, password);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        updateLastLogin(rs.getLong("id"));
        Restaurant restaurant = mapResultSetToRestaurant(rs);
        currentRestaurant = restaurant;
        return restaurant;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void logout() {
    currentRestaurant = null;
  }

  @Override
  public Boolean changePassword(Long restaurantId, String currentPassword, String newPassword) {
    String sql = "UPDATE restaurant SET password = ? WHERE id = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, newPassword);
      stmt.setLong(2, restaurantId);
      stmt.setString(3, currentPassword);

      int rowsUpdated = stmt.executeUpdate();
      return rowsUpdated > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void resetPassword(String email) {
    System.out.println("Запрос на сброс пароля для: " + email);
  }

  public static List<Restaurant> getAllRestaurants() {
    List<Restaurant> restaurants = new ArrayList<>();
    String sql = "SELECT * FROM restaurant";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        restaurants.add(mapResultSetToRestaurant(rs));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return restaurants;
  }

  public static Restaurant getRestaurantById(Long id) {
    String sql = "SELECT * FROM restaurant WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToRestaurant(rs);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void updateLastLogin(Long restaurantId) {
    String sql = "UPDATE restaurant SET last_login_date = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(rs.getLong("id"));
    restaurant.setName(rs.getString("name"));
    restaurant.setEmail(rs.getString("email"));
    restaurant.setPassword(rs.getString("password"));
    restaurant.setPhone(rs.getString("phone"));
    restaurant.setAddress(rs.getString("address"));
    restaurant.setCuisineType(rs.getString("cuisine_type"));
    restaurant.setDescription(rs.getString("description"));
    restaurant.setStatus(rs.getString("status"));
    restaurant.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());

    Timestamp lastLogin = rs.getTimestamp("last_login_date");
    if (lastLogin != null) {
      restaurant.setLastLoginDate(lastLogin.toLocalDateTime());
    }

    restaurant.setEmailVerified(rs.getBoolean("email_verified"));
    return restaurant;
  }

  private boolean isValidPhone(String phone) {
    return phone.matches("^(\\+7|8)\\d{10}$");
  }
}
