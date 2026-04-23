package com.team7.persistence;

import com.team7.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

  /** Same ordering as legacy JDBC: no ORDER BY in SQL (database default). */
  List<ReviewEntity> findByUserId(Long userId);

  List<ReviewEntity> findByUserIdAndCourierRatingIsNotNullOrderByCreatedAtDesc(Long userId);

  List<ReviewEntity> findByCourierIdIsNotNullOrderByCreatedAtDesc();

  boolean existsByOrderId(Long orderId);

  Optional<ReviewEntity> findByOrderId(Long orderId);

  @Query(
      "SELECT AVG(r.restaurantRating) FROM ReviewEntity r "
          + "WHERE r.restaurantId = :restaurantId AND r.restaurantRating IS NOT NULL"
  )
  Double averageRestaurantRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

  @Query(
      "SELECT AVG(r.courierRating) FROM ReviewEntity r "
          + "WHERE r.courierId = :courierId AND r.courierRating IS NOT NULL"
  )
  Double averageCourierRatingByCourierId(@Param("courierId") Long courierId);
}
