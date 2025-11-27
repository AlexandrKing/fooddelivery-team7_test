package com.team7.restaurant.service;

import com.team7.restaurant.api.MenuOperations;
import com.team7.restaurant.config.DatabaseConfig;
import com.team7.restaurant.model.Dish;
import com.team7.restaurant.model.MenuCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuService implements MenuOperations {

  @Override
  public Dish addDishToMenu(Long restaurantId, Dish dish) {
    String sql = "INSERT INTO dish (name, description, price, available, restaurant_id) VALUES (?, ?, ?, ?, ?) RETURNING *";

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
  public void removeDishFromMenu(Long restaurantId, Long dishId) {
    String sql = "DELETE FROM dish WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, dishId);
      stmt.setLong(2, restaurantId);
      stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateDish(Long restaurantId, Dish updatedDish) {
    String sql = "UPDATE dish SET name = ?, description = ?, price = ? WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, updatedDish.getName());
      stmt.setString(2, updatedDish.getDescription());
      stmt.setBigDecimal(3, updatedDish.getPrice());
      stmt.setLong(4, updatedDish.getId());
      stmt.setLong(5, restaurantId);

      stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void toggleDishAvailability(Long restaurantId, Long dishId) {
    String sql = "UPDATE dish SET available = NOT available WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, dishId);
      stmt.setLong(2, restaurantId);
      stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<Dish> getMenuByRestaurantId(Long restaurantId) {
    List<Dish> dishes = new ArrayList<>();
    String sql = "SELECT * FROM dish WHERE restaurant_id = ?";

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
    String sql = "SELECT * FROM dish WHERE restaurant_id = ? AND available = true";

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
    dish.setName(rs.getString("name"));
    dish.setDescription(rs.getString("description"));
    dish.setPrice(rs.getBigDecimal("price"));
    dish.setAvailable(rs.getBoolean("available"));
    dish.setRestaurantId(rs.getLong("restaurant_id"));
    return dish;
  }

  // Эти методы можно оставить пустыми или удалить, если не используются
  public MenuCategory createCategory(Long restaurantId, String name, String description) {
    return null;
  }

  public void deleteCategory(Long restaurantId, Long categoryId) {
  }

  public List<MenuCategory> getCategoriesByRestaurantId(Long restaurantId) {
    return new ArrayList<>();
  }

  public void addDishToCategory(Long restaurantId, Long categoryId, Dish dish) {
  }
}
