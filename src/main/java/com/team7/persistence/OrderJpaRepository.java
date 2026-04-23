package com.team7.persistence;

import com.team7.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<OrderEntity> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

  List<OrderEntity> findAllByOrderByCreatedAtDesc();

  @Query("""
      SELECT o FROM OrderEntity o
      WHERE UPPER(TRIM(o.deliveryType)) = 'DELIVERY'
        AND UPPER(TRIM(o.status)) IN ('PENDING', 'PREPARING', 'READY')
        AND NOT EXISTS (SELECT 1 FROM CourierAssignedOrderEntity c WHERE c.orderId = o.id)
      ORDER BY o.createdAt ASC
      """)
  List<OrderEntity> findAvailableForCourierAssignment();
}
