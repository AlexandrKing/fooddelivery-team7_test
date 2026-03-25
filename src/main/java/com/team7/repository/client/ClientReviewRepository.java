package com.team7.repository.client;

import com.team7.model.client.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ClientReviewRepository {
  private final JdbcTemplate jdbcTemplate;

  public ClientReviewRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Deprecated(forRemoval = false, since = "1.1")
  public ClientReviewRepository() {
    DataSource ds = DatabaseConfigDataSource.createFallbackDataSource();
    this.jdbcTemplate = new JdbcTemplate(ds);
  }

  public Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment) {
    Long userId = 1L;
    Long restaurantId = 1L;
    Long courierId = 1L;
    LocalDateTime createdAt = LocalDateTime.now();

    String sql = "INSERT INTO reviews (order_id, user_id, restaurant_id, courier_id, restaurant_rating, courier_rating, comment, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, orderId);
      ps.setLong(2, userId);
      ps.setLong(3, restaurantId);
      ps.setLong(4, courierId);
      if (restaurantRating != null) {
        ps.setInt(5, restaurantRating);
      } else {
        ps.setNull(5, java.sql.Types.INTEGER);
      }
      if (courierRating != null) {
        ps.setInt(6, courierRating);
      } else {
        ps.setNull(6, java.sql.Types.INTEGER);
      }
      ps.setString(7, comment);
      ps.setTimestamp(8, java.sql.Timestamp.valueOf(createdAt));
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new RuntimeException("Не удалось создать отзыв");
    }

    Review review = new Review();
    review.setId(key.longValue());
    review.setOrderId(orderId);
    review.setUserId(userId);
    review.setRestaurantId(restaurantId);
    review.setCourierId(courierId);
    review.setRestaurantRating(restaurantRating);
    review.setCourierRating(courierRating);
    review.setComment(comment);
    review.setCreatedAt(createdAt);
    return review;
  }

  public List<Review> getReviews(Long userId) {
    return jdbcTemplate.query(
        "SELECT * FROM reviews WHERE user_id = ?",
        (rs, rowNum) -> new Review(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getLong("user_id"),
            rs.getLong("restaurant_id"),
            rs.getLong("courier_id"),
            rs.getObject("restaurant_rating", Integer.class),
            rs.getObject("courier_rating", Integer.class),
            rs.getString("comment"),
            rs.getTimestamp("created_at").toLocalDateTime()
        ),
        userId
    );
  }

  public Double getRestaurantRating(Long restaurantId) {
    Double avg = jdbcTemplate.queryForObject(
        "SELECT AVG(restaurant_rating) FROM reviews WHERE restaurant_id = ? AND restaurant_rating IS NOT NULL",
        Double.class,
        restaurantId
    );
    return avg == null ? 0.0 : avg;
  }

  public Double getCourierRating(Long courierId) {
    Double avg = jdbcTemplate.queryForObject(
        "SELECT AVG(courier_rating) FROM reviews WHERE courier_id = ? AND courier_rating IS NOT NULL",
        Double.class,
        courierId
    );
    return avg == null ? 0.0 : avg;
  }
}

