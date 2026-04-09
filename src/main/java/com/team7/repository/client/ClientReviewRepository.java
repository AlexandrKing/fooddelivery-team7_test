package com.team7.repository.client;

import com.team7.model.client.Review;
import com.team7.persistence.ReviewEntityMappings;
import com.team7.persistence.ReviewJpaRepository;
import com.team7.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ClientReviewRepository {
  private final ReviewJpaRepository reviewJpaRepository;

  public ClientReviewRepository(ReviewJpaRepository reviewJpaRepository) {
    this.reviewJpaRepository = reviewJpaRepository;
  }

  public Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment) {
    Long userId = 1L;
    Long restaurantId = 1L;
    Long courierId = 1L;
    LocalDateTime createdAt = LocalDateTime.now();

    ReviewEntity entity = new ReviewEntity();
    entity.setOrderId(orderId);
    entity.setUserId(userId);
    entity.setRestaurantId(restaurantId);
    entity.setCourierId(courierId);
    entity.setRestaurantRating(restaurantRating);
    entity.setCourierRating(courierRating);
    entity.setComment(comment);
    entity.setCreatedAt(createdAt);
    ReviewEntity saved = reviewJpaRepository.save(entity);
    return ReviewEntityMappings.toDto(saved);
  }

  public List<Review> getReviews(Long userId) {
    return reviewJpaRepository.findByUserId(userId).stream()
        .map(ReviewEntityMappings::toDto)
        .collect(Collectors.toList());
  }

  public Double getRestaurantRating(Long restaurantId) {
    Double avg = reviewJpaRepository.averageRestaurantRatingByRestaurantId(restaurantId);
    return avg == null ? 0.0 : avg;
  }

  public Double getCourierRating(Long courierId) {
    Double avg = reviewJpaRepository.averageCourierRatingByCourierId(courierId);
    return avg == null ? 0.0 : avg;
  }
}
