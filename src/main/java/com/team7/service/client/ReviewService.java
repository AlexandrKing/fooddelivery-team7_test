package com.team7.service.client;

import com.team7.model.client.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment);
    List<Review> getReviews(Long userId);
    Double getRestaurantRating(Long restaurantId);
    Double getCourierRating(Long courierId);
}
