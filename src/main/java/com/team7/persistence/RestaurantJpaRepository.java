package com.team7.persistence;

import com.team7.persistence.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {

  List<RestaurantEntity> findByIsActiveTrue();

  Optional<RestaurantEntity> findByIdAndIsActiveTrue(Long id);

  @Query(
      "SELECT r FROM RestaurantEntity r WHERE r.isActive = true "
          + "AND (:minRating IS NULL OR r.rating >= :minRating) "
          + "AND (:maxDeliveryTime IS NULL OR r.deliveryTime <= :maxDeliveryTime)"
  )
  List<RestaurantEntity> findActiveFiltered(
      @Param("minRating") Double minRating,
      @Param("maxDeliveryTime") Integer maxDeliveryTime
  );
}
