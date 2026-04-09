package com.team7.repository.client;

import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserSecurityRepository {
  private final AppAccountJpaRepository appAccountJpaRepository;

  public UserSecurityRepository(AppAccountJpaRepository appAccountJpaRepository) {
    this.appAccountJpaRepository = appAccountJpaRepository;
  }

  /**
   * Загрузка учетных данных для Spring Security.
   */
  public SecurityUserRecord findByEmail(String email) {
    return appAccountJpaRepository.findByEmail(email)
        .map(this::toRecord)
        .orElse(null);
  }

  private SecurityUserRecord toRecord(AppAccountEntity account) {
    return new SecurityUserRecord(
        account.getId(),
        account.getEmail(),
        account.getPasswordHash(),
        account.getRole().name(),
        account.getLinkedUserId(),
        account.getLinkedRestaurantId(),
        account.getLinkedCourierId(),
        account.getLinkedAdminId(),
        account.getIsActive() != null ? account.getIsActive() : Boolean.TRUE
    );
  }

  public record SecurityUserRecord(
      Long id,
      String email,
      String passwordHash,
      String role,
      Long linkedUserId,
      Long linkedRestaurantId,
      Long linkedCourierId,
      Long linkedAdminId,
      boolean active
  ) {
  }
}
