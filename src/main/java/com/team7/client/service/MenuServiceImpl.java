package com.team7.client.service;

import com.team7.client.model.*;
import java.sql.*;
import java.util.*;

public class MenuServiceImpl implements MenuService {
    private final DatabaseService dbService = new DatabaseService();

    @Override
    public List<Menu> getMenu(Long restaurantId) {
        List<Menu> menu = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE restaurant_id = ? AND is_available = true";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Menu menuItem = new Menu();
                menuItem.setId(rs.getLong("id"));
                menuItem.setName(rs.getString("name"));
                menuItem.setDescription(rs.getString("description"));
                menuItem.setPrice(rs.getDouble("price"));
                menuItem.setIsAvailable(rs.getBoolean("is_available"));
                menu.add(menuItem);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении меню: " + e.getMessage(), e);
        }
        return menu;
    }

    @Override
    public Menu getMenuItem(Long restaurantId, Long itemId) {
        String sql = "SELECT * FROM menu WHERE id = ? AND restaurant_id = ? AND is_available = true";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, itemId);
            pstmt.setLong(2, restaurantId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Menu menuItem = new Menu();
                menuItem.setId(rs.getLong("id"));
                menuItem.setName(rs.getString("name"));
                menuItem.setDescription(rs.getString("description"));
                menuItem.setPrice(rs.getDouble("price"));
                menuItem.setIsAvailable(rs.getBoolean("is_available"));
                return menuItem;
            } else {
                throw new IllegalArgumentException("Блюдо не найдено");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении блюда: " + e.getMessage(), e);
        }
    }

    // Дополнительный метод для получения меню по категории
    public List<Menu> getMenuByCategory(Long restaurantId, String category) {
        List<Menu> menu = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE restaurant_id = ? AND category = ? AND is_available = true";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, restaurantId);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Menu menuItem = new Menu();
                menuItem.setId(rs.getLong("id"));
                menuItem.setName(rs.getString("name"));
                menuItem.setDescription(rs.getString("description"));
                menuItem.setPrice(rs.getDouble("price"));
                menuItem.setIsAvailable(rs.getBoolean("is_available"));
                menu.add(menuItem);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении меню по категории: " + e.getMessage(), e);
        }
        return menu;
    }

    // Метод для получения всех категорий ресторана
    public List<String> getCategories(Long restaurantId) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM menu WHERE restaurant_id = ? AND is_available = true AND category IS NOT NULL";

        try (Connection conn = dbService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении категорий: " + e.getMessage(), e);
        }
        return categories;
    }
}