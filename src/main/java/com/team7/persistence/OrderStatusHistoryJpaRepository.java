package com.team7.persistence;

import com.team7.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
}
