package com.team7.persistence;

import com.team7.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

  /**
   * Соответствует прежнему JDBC: первая корзина пользователя по id.
   */
  Optional<CartEntity> findFirstByUserIdOrderByIdAsc(Long userId);
}
