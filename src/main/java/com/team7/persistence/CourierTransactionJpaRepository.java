package com.team7.persistence;

import com.team7.persistence.entity.CourierTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CourierTransactionJpaRepository extends JpaRepository<CourierTransactionEntity, Long> {
  boolean existsByOrderId(Long orderId);

  List<CourierTransactionEntity> findByCourierIdOrderByCreatedAtDesc(Long courierId);

  Page<CourierTransactionEntity> findByCourierIdOrderByCreatedAtDesc(Long courierId, Pageable pageable);

  @Query("""
      SELECT COALESCE(SUM(t.amount), 0)
      FROM CourierTransactionEntity t
      WHERE t.courierId = :courierId
        AND t.createdAt >= :from
      """)
  BigDecimal sumAmountByCourierIdSince(@Param("courierId") Long courierId, @Param("from") LocalDateTime from);

  @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CourierTransactionEntity t")
  BigDecimal sumTotalAmount();
}
