package com.team7.service.restaurant;

import com.team7.model.restaurant.Restaurant;

public interface RestaurantOperations {
  Restaurant updateRestaurant(Long restaurantId, String name, String phone,
                              String address, String cuisineType, String description);
  Restaurant getRestaurantById(Long restaurantId);
  Boolean updateEmail(Long restaurantId, String newEmail, String password);
  Boolean updatePhone(Long restaurantId, String newPhone);
}
