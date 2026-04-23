package com.team7.persistence;

import com.team7.model.client.Menu;
import com.team7.persistence.entity.DishEntity;

/**
 * Maps {@link DishEntity} to the existing client {@link Menu} DTO (no JPA on API models).
 */
public final class DishEntityMappings {

  private DishEntityMappings() {
  }

  public static Menu toMenu(DishEntity e) {
    Menu item = new Menu();
    item.setId(e.getId());
    item.setRestaurantId(e.getRestaurantId());
    item.setName(e.getName());
    item.setDescription(e.getDescription());
    item.setPrice(e.getPrice() != null ? e.getPrice() : 0.0);
    item.setAvailable(e.getIsAvailable() != null ? e.getIsAvailable() : Boolean.TRUE);
    item.setCategory(e.getCategory());
    item.setCalories(e.getCalories());
    item.setImageUrl(e.getImageUrl());
    item.setCookingTime(e.getPreparationTimeMin());
    return item;
  }
}
