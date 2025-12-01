package com.team7.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
