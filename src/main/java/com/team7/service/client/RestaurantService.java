package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;

import java.util.List;

public interface RestaurantService {
    List<Restaurant> getRestaurants();
    Restaurant getRestaurantById(Long id);
    List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime);

    List<Menu> getMenu(Long restaurantId);
}
