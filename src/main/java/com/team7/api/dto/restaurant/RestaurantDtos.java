package com.team7.api.dto.restaurant;

public final class RestaurantDtos {
  private RestaurantDtos() {
  }

  public static class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String cuisineType;
    private Double rating;
    private Integer deliveryTime;
    private Double minOrderAmount;
    private Boolean isActive;

    public RestaurantResponse() {
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public Double getLatitude() {
      return latitude;
    }

    public void setLatitude(Double latitude) {
      this.latitude = latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public void setLongitude(Double longitude) {
      this.longitude = longitude;
    }

    public String getCuisineType() {
      return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
      this.cuisineType = cuisineType;
    }

    public Double getRating() {
      return rating;
    }

    public void setRating(Double rating) {
      this.rating = rating;
    }

    public Integer getDeliveryTime() {
      return deliveryTime;
    }

    public void setDeliveryTime(Integer deliveryTime) {
      this.deliveryTime = deliveryTime;
    }

    public Double getMinOrderAmount() {
      return minOrderAmount;
    }

    public void setMinOrderAmount(Double minOrderAmount) {
      this.minOrderAmount = minOrderAmount;
    }

    public Boolean getIsActive() {
      return isActive;
    }

    public void setIsActive(Boolean active) {
      isActive = active;
    }
  }

  public record MenuItemResponse(
      Long id,
      Long restaurantId,
      String name,
      String description,
      Double price,
      Boolean available,
      String category,
      Integer calories,
      Double weight,
      String imageUrl,
      Integer cookingTime
  ) {
  }
}

