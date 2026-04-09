package com.team7.repository.client;

import com.team7.persistence.UserJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserSecurityRepository {
  private final UserJpaRepository userJpaRepository;

  public UserSecurityRepository(UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  /**
   * Загрузка учётных данных для Spring Security (тот же источник, что и {@link com.team7.persistence.entity.UserEntity}).
   */
  public SecurityUserRecord findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
        .map(u -> new SecurityUserRecord(u.getId(), u.getEmail(), u.getPassword()))
        .orElse(null);
  }

  public record SecurityUserRecord(Long id, String email, String passwordHash) {
  }
}
