package com.team7.service.restaurant;

import com.team7.service.config.DatabaseConfig;
import com.team7.model.restaurant.Dish;
import com.team7.model.restaurant.MenuCategory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuService implements MenuOperations {

  @Override
  public Dish addDishToMenu(Long restaurantId, Dish dish) {
    // ИСПРАВЛЕНО: Добавлено указание, что id будет сгенерировано автоматически
    String sql = "INSERT INTO dishes (restaurant_id, name, description, price, is_available, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING *";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      // Параметры в правильном порядке
      stmt.setLong(1, restaurantId);      // restaurant_id
      stmt.setString(2, dish.getName());   // name
      stmt.setString(3, dish.getDescription()); // description
      stmt.setBigDecimal(4, dish.getPrice());   // price
      stmt.setBoolean(5, dish.getAvailable());  // is_available

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToDish(rs);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println("❌ Ошибка при добавлении блюда: " + e.getMessage());
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
    // Сначала получим текущее блюдо
    String getSql = "SELECT * FROM dishes WHERE id = ? AND restaurant_id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement getStmt = conn.prepareStatement(getSql)) {

      getStmt.setLong(1, updatedDish.getId());
      getStmt.setLong(2, restaurantId);
      ResultSet rs = getStmt.executeQuery();

      if (!rs.next()) {
        return false; // Блюдо не найдено
      }

      // Используем текущие значения для полей, которые не указаны
      String currentName = rs.getString("name");
      String currentDescription = rs.getString("description");
      BigDecimal currentPrice = rs.getBigDecimal("price");

      // Обновляем только указанные поля
      String name = (updatedDish.getName() != null && !updatedDish.getName().trim().isEmpty())
          ? updatedDish.getName()
          : currentName;
      String description = (updatedDish.getDescription() != null)
          ? updatedDish.getDescription()
          : currentDescription;
      BigDecimal price = (updatedDish.getPrice() != null)
          ? updatedDish.getPrice()
          : currentPrice;

      // Теперь выполняем обновление
      String updateSql = "UPDATE dishes SET name = ?, description = ?, price = ?, updated_at = CURRENT_TIMESTAMP " +
          "WHERE id = ? AND restaurant_id = ?";

      try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
        updateStmt.setString(1, name);
        updateStmt.setString(2, description);
        updateStmt.setBigDecimal(3, price);
        updateStmt.setLong(4, updatedDish.getId());
        updateStmt.setLong(5, restaurantId);

        int rowsUpdated = updateStmt.executeUpdate();
        return rowsUpdated > 0;
      }

    } catch (SQLException e) {
      System.err.println("❌ Ошибка при обновлении блюда: " + e.getMessage());
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

  public List<MenuCategory> getMenuCategoriesByRestaurantId(Long restaurantId) {
    List<MenuCategory> categories = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection()) {
      String sql = "SELECT id, name, description, created_at FROM menu_categories WHERE restaurant_id = ? ORDER BY name";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, restaurantId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          MenuCategory category = new MenuCategory();
          category.setId(rs.getLong("id"));
          category.setName(rs.getString("name"));
          category.setDescription(rs.getString("description"));
          category.setRestaurantId(restaurantId);
          try {
            category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
          } catch (Exception e) {
            // Игнорируем если поля нет
          }
          categories.add(category);
        }
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при получении категорий меню: " + e.getMessage());
      e.printStackTrace();
    }
    return categories;
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
  // Добавьте в конец класса MenuService, но перед последней закрывающей фигурной скобкой

  public MenuCategory addMenuCategory(Long restaurantId, MenuCategory category) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      String sql = "INSERT INTO menu_categories (name, description, restaurant_id, created_at) " +
          "VALUES (?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, category.getName());
        pstmt.setString(2, category.getDescription());
        pstmt.setLong(3, restaurantId);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          category.setId(rs.getLong("id"));
          return category;
        }
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при добавлении категории: " + e.getMessage());
    }
    return null;
  }

  public boolean removeMenuCategory(Long restaurantId, Long categoryId) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      // Проверяем, есть ли блюда в этой категории
      String checkDishesSQL = "SELECT COUNT(*) FROM dishes WHERE restaurant_id = ? AND menu_category_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(checkDishesSQL)) {
        pstmt.setLong(1, restaurantId);
        pstmt.setLong(2, categoryId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
          // Если есть блюда, устанавливаем menu_category_id в NULL
          String updateDishesSQL = "UPDATE dishes SET menu_category_id = NULL WHERE restaurant_id = ? AND menu_category_id = ?";
          try (PreparedStatement updateStmt = conn.prepareStatement(updateDishesSQL)) {
            updateStmt.setLong(1, restaurantId);
            updateStmt.setLong(2, categoryId);
            updateStmt.executeUpdate();
          }
        }
      }

      // Удаляем категорию
      String deleteCategorySQL = "DELETE FROM menu_categories WHERE id = ? AND restaurant_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(deleteCategorySQL)) {
        pstmt.setLong(1, categoryId);
        pstmt.setLong(2, restaurantId);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при удалении категории: " + e.getMessage());
      return false;
    }
  }

  public boolean updateMenuCategory(Long restaurantId, MenuCategory category) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      String sql = "UPDATE menu_categories SET name = ?, description = ? WHERE id = ? AND restaurant_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, category.getName());
        pstmt.setString(2, category.getDescription());
        pstmt.setLong(3, category.getId());
        pstmt.setLong(4, restaurantId);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при обновлении категории: " + e.getMessage());
      return false;
    }
  }

  public List<Dish> getDishesByCategory(Long restaurantId, Long categoryId) {
    List<Dish> dishes = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection()) {
      String sql = "SELECT * FROM dishes WHERE restaurant_id = ? AND menu_category_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, restaurantId);
        pstmt.setLong(2, categoryId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          Dish dish = new Dish();
          dish.setId(rs.getLong("id"));
          dish.setName(rs.getString("name"));
          dish.setDescription(rs.getString("description"));
          dish.setPrice(rs.getBigDecimal("price"));
          dish.setAvailable(rs.getBoolean("is_available"));
          dishes.add(dish);
        }
      }
    } catch (SQLException e) {
      System.err.println("❌ Ошибка при получении блюд по категории: " + e.getMessage());
    }
    return dishes;
  }
}