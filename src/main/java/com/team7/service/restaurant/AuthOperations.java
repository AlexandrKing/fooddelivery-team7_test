package com.team7.service.restaurant;

import com.team7.model.restaurant.Restaurant;

public interface AuthOperations {
  Restaurant registerRestaurant(String name, String email, String password, String phone,
                                String address, String cuisineType);
  Restaurant login(String email, String password);
  Boolean changePassword(Long restaurantId, String currentPassword, String newPassword);
  void resetPassword(String email);
  void logout();
}
