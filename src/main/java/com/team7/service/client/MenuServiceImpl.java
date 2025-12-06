package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuServiceImpl implements MenuService {

  public MenuServiceImpl() {
    // Конструктор без параметров
  }

  @Override
  public List<Menu> getMenu(Long restaurantId) {
    List<Menu> menu = new ArrayList<>();
    String sql = "SELECT * FROM dishes WHERE restaurant_id = ? AND is_available = true";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, restaurantId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Menu item = new Menu();
          item.setId(rs.getLong("id"));
          item.setRestaurantId(rs.getLong("restaurant_id"));
          item.setName(rs.getString("name"));
          item.setDescription(rs.getString("description"));
          item.setPrice(rs.getDouble("price"));
          item.setAvailable(rs.getBoolean("is_available"));

          // Опциональные поля
          String category = rs.getString("category");
          if (category != null) item.setCategory(category);

          Integer calories = rs.getInt("calories");
          if (!rs.wasNull()) item.setCalories(calories);

          String imageUrl = rs.getString("image_url");
          if (imageUrl != null) item.setImageUrl(imageUrl);

          Integer preparationTimeMin = rs.getInt("preparation_time_min");
          if (!rs.wasNull()) item.setCookingTime(preparationTimeMin);

          menu.add(item);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Ошибка при получении меню ресторана: " + restaurantId, e);
    }
    return menu;
  }

  @Override
  public Menu getMenuItem(Long restaurantId, Long itemId) {
    String sql = "SELECT * FROM dishes WHERE restaurant_id = ? AND id = ? AND is_available = true";

    try (Connection conn = DatabaseConfig.getConnection();
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

          // Опциональные поля
          String category = rs.getString("category");
          if (category != null) item.setCategory(category);

          Integer calories = rs.getInt("calories");
          if (!rs.wasNull()) item.setCalories(calories);

          String imageUrl = rs.getString("image_url");
          if (imageUrl != null) item.setImageUrl(imageUrl);

          Integer preparationTimeMin = rs.getInt("preparation_time_min");
          if (!rs.wasNull()) item.setCookingTime(preparationTimeMin);

          return item;
        } else {
          throw new IllegalArgumentException("Блюдо не найдено в меню ресторана");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Ошибка при получении блюда: " + itemId + " для ресторана: " + restaurantId, e);
    }
  }
}