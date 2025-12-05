package com.team7.service.courieradmin;

import com.team7.model.review.Review;
import com.team7.service.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {

  public List<Review> getAllReviews() {
    List<Review> reviews = new ArrayList<>();
    String sql = "SELECT * FROM client_reviews ORDER BY created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения отзывов: " + e.getMessage());
    }

    return reviews;
  }

  public List<Review> getActiveReviews() {
    List<Review> reviews = new ArrayList<>();
    String sql = "SELECT * FROM client_reviews WHERE is_active = TRUE ORDER BY created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения активных отзывов: " + e.getMessage());
    }

    return reviews;
  }

  public List<Review> getReviewsByRestaurantId(Long restaurantId) {
    List<Review> reviews = new ArrayList<>();
    String sql = "SELECT * FROM client_reviews WHERE restaurant_id = ? AND is_active = TRUE ORDER BY rating DESC, created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения отзывов ресторана: " + e.getMessage());
    }

    return reviews;
  }

  public List<Review> getReviewsByCourierId(Long courierId) {
    List<Review> reviews = new ArrayList<>();
    String sql = "SELECT * FROM client_reviews WHERE courier_id = ? AND is_active = TRUE ORDER BY rating DESC, created_at DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения отзывов курьера: " + e.getMessage());
    }

    return reviews;
  }

  public Review getReviewById(Long reviewId) {
    String sql = "SELECT * FROM client_reviews WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, reviewId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToReview(rs);
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения отзыва: " + e.getMessage());
    }

    return null;
  }

  public boolean deactivateReview(Long reviewId) {
    String sql = "UPDATE client_reviews SET is_active = FALSE WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, reviewId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка деактивации отзыва: " + e.getMessage());
    }

    return false;
  }

  public boolean activateReview(Long reviewId) {
    String sql = "UPDATE client_reviews SET is_active = TRUE WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, reviewId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка активации отзыва: " + e.getMessage());
    }

    return false;
  }

  public boolean deleteReview(Long reviewId) {
    String sql = "DELETE FROM client_reviews WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, reviewId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Ошибка удаления отзыва: " + e.getMessage());
    }

    return false;
  }

  public double getAverageRatingForRestaurant(Long restaurantId) {
    String sql = "SELECT AVG(rating) as avg_rating FROM client_reviews WHERE restaurant_id = ? AND is_active = TRUE";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, restaurantId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getDouble("avg_rating");
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения среднего рейтинга: " + e.getMessage());
    }

    return 0.0;
  }

  public double getAverageRatingForCourier(Long courierId) {
    String sql = "SELECT AVG(rating) as avg_rating FROM client_reviews WHERE courier_id = ? AND is_active = TRUE";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getDouble("avg_rating");
      }

    } catch (SQLException e) {
      System.err.println("Ошибка получения среднего рейтинга: " + e.getMessage());
    }

    return 0.0;
  }

  private Review mapResultSetToReview(ResultSet rs) throws SQLException {
    Review review = new Review();
    review.setId(rs.getLong("id"));
    review.setOrderId(rs.getLong("order_id"));
    review.setUserId(rs.getLong("user_id"));
    review.setRestaurantId(rs.getLong("restaurant_id"));
    review.setCourierId(rs.getLong("courier_id"));
    review.setRating(rs.getInt("rating"));
    review.setComment(rs.getString("comment"));
    review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    review.setIsActive(rs.getBoolean("is_active"));
    return review;
  }
}