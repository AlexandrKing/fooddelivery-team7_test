package com.team7.persistence;

import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppAccountJpaRepository extends JpaRepository<AppAccountEntity, Long> {
  Optional<AppAccountEntity> findByEmail(String email);

  List<AppAccountEntity> findByRoleOrderByIdAsc(AppRole role);
}

