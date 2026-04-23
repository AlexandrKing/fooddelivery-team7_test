package com.team7.persistence;

import com.team7.persistence.entity.CourierUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierUserJpaRepository extends JpaRepository<CourierUserEntity, Long> {
}

