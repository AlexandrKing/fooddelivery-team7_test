package com.team7.repository.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import com.team7.persistence.DishEntityMappings;
import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.RestaurantJpaRepository;
import com.team7.persistence.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RestaurantRepository {
  private final RestaurantJpaRepository restaurantJpaRepository;
  private final DishJpaRepository dishJpaRepository;

  public RestaurantRepository(
      RestaurantJpaRepository restaurantJpaRepository,
      DishJpaRepository dishJpaRepository
  ) {
    this.restaurantJpaRepository = restaurantJpaRepository;
    this.dishJpaRepository = dishJpaRepository;
  }

  public List<Restaurant> getRestaurants() {
    return restaurantJpaRepository.findByIsActiveTrue().stream()
        .map(RestaurantRepository::toClientRestaurant)
        .collect(Collectors.toList());
  }

  public Restaurant getRestaurantById(Long id) {
    return restaurantJpaRepository.findByIdAndIsActiveTrue(id)
        .map(RestaurantRepository::toClientRestaurant)
        .orElseThrow(() -> new IllegalArgumentException("Ресторан не найден"));
  }

  public List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime) {
    return restaurantJpaRepository.findActiveFiltered(rating, deliveryTime).stream()
        .map(RestaurantRepository::toClientRestaurant)
        .collect(Collectors.toList());
  }

  public List<Menu> getMenu(Long restaurantId) {
    return dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
        .map(DishEntityMappings::toMenu)
        .collect(Collectors.toList());
  }

  private static Restaurant toClientRestaurant(RestaurantEntity e) {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(e.getId());
    restaurant.setName(e.getName());
    restaurant.setAddress(e.getAddress());
    restaurant.setLatitude(e.getLatitude());
    restaurant.setLongitude(e.getLongitude());
    restaurant.setCuisineType(e.getCuisineType());
    restaurant.setRating(e.getRating() != null ? e.getRating() : 0.0);
    restaurant.setDeliveryTime(e.getDeliveryTime() != null ? e.getDeliveryTime() : 0);
    restaurant.setMinOrderAmount(e.getMinOrderAmount() != null ? e.getMinOrderAmount() : 0.0);
    restaurant.setIsActive(e.getIsActive() != null ? e.getIsActive() : Boolean.FALSE);
    return restaurant;
  }
}
