package com.team7.persistence;

import com.team7.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

  List<OrderItemEntity> findByOrderIdOrderByIdAsc(Long orderId);
}
