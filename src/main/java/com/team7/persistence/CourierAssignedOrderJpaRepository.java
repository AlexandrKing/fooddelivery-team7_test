package com.team7.persistence;

import com.team7.persistence.entity.CourierAssignedOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourierAssignedOrderJpaRepository extends JpaRepository<CourierAssignedOrderEntity, Long> {
  List<CourierAssignedOrderEntity> findByCourierIdOrderByAssignedAtDesc(Long courierId);

  Optional<CourierAssignedOrderEntity> findByCourierIdAndOrderId(Long courierId, Long orderId);

  Optional<CourierAssignedOrderEntity> findByOrderId(Long orderId);

  List<CourierAssignedOrderEntity> findByOrderIdIn(Collection<Long> orderIds);

  boolean existsByOrderId(Long orderId);
}

