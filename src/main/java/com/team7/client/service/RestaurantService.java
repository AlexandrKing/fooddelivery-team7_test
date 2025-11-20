package com.team7.client.service;

import com.team7.client.model.Restaurant;

import java.util.List;

public interface RestaurantService {
    List<Restaurant> getRestaurants();
    Restaurant getRestaurantById(String id);
    List<Restaurant> filterRestaurants(String cuisine, Double rating, Integer deliveryTime);
    List<String> getAvailableCuisines();
}
