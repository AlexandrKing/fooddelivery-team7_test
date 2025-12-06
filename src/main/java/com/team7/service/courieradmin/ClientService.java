package com.team7.service.courieradmin;

import com.team7.service.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

  public static class Client {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private Boolean isActive;

    public Client() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
  }

  public List<Client> getAllClients() {
    List<Client> clients = new ArrayList<>();
    String sql = "SELECT * FROM users ORDER BY created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        clients.add(mapResultSetToClient(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения клиентов: " + e.getMessage());
    }

    return clients;
  }

  public Client getClientById(Long clientId) {
    String sql = "SELECT * FROM client_users WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, clientId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToClient(rs);
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения клиента: " + e.getMessage());
    }

    return null;
  }

  public boolean deactivateClient(Long clientId) {
    String sql = "UPDATE client_users SET is_active = FALSE WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, clientId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка деактивации клиента: " + e.getMessage());
    }

    return false;
  }

  public boolean activateClient(Long clientId) {
    String sql = "UPDATE client_users SET is_active = TRUE WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, clientId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка активации клиента: " + e.getMessage());
    }

    return false;
  }

  private Client mapResultSetToClient(ResultSet rs) throws SQLException {
    Client client = new Client();
    client.setId(rs.getLong("id"));
    client.setUsername(rs.getString("username"));
    client.setEmail(rs.getString("email"));
    client.setFullName(rs.getString("full_name"));
    client.setPhone(rs.getString("phone"));
    client.setAddress(rs.getString("address"));
    client.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    client.setIsActive(rs.getBoolean("is_active"));
    return client;
  }
}