package com.team7.service.client;

import com.team7.model.client.Review;
import com.team7.repository.client.ClientReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ClientReviewRepository reviewRepository;

    public ReviewServiceImpl(ClientReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review createReview(Long orderId, Integer restaurantRating, Integer courierRating, String comment) {
        if (restaurantRating != null && (restaurantRating < 1 || restaurantRating > 5)) {
            throw new IllegalArgumentException("Рейтинг ресторана должен быть от 1 до 5");
        }

        if (courierRating != null && (courierRating < 1 || courierRating > 5)) {
            throw new IllegalArgumentException("Рейтинг курьера должен быть от 1 до 5");
        }

        return reviewRepository.createReview(orderId, restaurantRating, courierRating, comment);
    }

    @Override
    public List<Review> getReviews(Long userId) {
        return reviewRepository.getReviews(userId);
    }

    @Override
    public Double getRestaurantRating(Long restaurantId) {
        return reviewRepository.getRestaurantRating(restaurantId);
    }

    @Override
    public Double getCourierRating(Long courierId) {
        return reviewRepository.getCourierRating(courierId);
    }
}