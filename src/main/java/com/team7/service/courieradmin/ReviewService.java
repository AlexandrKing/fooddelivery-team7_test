package com.team7.service.courieradmin;

import com.team7.model.review.Review;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения отзывов: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public List<Review> getActiveReviews() {
        // В вашей таблице reviews нет колонки is_active
        // Поэтому возвращаем все отзывы или используем другой критерий
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения активных отзывов: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public List<Review> getReviewsByRestaurantId(Long restaurantId) {
        List<Review> reviews = new ArrayList<>();
        // В вашей таблице есть restaurant_rating
        String sql = "SELECT * FROM reviews WHERE restaurant_id = ? ORDER BY restaurant_rating DESC, created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения отзывов ресторана: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public List<Review> getReviewsByCourierId(Long courierId) {
        List<Review> reviews = new ArrayList<>();
        // В вашей таблице есть courier_rating
        String sql = "SELECT * FROM reviews WHERE courier_id = ? ORDER BY courier_rating DESC, created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения отзывов курьера: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public Review getReviewById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, reviewId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReview(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения отзыва: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean deactivateReview(Long reviewId) {
        // В вашей таблице reviews нет колонки is_active
        // Можно добать ее или удалить отзыв
        String sql = "DELETE FROM reviews WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, reviewId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка деактивации отзыва: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean activateReview(Long reviewId) {
        // В вашей таблице reviews нет колонки is_active
        // Не применимо в текущей схеме
        System.out.println("Внимание: таблица reviews не имеет колонки is_active");
        return false;
    }

    public boolean deleteReview(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, reviewId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка удаления отзыва: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public double getAverageRatingForRestaurant(Long restaurantId) {
        String sql = "SELECT AVG(restaurant_rating) as avg_rating FROM reviews WHERE restaurant_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения среднего рейтинга: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    public double getAverageRatingForCourier(Long courierId) {
        String sql = "SELECT AVG(courier_rating) as avg_rating FROM reviews WHERE courier_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, courierId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения среднего рейтинга: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setOrderId(rs.getLong("order_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setRestaurantId(rs.getLong("restaurant_id"));

        Long courierId = rs.getLong("courier_id");
        if (!rs.wasNull()) {
            review.setCourierId(courierId);
        }

        // В вашей таблице есть restaurant_rating и courier_rating
        // Используем restaurant_rating как основной рейтинг
        Integer rating = rs.getInt("restaurant_rating");
        if (rs.wasNull()) {
            rating = rs.getInt("courier_rating");
        }
        review.setRating(rating != null ? rating : 0);

        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        // В вашей таблице нет is_active, устанавливаем true по умолчанию
        review.setIsActive(true);

        return review;
    }
}