package com.team7.api.controller.restaurant;

import com.team7.api.dto.restaurant.RestaurantManagementDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.DishEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.restaurant.RestaurantManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantManagementController {
  private final RestaurantManagementService restaurantManagementService;
  private final AppAccountJpaRepository appAccountJpaRepository;

  public RestaurantManagementController(
      RestaurantManagementService restaurantManagementService,
      AppAccountJpaRepository appAccountJpaRepository
  ) {
    this.restaurantManagementService = restaurantManagementService;
    this.appAccountJpaRepository = appAccountJpaRepository;
  }

  @GetMapping("/orders")
  public ApiSuccessResponse<List<RestaurantManagementDtos.RestaurantOrderResponse>> orders(Authentication authentication) {
    Long restaurantId = resolveRestaurantId(authentication);
    return ApiSuccessResponse.of(
        restaurantManagementService.getRestaurantOrders(restaurantId).stream().map(this::toOrderResponse).toList()
    );
  }

  @PatchMapping("/orders/{orderId}/status")
  public ApiSuccessResponse<RestaurantManagementDtos.RestaurantOrderResponse> updateOrderStatus(
      @PathVariable Long orderId,
      @RequestBody RestaurantManagementDtos.UpdateOrderStatusRequest request,
      Authentication authentication
  ) {
    if (request == null || request.status() == null || request.status().isBlank()) {
      throw new IllegalArgumentException("Status is required");
    }
    Long restaurantId = resolveRestaurantId(authentication);
    OrderEntity order = restaurantManagementService.updateRestaurantOrderStatus(restaurantId, orderId, request.status());
    return ApiSuccessResponse.of(toOrderResponse(order));
  }

  @GetMapping("/menu")
  public ApiSuccessResponse<List<RestaurantManagementDtos.RestaurantDishResponse>> getMenu(Authentication authentication) {
    Long restaurantId = resolveRestaurantId(authentication);
    return ApiSuccessResponse.of(
        restaurantManagementService.getMenu(restaurantId).stream().map(this::toDishResponse).toList()
    );
  }

  @PostMapping("/menu")
  public ApiSuccessResponse<RestaurantManagementDtos.RestaurantDishResponse> createDish(
      @RequestBody RestaurantManagementDtos.UpsertDishRequest request,
      Authentication authentication
  ) {
    Long restaurantId = resolveRestaurantId(authentication);
    DishEntity created = restaurantManagementService.createDish(restaurantId, toDishEntity(request));
    return ApiSuccessResponse.of(toDishResponse(created));
  }

  @PutMapping("/menu/{dishId}")
  public ApiSuccessResponse<RestaurantManagementDtos.RestaurantDishResponse> updateDish(
      @PathVariable Long dishId,
      @RequestBody RestaurantManagementDtos.UpsertDishRequest request,
      Authentication authentication
  ) {
    Long restaurantId = resolveRestaurantId(authentication);
    DishEntity updated = restaurantManagementService.updateDish(restaurantId, dishId, toDishEntity(request));
    return ApiSuccessResponse.of(toDishResponse(updated));
  }

  @DeleteMapping("/menu/{dishId}")
  public ApiSuccessResponse<String> deleteDish(@PathVariable Long dishId, Authentication authentication) {
    Long restaurantId = resolveRestaurantId(authentication);
    restaurantManagementService.deleteDish(restaurantId, dishId);
    return ApiSuccessResponse.of("ok");
  }

  private Long resolveRestaurantId(Authentication authentication) {
    String email = authentication.getName();
    AppAccountEntity account = appAccountJpaRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    if (account.getLinkedRestaurantId() == null) {
      throw new IllegalArgumentException("Restaurant profile is not linked");
    }
    return account.getLinkedRestaurantId();
  }

  private RestaurantManagementDtos.RestaurantOrderResponse toOrderResponse(OrderEntity e) {
    return new RestaurantManagementDtos.RestaurantOrderResponse(
        e.getId(),
        e.getUserId(),
        e.getRestaurantId(),
        e.getStatus(),
        e.getTotalAmount(),
        e.getCreatedAt()
    );
  }

  private DishEntity toDishEntity(RestaurantManagementDtos.UpsertDishRequest request) {
    DishEntity e = new DishEntity();
    if (request == null) {
      return e;
    }
    e.setName(request.name());
    e.setDescription(request.description());
    e.setPrice(request.price());
    e.setIsAvailable(request.available());
    e.setCategory(request.category());
    e.setCalories(request.calories());
    e.setImageUrl(request.imageUrl());
    e.setPreparationTimeMin(request.preparationTimeMin());
    return e;
  }

  private RestaurantManagementDtos.RestaurantDishResponse toDishResponse(DishEntity e) {
    return new RestaurantManagementDtos.RestaurantDishResponse(
        e.getId(),
        e.getRestaurantId(),
        e.getName(),
        e.getDescription(),
        e.getPrice(),
        e.getIsAvailable(),
        e.getCategory(),
        e.getCalories(),
        e.getImageUrl(),
        e.getPreparationTimeMin()
    );
  }
}

