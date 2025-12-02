package com.team7.service.client;

import com.team7.model.client.Menu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuServicelmpl implements MenuService {
  private final DatabaseService dbService;

  public MenuServicelmpl(DatabaseService dbService) {
    this.dbService = dbService;
  }

  @Override
  public List<Menu> getMenu(Long restaurantId) {
    List<Menu> menu = new ArrayList<>();
    String sql = "SELECT * FROM client_menu WHERE restaurant_id = ? AND is_available = true"; // Исправлены кавычки и is_available

    try (Connection conn = dbService.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) { // Исправлено: PreparedStatement

      pstmt.setLong(1, restaurantId); // Убраны именованные параметры

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Menu item = new Menu();
          item.setId(rs.getLong("id"));
          item.setRestaurantId(rs.getLong("restaurant_id"));
          item.setName(rs.getString("name"));
          item.setDescription(rs.getString("description"));
          item.setPrice(rs.getDouble("price"));
          item.setAvailable(rs.getBoolean("is_available"));
          menu.add(item);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Ошибка при получении меню: " + e.getMessage(), e);
    }

    return menu;
  }

  @Override
  public Menu getMenuItem(Long restaurantId, Long itemId) {
    String sql = "SELECT * FROM client_menu WHERE restaurant_id = ? AND id = ? AND is_available = true";

    try (Connection conn = dbService.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, restaurantId);
      pstmt.setLong(2, itemId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          Menu item = new Menu();
          item.setId(rs.getLong("id"));
          item.setRestaurantId(rs.getLong("restaurant_id"));
          item.setName(rs.getString("name"));
          item.setDescription(rs.getString("description"));
          item.setPrice(rs.getDouble("price"));
          item.setAvailable(rs.getBoolean("is_available"));
          return item;
        } else {
          throw new IllegalArgumentException("Блюдо с ID " + itemId + " не найдено в ресторане " + restaurantId);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Ошибка при получении блюда: " + e.getMessage(), e);
    }
  }
}