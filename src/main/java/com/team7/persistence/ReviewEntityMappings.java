package com.team7.persistence;

import com.team7.model.client.Review;
import com.team7.persistence.entity.ReviewEntity;

public final class ReviewEntityMappings {

  private ReviewEntityMappings() {
  }

  public static Review toDto(ReviewEntity e) {
    return new Review(
        e.getId(),
        e.getOrderId(),
        e.getUserId(),
        e.getRestaurantId(),
        e.getCourierId(),
        e.getRestaurantRating(),
        e.getCourierRating(),
        e.getComment(),
        e.getCreatedAt()
    );
  }
}
