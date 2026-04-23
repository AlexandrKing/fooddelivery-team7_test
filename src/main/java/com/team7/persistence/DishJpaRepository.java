package com.team7.persistence;

import com.team7.persistence.entity.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {

  List<DishEntity> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

  Optional<DishEntity> findByRestaurantIdAndIdAndIsAvailableTrue(Long restaurantId, Long id);
}
