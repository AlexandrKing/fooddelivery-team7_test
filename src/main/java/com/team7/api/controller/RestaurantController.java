package com.team7.api.controller;

import com.team7.api.dto.restaurant.RestaurantDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.Menu;
import com.team7.model.client.Restaurant;
import com.team7.service.client.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @GetMapping
  public ApiSuccessResponse<List<RestaurantDtos.RestaurantResponse>> getRestaurants(
      @RequestParam(required = false) Double rating,
      @RequestParam(required = false) Integer deliveryTime
  ) {
    List<Restaurant> restaurants = (rating != null || deliveryTime != null)
        ? restaurantService.filterRestaurants(rating, deliveryTime)
        : restaurantService.getRestaurants();
    return ApiSuccessResponse.of(restaurants.stream().map(this::toRestaurantResponse).toList());
  }

  @GetMapping("/{id}")
  public ApiSuccessResponse<RestaurantDtos.RestaurantResponse> getRestaurantById(@PathVariable Long id) {
    return ApiSuccessResponse.of(toRestaurantResponse(restaurantService.getRestaurantById(id)));
  }

  @GetMapping("/{id}/menu")
  public ApiSuccessResponse<List<RestaurantDtos.MenuItemResponse>> getMenu(@PathVariable Long id) {
    return ApiSuccessResponse.of(restaurantService.getMenu(id).stream().map(this::toMenuItemResponse).toList());
  }

  private RestaurantDtos.RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
    RestaurantDtos.RestaurantResponse dto = new RestaurantDtos.RestaurantResponse();
    dto.setId(restaurant.getId());
    dto.setName(restaurant.getName());
    dto.setAddress(restaurant.getAddress());
    dto.setLatitude(restaurant.getLatitude());
    dto.setLongitude(restaurant.getLongitude());
    dto.setCuisineType(restaurant.getCuisineType());
    dto.setRating(restaurant.getRating());
    dto.setDeliveryTime(restaurant.getDeliveryTime());
    dto.setMinOrderAmount(restaurant.getMinOrderAmount());
    dto.setIsActive(restaurant.getIsActive());
    return dto;
  }

  private RestaurantDtos.MenuItemResponse toMenuItemResponse(Menu menu) {
    return new RestaurantDtos.MenuItemResponse(
        menu.getId(),
        menu.getRestaurantId(),
        menu.getName(),
        menu.getDescription(),
        menu.getPrice(),
        menu.getAvailable(),
        menu.getCategory(),
        menu.getCalories(),
        menu.getWeight(),
        menu.getImageUrl(),
        menu.getCookingTime()
    );
  }
}

