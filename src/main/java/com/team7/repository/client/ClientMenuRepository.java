package com.team7.repository.client;

import com.team7.model.client.Menu;
import com.team7.persistence.DishEntityMappings;
import com.team7.persistence.DishJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ClientMenuRepository {
  private final DishJpaRepository dishJpaRepository;
  public ClientMenuRepository(DishJpaRepository dishJpaRepository) {
    this.dishJpaRepository = dishJpaRepository;
  }

  public List<Menu> getMenu(Long restaurantId) {
    return dishJpaRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
        .map(DishEntityMappings::toMenu)
        .collect(Collectors.toList());
  }

  public Menu getMenuItem(Long restaurantId, Long itemId) {
    return dishJpaRepository.findByRestaurantIdAndIdAndIsAvailableTrue(restaurantId, itemId)
        .map(DishEntityMappings::toMenu)
        .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено в меню ресторана"));
  }
}
