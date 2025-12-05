package com.team7.service.client;

import com.team7.model.client.Review;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewServiceImpl implements ReviewService {

    public ReviewServiceImpl() {
        // Конструктор без параметров
    }

    @Override
    public Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment) {
        if (restaurantRating != null && (restaurantRating < 1 || restaurantRating > 5)) {
            throw new IllegalArgumentException("Рейтинг ресторана должен быть от 1 до 5");
        }

        if (courierRating != null && (courierRating < 1 || courierRating > 5)) {
            throw new IllegalArgumentException("Рейтинг курьера должен быть от 1 до 5");
        }

        String sql = "INSERT INTO reviews (order_id, user_id, restaurant_id, courier_id, restaurant_rating, courier_rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, orderId);
            pstmt.setLong(2, 1L); // TODO: Получить user_id из текущей сессии
            pstmt.setLong(3, 1L); // TODO: Получить restaurant_id из заказа
            pstmt.setLong(4, 1L); // TODO: Получить courier_id из заказа

            if (restaurantRating != null) {
                pstmt.setInt(5, restaurantRating);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (courierRating != null) {
                pstmt.setInt(6, courierRating);
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, comment);
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Review review = new Review();
                        review.setId(rs.getLong(1));
                        review.setOrderId(orderId);
                        review.setUserId(1L);
                        review.setRestaurantId(1L);
                        review.setCourierId(1L);
                        review.setRestaurantRating(restaurantRating);
                        review.setCourierRating(courierRating);
                        review.setComment(comment);
                        review.setCreatedAt(LocalDateTime.now());
                        return review;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании отзыва: " + e.getMessage(), e);
        }

        throw new RuntimeException("Не удалось создать отзыв");
    }

    @Override
    public List<Review> getReviews(Long userId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getLong("id"));
                    review.setOrderId(rs.getLong("order_id"));
                    review.setUserId(rs.getLong("user_id"));
                    review.setRestaurantId(rs.getLong("restaurant_id"));
                    review.setCourierId(rs.getLong("courier_id"));
                    review.setRestaurantRating(rs.getObject("restaurant_rating", Integer.class));
                    review.setCourierRating(rs.getObject("courier_rating", Integer.class));
                    review.setComment(rs.getString("comment"));
                    review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении отзывов: " + e.getMessage(), e);
        }

        return reviews;
    }

    @Override
    public Double getRestaurantRating(Long restaurantId) {
        String sql = "SELECT AVG(restaurant_rating) as avg_rating FROM reviews WHERE restaurant_id = ? AND restaurant_rating IS NOT NULL";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, restaurantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avgRating = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avgRating;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении рейтинга ресторана: " + e.getMessage(), e);
        }

        return 0.0;
    }

    @Override
    public Double getCourierRating(Long courierId) {
        String sql = "SELECT AVG(courier_rating) as avg_rating FROM reviews WHERE courier_id = ? AND courier_rating IS NOT NULL";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, courierId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avgRating = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avgRating;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении рейтинга курьера: " + e.getMessage(), e);
        }

        return 0.0;
    }
}