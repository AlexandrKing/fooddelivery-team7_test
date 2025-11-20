package com.team7.client.model;

import java.time.LocalDateTime;

public class Review {
    private Long id;
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private Long courierId;
    private Integer restaurantRating;
    private Integer courierRating;
    private String comment;
    private LocalDateTime createdAt;
}
