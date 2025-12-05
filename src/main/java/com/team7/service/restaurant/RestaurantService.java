package com.team7.service.restaurant;

import com.team7.service.config.DatabaseConfig;
import com.team7.model.restaurant.Restaurant;

import java.sql.*;

public class RestaurantService implements RestaurantOperations {

  @Override
  public Restaurant updateRestaurant(Long restaurantId, String name, String phone,
                                     String address, String cuisineType, String description) {

    // Сначала получаем текущий ресторан
    Restaurant restaurant = getRestaurantById(restaurantId);
    if (restaurant == null) {
      System.out.println("❌ Ресторан не найден");
      return null;
    }

    // Строим динамический SQL запрос
    StringBuilder sql = new StringBuilder("UPDATE restaurants SET updated_at = CURRENT_TIMESTAMP");
    boolean hasUpdates = false;

    if (name != null) {
      sql.append(", name = ?");
      hasUpdates = true;
    }
    if (phone != null) {
      sql.append(", phone = ?");
      hasUpdates = true;
    }
    if (address != null) {
      sql.append(", address = ?");
      hasUpdates = true;
    }
    if (cuisineType != null) {
      sql.append(", cuisine_type = ?");
      hasUpdates = true;
    }
    if (description != null) {
      sql.append(", description = ?");
      hasUpdates = true;
    }

    if (!hasUpdates) {
      System.out.println("ℹ️  Нет данных для обновления");
      return restaurant;
    }

    sql.append(" WHERE id = ? RETURNING *");

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

      int paramIndex = 1;

      if (name != null) {
        stmt.setString(paramIndex++, name);
        restaurant.setName(name);
      }
      if (phone != null) {
        stmt.setString(paramIndex++, phone);
        restaurant.setPhone(phone);
      }
      if (address != null) {
        stmt.setString(paramIndex++, address);
        restaurant.setAddress(address);
      }
      if (cuisineType != null) {
        stmt.setString(paramIndex++, cuisineType);
        restaurant.setCuisineType(cuisineType);
      }
      if (description != null) {
        stmt.setString(paramIndex++, description);
        restaurant.setDescription(description);
      }

      stmt.setLong(paramIndex, restaurantId);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        // Обновляем объект ресторана
        restaurant = AuthService.mapResultSetToRestaurant(rs);
        System.out.println("✅ Данные ресторана успешно обновлены");
        return restaurant;
      }

    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) { // Нарушение уникальности
        String errorMessage = e.getMessage();
        if (errorMessage.contains("restaurants_phone_key")) {
          throw new IllegalArgumentException("Телефон уже используется другим рестораном");
        } else if (errorMessage.contains("restaurants_email_key")) {
          throw new IllegalArgumentException("Email уже используется другим рестораном");
        }
      }
      e.printStackTrace();
      throw new RuntimeException("Ошибка при обновлении ресторана: " + e.getMessage());
    }

    return restaurant;
  }

  @Override
  public Restaurant getRestaurantById(Long restaurantId) {
    return AuthService.getRestaurantById(restaurantId);
  }

  @Override
  public Boolean updateEmail(Long restaurantId, String newEmail, String password) {
    String sql = "UPDATE restaurants SET email = ?, email_verified = false WHERE id = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, newEmail);
      stmt.setLong(2, restaurantId);
      stmt.setString(3, password);

      int rowsUpdated = stmt.executeUpdate();
      return rowsUpdated > 0;

    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) { // Нарушение уникальности email
        throw new IllegalArgumentException("Email уже используется другим рестораном");
      }
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public Boolean updatePhone(Long restaurantId, String newPhone) {
    String sql = "UPDATE restaurants SET phone = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, newPhone);
      stmt.setLong(2, restaurantId);

      int rowsUpdated = stmt.executeUpdate();
      return rowsUpdated > 0;

    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) { // Нарушение уникальности телефона
        throw new IllegalArgumentException("Телефон уже используется другим рестораном");
      }
      e.printStackTrace();
    }
    return false;
  }
}