package com.team7.client.service;

import com.team7.client.model.Menu;
import com.team7.client.model.Restaurant;
import java.util.*;

public class RestaurantServiceImpl implements RestaurantService {
    private static final Map<Long, Restaurant> RESTAURANTS = new HashMap<>();

    @Override
    public List<Restaurant> getRestaurants() {
        return new ArrayList<>(RESTAURANTS.values());
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        Restaurant restaurant = RESTAURANTS.get(id);
        if (restaurant == null) {
            throw new IllegalArgumentException("Ресторан не найден");
        }
        return restaurant;
    }

    @Override
    public List<Restaurant> filterRestaurants(Double minRating, Integer maxDeliveryTime) {
        return RESTAURANTS.values().stream()
                .filter(restaurant -> minRating == null || restaurant.getRating() >= minRating)
                .filter(restaurant -> maxDeliveryTime == null || restaurant.getDeliveryTime() <= maxDeliveryTime)
                .filter(Restaurant::getIsActive)
                .toList();
    }

    @Override
    public List<Menu> getMenu(Long restaurantId) {
        return List.of();
    }


    public void addRestaurant(Restaurant restaurant) {
        RESTAURANTS.put(restaurant.getId(), restaurant);
    }
}
