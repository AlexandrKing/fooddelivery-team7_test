package com.team7.service.restaurant;

import com.team7.service.config.DatabaseConfig;
import com.team7.model.restaurant.Restaurant;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthService implements AuthOperations {
  private Restaurant currentRestaurant = null;

  public boolean isEmailExists(String email) {
    String sql = "SELECT COUNT(*) FROM restaurant WHERE LOWER(email) = LOWER(?)";

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

  // НОВЫЙ МЕТОД: проверка уникальности названия ресторана
  public boolean isRestaurantNameExists(String name) {
    String sql = "SELECT COUNT(*) FROM restaurant WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, name);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  // НОВЫЙ МЕТОД: проверка уникальности адреса ресторана
  public boolean isRestaurantAddressExists(String address) {
    String sql = "SELECT COUNT(*) FROM restaurant WHERE LOWER(TRIM(address)) = LOWER(TRIM(?))";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, address);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  // НОВЫЙ МЕТОД: поиск похожих названий ресторанов
  public List<String> findSimilarRestaurantNames(String name) {
    List<String> similarNames = new ArrayList<>();
    String sql = "SELECT name FROM restaurant WHERE LOWER(name) LIKE LOWER(?) LIMIT 5";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, "%" + name.trim() + "%");
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        similarNames.add(rs.getString("name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return similarNames;
  }

  @Override
  public Restaurant registerRestaurant(String name, String email, String password,
                                       String phone, String address, String cuisineType) {
    // Валидация входных данных
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Название ресторана не может быть пустым");
    }

    if (!isValidPhone(phone)) {
      throw new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7XXXXXXXXXX или 8XXXXXXXXXX");
    }

    if (!isValidEmail(email)) {
      throw new IllegalArgumentException("Неверный формат email");
    }

    // Проверка уникальности данных
    if (isEmailExists(email)) {
      throw new IllegalArgumentException("Email уже используется другим рестораном");
    }

    if (isPhoneExists(phone)) {
      throw new IllegalArgumentException("Телефон уже используется другим рестораном");
    }

    if (isRestaurantNameExists(name)) {
      throw new IllegalArgumentException("Ресторан с таким названием уже зарегистрирован");
    }

    if (isRestaurantAddressExists(address)) {
      throw new IllegalArgumentException("По этому адресу уже зарегистрирован ресторан. Пожалуйста, уточните адрес");
    }

    String sql = "INSERT INTO restaurant (name, email, password, phone, address, cuisine_type, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING') RETURNING *";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, name.trim());
      stmt.setString(2, email.toLowerCase().trim());
      stmt.setString(3, password);
      stmt.setString(4, phone);
      stmt.setString(5, address.trim());
      stmt.setString(6, cuisineType);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        Restaurant restaurant = mapResultSetToRestaurant(rs);
        System.out.println("✅ Ресторан успешно зарегистрирован! ID: " + restaurant.getId());
        return restaurant;
      }
    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) { // PostgreSQL error code for unique violation
        // Анализируем сообщение об ошибке для уточнения
        String errorMessage = e.getMessage();
        if (errorMessage.contains("restaurant_email_key")) {
          throw new IllegalArgumentException("Email уже используется другим рестораном");
        } else if (errorMessage.contains("restaurant_phone_key")) {
          throw new IllegalArgumentException("Телефон уже используется другим рестораном");
        } else if (errorMessage.contains("restaurant_name_address_key")) {
          throw new IllegalArgumentException("Ресторан с таким названием и адресом уже существует");
        } else {
          throw new IllegalArgumentException("Нарушение уникальности данных: " + errorMessage);
        }
      }
      e.printStackTrace();
      throw new RuntimeException("Ошибка при регистрации ресторана: " + e.getMessage());
    }
    return null;
  }

  @Override
  public Restaurant login(String email, String password) {
    if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
      System.out.println("Ошибка: Email и пароль не могут быть пустыми");
      return null;
    }

    String sql = "SELECT * FROM restaurant WHERE LOWER(email) = LOWER(?) AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email.trim());
      stmt.setString(2, password);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        // Проверяем статус ресторана
        String status = rs.getString("status");
        if ("PENDING".equals(status)) {
          System.out.println("⚠️  Ваш ресторан еще не прошел модерацию. Ожидайте подтверждения.");
        } else if ("SUSPENDED".equals(status)) {
          System.out.println("❌ Ваш ресторан заблокирован. Свяжитесь с администрацией.");
          return null;
        } else if ("REJECTED".equals(status)) {
          System.out.println("❌ Заявка вашего ресторана отклонена. Свяжитесь с администрацией.");
          return null;
        }

        updateLastLogin(rs.getLong("id"));
        Restaurant restaurant = mapResultSetToRestaurant(rs);
        currentRestaurant = restaurant;
        System.out.println("✅ Вход выполнен успешно! Добро пожаловать, " + restaurant.getName());
        return restaurant;
      } else {
        System.out.println("❌ Неверный email или пароль");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Ошибка при входе: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void logout() {
    if (currentRestaurant != null) {
      System.out.println("👋 До свидания, " + currentRestaurant.getName() + "!");
    }
    currentRestaurant = null;
  }

  @Override
  public Boolean changePassword(Long restaurantId, String currentPassword, String newPassword) {
    if (newPassword == null || newPassword.length() < 6) {
      System.out.println("Ошибка: Новый пароль должен содержать минимум 6 символов");
      return false;
    }

    String sql = "UPDATE restaurant SET password = ? WHERE id = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, newPassword);
      stmt.setLong(2, restaurantId);
      stmt.setString(3, currentPassword);

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        System.out.println("✅ Пароль успешно изменен!");
        return true;
      } else {
        System.out.println("❌ Неверный текущий пароль");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void resetPassword(String email) {
    if (!isValidEmail(email)) {
      System.out.println("❌ Неверный формат email");
      return;
    }

    if (!isEmailExists(email)) {
      System.out.println("❌ Ресторан с таким email не найден");
      return;
    }

    // Генерация временного пароля
    String tempPassword = generateTemporaryPassword();

    String sql = "UPDATE restaurant SET password = ? WHERE LOWER(email) = LOWER(?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, tempPassword);
      stmt.setString(2, email);

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        System.out.println("✅ Временный пароль установлен: " + tempPassword);
        System.out.println("⚠️  Смените пароль при следующем входе в систему!");
      } else {
        System.out.println("❌ Ошибка сброса пароля");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private String generateTemporaryPassword() {
    // Генерация простого временного пароля
    return "temp" + System.currentTimeMillis() % 10000;
  }

  public static List<Restaurant> getAllRestaurants() {
    List<Restaurant> restaurants = new ArrayList<>();
    String sql = "SELECT * FROM restaurant ORDER BY name";

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
    return phone != null && phone.matches("^(\\+7|8)\\d{10}$");
  }

  private boolean isValidEmail(String email) {
    return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
  }
}