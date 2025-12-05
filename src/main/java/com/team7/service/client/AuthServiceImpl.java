package com.team7.service.client;

import com.team7.model.client.User;
import com.team7.model.client.UserRole;
import com.team7.model.client.Address;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {
  private User currentUser;

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+79[0-9]{9}$");

  public AuthServiceImpl() {
    // Конструктор без параметров
  }

  @Override
  public User register(UserRole role, String name, String email, String phone, String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new IllegalArgumentException("Пароли не совпадают");
    }

    if (!isValidEmail(email)) {
      throw new IllegalArgumentException("Неверный формат email");
    }

    if (!isValidPhone(phone)) {
      throw new IllegalArgumentException("Телефон должен быть в формате +79XXXXXXXXX");
    }

    if (!isEmailAvailable(email)) {
      throw new IllegalArgumentException("Email уже используется");
    }

    if (!isPhoneAvailable(phone)) {
      throw new IllegalArgumentException("Номер телефона уже используется");
    }

    String sql = "INSERT INTO client_users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?::user_role) RETURNING id";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, name);
      pstmt.setString(2, email);
      pstmt.setString(3, phone);
      pstmt.setString(4, password);
      pstmt.setString(5, role.toString());

      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setRole(role);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setAddresses(new ArrayList<>());
        return user;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка при регистрации: " + e.getMessage(), e);
    }

    throw new RuntimeException("Не удалось зарегистрировать пользователя");
  }

  @Override
  public User login(String email, String password) {
    String sql = "SELECT * FROM client_users WHERE email = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      pstmt.setString(2, password);

      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        User user = mapUserFromResultSet(rs);
        currentUser = user;
        return user;
      } else {
        throw new IllegalArgumentException("Пользователь не найден или неверный пароль");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка при входе: " + e.getMessage(), e);
    }
  }

  @Override
  public void logout() {
    currentUser = null;
  }

  @Override
  public User getCurrentUser() {
    return currentUser;
  }

  @Override
  public boolean isEmailAvailable(String email) {
    String sql = "SELECT COUNT(*) FROM client_users WHERE email = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) == 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка проверки email: " + e.getMessage(), e);
    }
    return true;
  }

  @Override
  public boolean isPhoneAvailable(String phone) {
    String sql = "SELECT COUNT(*) FROM client_users WHERE phone = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, phone);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) == 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка проверки телефона: " + e.getMessage(), e);
    }
    return true;
  }

  @Override
  public User updateProfile(User updatedUser) {
    String sql = "UPDATE client_users SET name = ?, phone = ?, email = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, updatedUser.getName());
      pstmt.setString(2, updatedUser.getPhone());
      pstmt.setString(3, updatedUser.getEmail());
      pstmt.setLong(4, updatedUser.getId());

      int rows = pstmt.executeUpdate();
      if (rows > 0) {
        // Обновляем адреса
        List<Address> addresses = getUserAddresses(updatedUser.getId(), conn);
        updatedUser.setAddresses(addresses);

        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
          currentUser = updatedUser;
        }
        return updatedUser;
      } else {
        throw new IllegalArgumentException("Пользователь не найден");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка обновления профиля: " + e.getMessage(), e);
    }
  }

  @Override
  public User addAddress(Long userId, Address address) {
    String sql = "INSERT INTO client_addresses (user_id, label, address, apartment) VALUES (?, ?, ?, ?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setLong(1, userId);
      pstmt.setString(2, address.getLabel());
      pstmt.setString(3, address.getAddress());
      pstmt.setString(4, address.getApartment());

      int rows = pstmt.executeUpdate();
      if (rows > 0) {
        ResultSet rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
          address.setId(rs.getLong(1));

          // Получаем обновленного пользователя с адресами
          User user = getUserById(userId, conn);
          if (currentUser != null && currentUser.getId().equals(userId)) {
            currentUser = user;
          }
          return user;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка добавления адреса: " + e.getMessage(), e);
    }
    throw new RuntimeException("Не удалось добавить адрес");
  }

  @Override
  public User changePassword(Long userId, String oldPassword, String newPassword) {
    // Сначала проверяем старый пароль
    String checkSql = "SELECT COUNT(*) FROM client_users WHERE id = ? AND password = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

      checkStmt.setLong(1, userId);
      checkStmt.setString(2, oldPassword);

      ResultSet rs = checkStmt.executeQuery();
      if (rs.next() && rs.getInt(1) == 0) {
        throw new IllegalArgumentException("Неверный текущий пароль");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка проверки пароля: " + e.getMessage(), e);
    }

    // Обновляем пароль
    String updateSql = "UPDATE client_users SET password = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

      pstmt.setString(1, newPassword);
      pstmt.setLong(2, userId);

      int rows = pstmt.executeUpdate();
      if (rows > 0) {
        User user = getUserById(userId);
        if (currentUser != null && currentUser.getId().equals(userId)) {
          currentUser = user;
        }
        return user;
      } else {
        throw new IllegalArgumentException("Пользователь не найден");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка смены пароля: " + e.getMessage(), e);
    }
  }

  private User getUserById(Long userId) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      return getUserById(userId, conn);
    } catch (SQLException e) {
      throw new RuntimeException("Ошибка получения пользователя: " + e.getMessage(), e);
    }
  }

  private User getUserById(Long userId, Connection conn) throws SQLException {
    String sql = "SELECT * FROM client_users WHERE id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return mapUserFromResultSet(rs);
      }
    }
    return null;
  }

  private User mapUserFromResultSet(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setName(rs.getString("name"));
    user.setEmail(rs.getString("email"));
    user.setPhone(rs.getString("phone"));
    user.setPassword(rs.getString("password"));
    user.setRole(UserRole.valueOf(rs.getString("role")));

    // Загружаем адреса
    List<Address> addresses = getUserAddresses(user.getId(), rs.getStatement().getConnection());
    user.setAddresses(addresses);

    return user;
  }

  private List<Address> getUserAddresses(Long userId, Connection conn) throws SQLException {
    List<Address> addresses = new ArrayList<>();
    String sql = "SELECT * FROM client_addresses WHERE user_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, userId);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        Address address = new Address();
        address.setId(rs.getLong("id"));
        address.setLabel(rs.getString("label"));
        address.setAddress(rs.getString("address"));
        address.setApartment(rs.getString("apartment"));
        addresses.add(address);
      }
    }
    return addresses;
  }

  private boolean isValidEmail(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }

  private boolean isValidPhone(String phone) {
    return PHONE_PATTERN.matcher(phone).matches();
  }
}