package com.team7.service.restaurant;

import com.team7.model.restaurant.Restaurant;

public class RestaurantService implements RestaurantOperations {

  @Override
  public Restaurant updateRestaurant(Long restaurantId, String name, String phone,
                                     String address, String cuisineType, String description) {
    Restaurant restaurant = AuthService.getRestaurantById(restaurantId);

    if (restaurant != null) {
      if (name != null) restaurant.setName(name);
      if (phone != null) restaurant.setPhone(phone);
      if (address != null) restaurant.setAddress(address);
      if (cuisineType != null) restaurant.setCuisineType(cuisineType);
      if (description != null) restaurant.setDescription(description);
    }

    return restaurant;
  }

  @Override
  public Restaurant getRestaurantById(Long restaurantId) {
    return AuthService.getRestaurantById(restaurantId);
  }

  @Override
  public Boolean updateEmail(Long restaurantId, String newEmail, String password) {
    Restaurant restaurant = getRestaurantById(restaurantId);
    if (restaurant != null && restaurant.getPassword().equals(password)) {
      restaurant.setEmail(newEmail);
      restaurant.setEmailVerified(false);
      return true;
    }
    return false;
  }

  @Override
  public Boolean updatePhone(Long restaurantId, String newPhone) {
    Restaurant restaurant = getRestaurantById(restaurantId);
    if (restaurant != null) {
      restaurant.setPhone(newPhone);
      return true;
    }
    return false;
  }
}
