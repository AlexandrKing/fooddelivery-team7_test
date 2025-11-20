package com.team7.client.service;

import com.team7.client.model.Restaurant;

import java.util.List;

public interface RestaurantService {
    List<Restaurant> getRestaurants();
    Restaurant getRestaurantById(Long id);
    List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime);
}
