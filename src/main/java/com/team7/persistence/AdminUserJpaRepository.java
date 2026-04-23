package com.team7.persistence;

import com.team7.persistence.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserJpaRepository extends JpaRepository<AdminUserEntity, Long> {
}

