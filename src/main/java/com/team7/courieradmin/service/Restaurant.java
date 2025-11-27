package com.team7.courieradmin.service;

import java.time.Instant;

public class Restaurant {
    private Long restaurantId;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String phone;
    private String email;
    private Double rating;
    private Boolean isActive;
    private Instant createdAt;

    public Restaurant() {}

    public Restaurant(Long restaurantId, String name, String description, String cuisine,
                      String address, String phone, String email) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.cuisine = cuisine;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.rating = 0.0;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Restaurant{" +
                "restaurantId=" + restaurantId +
                ", name='" + name + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", address='" + address + '\'' +
                ", rating=" + rating +
                ", isActive=" + isActive +
                '}';
    }
}
