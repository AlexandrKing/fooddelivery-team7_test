package com.team7.service.client;

import com.team7.model.client.Review;
import java.time.LocalDateTime;
import java.util.*;

public class ReviewServiceImpl implements ReviewService {
    private static final List<Review> REVIEWS = new ArrayList<>();
    private static Long reviewIdCounter = 1L;

    @Override
    public Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment) {
        if (restaurantRating != null && (restaurantRating < 1 || restaurantRating > 5)) {
            throw new IllegalArgumentException("Рейтинг ресторана должен быть от 1 до 5");
        }

        if (courierRating != null && (courierRating < 1 || courierRating > 5)) {
            throw new IllegalArgumentException("Рейтинг курьера должен быть от 1 до 5");
        }

        Review review = new Review();
        review.setId(reviewIdCounter++);
        review.setOrderId(orderId);
        review.setUserId(1L);
        review.setRestaurantId(1L);
        review.setCourierId(1L);
        review.setRestaurantRating(restaurantRating);
        review.setCourierRating(courierRating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        REVIEWS.add(review);
        return review;
    }

    @Override
    public List<Review> getReviews(Long userId) {
        return REVIEWS.stream()
                .filter(review -> Objects.equals(review.getUserId(), userId))
                .toList();
    }

    @Override
    public Double getRestaurantRating(Long restaurantId) {
        List<Integer> ratings = REVIEWS.stream()
                .filter(review -> Objects.equals(review.getRestaurantId(), restaurantId))
                .map(Review::getRestaurantRating)
                .filter(Objects::nonNull)
                .toList();

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    @Override
    public Double getCourierRating(Long courierId) {
        List<Integer> ratings = REVIEWS.stream()
                .filter(review -> Objects.equals(review.getCourierId(), courierId))
                .map(Review::getCourierRating)
                .filter(Objects::nonNull)
                .toList();

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }
}