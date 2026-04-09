package com.team7.service.client;

import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import com.team7.repository.client.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<Restaurant> getRestaurants() {
        return restaurantRepository.getRestaurants();
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.getRestaurantById(id);
    }

    @Override
    public List<Restaurant> filterRestaurants(Double rating, Integer deliveryTime) {
        return restaurantRepository.filterRestaurants(rating, deliveryTime);
    }

    @Override
    public List<Menu> getMenu(Long restaurantId) {
        return restaurantRepository.getMenu(restaurantId);
    }
}