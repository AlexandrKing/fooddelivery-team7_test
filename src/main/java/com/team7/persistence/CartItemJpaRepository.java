package com.team7.persistence;

import com.team7.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, Long> {

  List<CartItemEntity> findByCartIdOrderByIdAsc(Long cartId);

  Optional<CartItemEntity> findByCartIdAndDishId(Long cartId, Long dishId);

  Optional<CartItemEntity> findByIdAndCartId(Long id, Long cartId);

  void deleteByCartId(Long cartId);
}
