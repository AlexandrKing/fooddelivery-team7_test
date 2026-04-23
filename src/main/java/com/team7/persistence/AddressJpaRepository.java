package com.team7.persistence;

import com.team7.persistence.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressJpaRepository extends JpaRepository<AddressEntity, Long> {

  List<AddressEntity> findByUserIdOrderByIdAsc(Long userId);
}
