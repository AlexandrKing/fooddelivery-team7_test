package com.team7.service.courieadmin;
import java.time.Instant;

public class Review {
    private Long reviewId;
    private Long orderId;
    private Long clientId;
    private Long restaurantId;
    private Long courierId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Boolean isActive;

    public Review() {}

    public Review(Long reviewId, Long orderId, Long clientId, Long restaurantId,
                  Long courierId, Integer rating, String comment) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.clientId = clientId;
        this.restaurantId = restaurantId;
        this.courierId = courierId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", orderId=" + orderId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
