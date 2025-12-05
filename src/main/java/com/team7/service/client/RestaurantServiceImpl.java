package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantServiceImpl implements RestaurantService {

    public RestaurantServiceImpl() {
        // Конструктор без параметров
    }

    @Override
    public List<Restaurant> getRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM client_restaurants WHERE is_active = true";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Restaurant restaurant = new Restaurant();
                restaurant.setId(rs.getLong("id"));
                restaurant.setName(rs.getString("name"));
                restaurant.setAddress(rs.getString("address"));
                restaurant.setCuisineType(rs.getString("cuisine_type"));
                restaurant.setRating(rs.getDouble("rating"));
                restaurant.setDeliveryTime(rs.getInt("delivery_time"));
                restaurant.setMinOrderAmount(rs.getDouble("min_order_amount"));
                restaurant.setIsActive(rs.getBoolean("is_active"));
                restaurants.add(restaurant);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении ресторанов: " + e.getMessage(), e);
        }

        return restaurants;
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        String sql = "SELECT * FROM client_restaurants WHERE id = ? AND is_active = true";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Restaurant restaurant = new Restaurant();
                restaurant.setId(rs.getLong("id"));
                restaurant.setName(rs.getString("name"));
                restaurant.setAddress(rs.getString("address"));
                restaurant.setCuisineType(rs.getString("cuisine_type"));
                restaurant.setRating(rs.getDouble("rating"));
                restaurant.setDeliveryTime(rs.getInt("delivery_time"));
                restaurant.setMinOrderAmount(rs.getDouble("min_order_amount"));
                restaurant.setIsActive(rs.getBoolean("is_active"));
                return restaurant;
            } else {
                throw new IllegalArgumentException("Ресторан не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении ресторана: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime) {
        List<Restaurant> restaurants = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM client_restaurants WHERE is_active = true");

        List<Object> params = new ArrayList<>();

        if (rating != null) {
            sql.append(" AND rating >= ?");
            params.add(rating);
        }

        if (deliveryTime != null) {
            sql.append(" AND delivery_time <= ?");
            params.add(deliveryTime);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Restaurant restaurant = new Restaurant();
                restaurant.setId(rs.getLong("id"));
                restaurant.setName(rs.getString("name"));
                restaurant.setAddress(rs.getString("address"));
                restaurant.setCuisineType(rs.getString("cuisine_type"));
                restaurant.setRating(rs.getDouble("rating"));
                restaurant.setDeliveryTime(rs.getInt("delivery_time"));
                restaurant.setMinOrderAmount(rs.getDouble("min_order_amount"));
                restaurant.setIsActive(rs.getBoolean("is_active"));
                restaurants.add(restaurant);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при фильтрации ресторанов: " + e.getMessage(), e);
        }

        return restaurants;
    }

    @Override
    public List<Menu> getMenu(Long restaurantId) {
        List<Menu> menu = new ArrayList<>();
        String sql = "SELECT * FROM client_menu WHERE restaurant_id = ? AND is_available = true";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();

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
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении меню: " + e.getMessage(), e);
        }

        return menu;
    }
}