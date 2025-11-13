package com.team7.client.service;

import com.team7.client.model.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(String orderId, Integer restaurantRating, Integer courierRating, String comment);
    List<Review> getReviews(String userId);
    Double getRestaurantRating(String restaurantId);
}
