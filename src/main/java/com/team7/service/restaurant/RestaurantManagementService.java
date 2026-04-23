package com.team7.service.restaurant;

import com.team7.persistence.DishJpaRepository;
import com.team7.persistence.OrderJpaRepository;
import com.team7.persistence.entity.DishEntity;
import com.team7.persistence.entity.OrderEntity;
import com.team7.service.order.OrderStatusTransitionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestaurantManagementService {
  private final OrderJpaRepository orderJpaRepository;
  private final DishJpaRepository dishJpaRepository;

  public RestaurantManagementService(OrderJpaRepository orderJpaRepository, DishJpaRepository dishJpaRepository) {
    this.orderJpaRepository = orderJpaRepository;
    this.dishJpaRepository = dishJpaRepository;
  }

  public List<OrderEntity> getRestaurantOrders(Long restaurantId) {
    return orderJpaRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
  }

  @Transactional
  public OrderEntity updateRestaurantOrderStatus(Long restaurantId, Long orderId, String status) {
    OrderEntity order = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    if (!restaurantId.equals(order.getRestaurantId())) {
      throw new IllegalArgumentException("Order does not belong to restaurant");
    }
    String nextStatus = OrderStatusTransitionPolicy.validateRestaurantTransition(order.getStatus(), status);
    order.setStatus(nextStatus);
    return orderJpaRepository.save(order);
  }

  public List<DishEntity> getMenu(Long restaurantId) {
    return dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
  }

  @Transactional
  public DishEntity createDish(Long restaurantId, DishEntity dish) {
    dish.setId(null);
    dish.setRestaurantId(restaurantId);
    dish.setCreatedAt(LocalDateTime.now());
    dish.setUpdatedAt(LocalDateTime.now());
    if (dish.getIsAvailable() == null) {
      dish.setIsAvailable(Boolean.TRUE);
    }
    return dishJpaRepository.save(dish);
  }

  @Transactional
  public DishEntity updateDish(Long restaurantId, Long dishId, DishEntity patch) {
    DishEntity dish = dishJpaRepository.findById(dishId)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found"));
    if (!restaurantId.equals(dish.getRestaurantId())) {
      throw new IllegalArgumentException("Dish does not belong to restaurant");
    }
    if (patch.getName() != null) dish.setName(patch.getName());
    if (patch.getDescription() != null) dish.setDescription(patch.getDescription());
    if (patch.getPrice() != null) dish.setPrice(patch.getPrice());
    if (patch.getIsAvailable() != null) dish.setIsAvailable(patch.getIsAvailable());
    if (patch.getCategory() != null) dish.setCategory(patch.getCategory());
    if (patch.getCalories() != null) dish.setCalories(patch.getCalories());
    if (patch.getImageUrl() != null) dish.setImageUrl(patch.getImageUrl());
    if (patch.getPreparationTimeMin() != null) dish.setPreparationTimeMin(patch.getPreparationTimeMin());
    dish.setUpdatedAt(LocalDateTime.now());
    return dishJpaRepository.save(dish);
  }

  @Transactional
  public void deleteDish(Long restaurantId, Long dishId) {
    DishEntity dish = dishJpaRepository.findById(dishId)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found"));
    if (!restaurantId.equals(dish.getRestaurantId())) {
      throw new IllegalArgumentException("Dish does not belong to restaurant");
    }
    dishJpaRepository.delete(dish);
  }
}

