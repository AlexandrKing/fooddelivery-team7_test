package com.team7.client.service;

import com.team7.client.model.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment);
    List<Review> getReviews(Long userId);
    Double getRestaurantRating(Long restaurantId);
    Double getCourierRating(Long courierId);
}
