package com.team7.service.courieradmin;

import com.team7.service.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantService {

    public static class Restaurant {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String cuisineType;
        private String description;
        private String status;
        private Double rating;
        private Boolean isActive;
        private LocalDateTime createdAt;

        public Restaurant() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCuisineType() { return cuisineType; }
        public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM restaurants ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                restaurants.add(mapResultSetToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения ресторанов: " + e.getMessage());
        }

        return restaurants;
    }

    public Restaurant getRestaurantById(Long restaurantId) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRestaurant(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения ресторана: " + e.getMessage());
        }

        return null;
    }

    public boolean deactivateRestaurant(Long restaurantId) {
        String sql = "UPDATE restaurants SET is_active = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка деактивации ресторана: " + e.getMessage());
        }

        return false;
    }

    public boolean activateRestaurant(Long restaurantId) {
        String sql = "UPDATE restaurants SET is_active = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка активации ресторана: " + e.getMessage());
        }

        return false;
    }

    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(rs.getLong("id"));
        restaurant.setName(rs.getString("name"));
        restaurant.setEmail(rs.getString("email"));
        restaurant.setPhone(rs.getString("phone"));
        restaurant.setAddress(rs.getString("address"));
        restaurant.setCuisineType(rs.getString("cuisine_type"));
        restaurant.setDescription(rs.getString("description"));
        restaurant.setStatus(rs.getString("status"));
        restaurant.setRating(rs.getDouble("rating"));
        restaurant.setIsActive(rs.getBoolean("is_active"));
        restaurant.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return restaurant;
    }
}