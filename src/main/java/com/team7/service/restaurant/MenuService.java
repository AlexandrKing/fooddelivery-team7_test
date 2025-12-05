package com.team7.service.restaurant;

import com.team7.service.config.DatabaseConfig;
import com.team7.model.restaurant.Dish;
import com.team7.model.restaurant.MenuCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuService implements MenuOperations {

  @Override
  public Dish addDishToMenu(Long restaurantId, Dish dish) {
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "INSERT INTO dishes (name, description, price, is_available, restaurant_id, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING *";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, dish.getName());
      stmt.setString(2, dish.getDescription());
      stmt.setBigDecimal(3, dish.getPrice());
      stmt.setBoolean(4, dish.getAvailable());
      stmt.setLong(5, restaurantId);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToDish(rs);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean removeDishFromMenu(Long restaurantId, Long dishId) {
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "DELETE FROM dishes WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, dishId);
      stmt.setLong(2, restaurantId);
      int rowsDeleted = stmt.executeUpdate();
      return rowsDeleted > 0;

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean updateDish(Long restaurantId, Dish updatedDish) {
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "UPDATE dishes SET name = ?, description = ?, price = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, updatedDish.getName());
      stmt.setString(2, updatedDish.getDescription());
      stmt.setBigDecimal(3, updatedDish.getPrice());
      stmt.setLong(4, updatedDish.getId());
      stmt.setLong(5, restaurantId);

      int rowsUpdated = stmt.executeUpdate();
      return rowsUpdated > 0;

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean toggleDishAvailability(Long restaurantId, Long dishId) {
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "UPDATE dishes SET is_available = NOT is_available, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, dishId);
      stmt.setLong(2, restaurantId);
      int rowsUpdated = stmt.executeUpdate();
      return rowsUpdated > 0;

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public List<Dish> getMenuByRestaurantId(Long restaurantId) {
    List<Dish> dishes = new ArrayList<>();
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "SELECT * FROM dishes WHERE restaurant_id = ? ORDER BY name";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        dishes.add(mapResultSetToDish(rs));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return dishes;
  }

  @Override
  public List<Dish> getAvailableDishes(Long restaurantId) {
    List<Dish> dishes = new ArrayList<>();
    // ИСПРАВЛЕНО: было dish, стало dishes
    String sql = "SELECT * FROM dishes WHERE restaurant_id = ? AND is_available = true ORDER BY name";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        dishes.add(mapResultSetToDish(rs));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return dishes;
  }

  private Dish mapResultSetToDish(ResultSet rs) throws SQLException {
    Dish dish = new Dish();
    dish.setId(rs.getLong("id"));
    System.out.println("DEBUG: Mapped dish ID = " + dish.getId());
    dish.setName(rs.getString("name"));
    dish.setDescription(rs.getString("description"));
    dish.setPrice(rs.getBigDecimal("price"));
    dish.setAvailable(rs.getBoolean("is_available"));
    dish.setRestaurantId(rs.getLong("restaurant_id"));

    // Добавляем дополнительные поля из вашей таблицы dishes
    dish.setCategory(rs.getString("category"));
    dish.setMenuCategoryId(rs.getLong("menu_category_id"));
    if (rs.wasNull()) {
      dish.setMenuCategoryId(null);
    }
    dish.setAvailableQuantity(rs.getInt("available_quantity"));
    if (rs.wasNull()) {
      dish.setAvailableQuantity(null);
    }
    dish.setPreparationTimeMin(rs.getInt("preparation_time_min"));
    dish.setCalories(rs.getInt("calories"));
    if (rs.wasNull()) {
      dish.setCalories(null);
    }
    dish.setVegetarian(rs.getBoolean("is_vegetarian"));
    dish.setSpicy(rs.getBoolean("is_spicy"));
    dish.setImageUrl(rs.getString("image_url"));

    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      dish.setCreatedAt(createdAt.toLocalDateTime());
    }

    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      dish.setUpdatedAt(updatedAt.toLocalDateTime());
    }

    return dish;
  }

  public MenuCategory createCategory(Long restaurantId, String name, String description) {
    // ИСПРАВЛЕНО: было menu_categories, но это правильно
    String sql = "INSERT INTO menu_categories (name, description, restaurant_id, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING *";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, name);
      stmt.setString(2, description);
      stmt.setLong(3, restaurantId);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        MenuCategory category = new MenuCategory();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setRestaurantId(rs.getLong("restaurant_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
          category.setCreatedAt(createdAt.toLocalDateTime());
        }

        return category;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void deleteCategory(Long restaurantId, Long categoryId) {
    String sql = "DELETE FROM menu_categories WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, categoryId);
      stmt.setLong(2, restaurantId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<MenuCategory> getCategoriesByRestaurantId(Long restaurantId) {
    List<MenuCategory> categories = new ArrayList<>();
    String sql = "SELECT * FROM menu_categories WHERE restaurant_id = ? ORDER BY name";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        MenuCategory category = new MenuCategory();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setRestaurantId(rs.getLong("restaurant_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
          category.setCreatedAt(createdAt.toLocalDateTime());
        }

        categories.add(category);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return categories;
  }

  public void addDishToCategory(Long restaurantId, Long categoryId, Dish dish) {
    String sql = "UPDATE dishes SET menu_category_id = ? WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, categoryId);
      stmt.setLong(2, dish.getId());
      stmt.setLong(3, restaurantId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}