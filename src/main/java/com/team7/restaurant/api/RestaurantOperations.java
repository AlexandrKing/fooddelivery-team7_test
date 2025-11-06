package com.team7.restaurant.api;

import com.team7.restaurant.model.Restaurant;

public interface RestaurantOperations {
  Restaurant updateRestaurant(Long restaurantId, String name, String phone,
                              String address, String cuisineType, String description);
  Restaurant getRestaurantById(Long restaurantId);
  Boolean updateEmail(Long restaurantId, String newEmail, String password);
  Boolean updatePhone(Long restaurantId, String newPhone);
}
