package com.team7.repository.client;

import com.team7.model.client.Address;
import com.team7.model.client.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientAuthRepository {
  private final JdbcTemplate jdbcTemplate;

  public ClientAuthRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Deprecated(forRemoval = false, since = "1.1")
  public ClientAuthRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
  }

  public User createUser(String name, String email, String phone, String encodedPassword) {
    String sql = "INSERT INTO users (full_name, email, password, phone) VALUES (?, ?, ?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, name);
      ps.setString(2, email);
      ps.setString(3, encodedPassword);
      ps.setString(4, phone);
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new RuntimeException("Не удалось зарегистрировать пользователя");
    }

    User user = new User();
    user.setId(key.longValue());
    user.setName(name);
    user.setEmail(email);
    user.setPhone(phone);
    user.setPassword(encodedPassword);
    user.setAddresses(new ArrayList<>());
    return user;
  }

  public User findByEmail(String email) {
    List<User> users = jdbcTemplate.query(
        "SELECT id, full_name, email, phone, password FROM users WHERE email = ?",
        (rs, rowNum) -> {
          User user = new User();
          user.setId(rs.getLong("id"));
          user.setName(rs.getString("full_name"));
          user.setEmail(rs.getString("email"));
          user.setPhone(rs.getString("phone"));
          user.setPassword(rs.getString("password"));
          return user;
        },
        email
    );
    if (users.isEmpty()) {
      return null;
    }
    User user = users.get(0);
    user.setAddresses(getUserAddresses(user.getId()));
    return user;
  }

  public User findById(Long userId) {
    List<User> users = jdbcTemplate.query(
        "SELECT id, full_name, email, phone, password FROM users WHERE id = ?",
        (rs, rowNum) -> {
          User user = new User();
          user.setId(rs.getLong("id"));
          user.setName(rs.getString("full_name"));
          user.setEmail(rs.getString("email"));
          user.setPhone(rs.getString("phone"));
          user.setPassword(rs.getString("password"));
          return user;
        },
        userId
    );
    if (users.isEmpty()) {
      return null;
    }
    User user = users.get(0);
    user.setAddresses(getUserAddresses(user.getId()));
    return user;
  }

  public int countByEmail(String email) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE email = ?",
        Integer.class,
        email
    );
    return count == null ? 0 : count;
  }

  public int countByPhone(String phone) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE phone = ?",
        Integer.class,
        phone
    );
    return count == null ? 0 : count;
  }

  public int updateProfile(User user) {
    return jdbcTemplate.update(
        "UPDATE users SET full_name = ?, phone = ?, email = ? WHERE id = ?",
        user.getName(),
        user.getPhone(),
        user.getEmail(),
        user.getId()
    );
  }

  public Address addAddress(Long userId, Address address) {
    String sql = "INSERT INTO addresses (user_id, label, address, apartment) VALUES (?, ?, ?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, address.getLabel());
      ps.setString(3, address.getAddress());
      ps.setString(4, address.getApartment());
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key != null) {
      address.setId(key.longValue());
    }
    return address;
  }

  public String findPasswordByUserId(Long userId) {
    List<String> values = jdbcTemplate.query(
        "SELECT password FROM users WHERE id = ?",
        (rs, rowNum) -> rs.getString("password"),
        userId
    );
    return values.isEmpty() ? null : values.get(0);
  }

  public int updatePassword(Long userId, String encodedPassword) {
    return jdbcTemplate.update(
        "UPDATE users SET password = ? WHERE id = ?",
        encodedPassword,
        userId
    );
  }

  private List<Address> getUserAddresses(Long userId) {
    return jdbcTemplate.query(
        "SELECT id, label, address, apartment FROM addresses WHERE user_id = ?",
        (rs, rowNum) -> {
          Address address = new Address();
          address.setId(rs.getLong("id"));
          address.setLabel(rs.getString("label"));
          address.setAddress(rs.getString("address"));
          address.setApartment(rs.getString("apartment"));
          return address;
        },
        userId
    );
  }
}

