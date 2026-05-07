package com.team7.repository.client;

import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class UserSecurityRepositoryTest {

  private AppAccountJpaRepository appAccountJpaRepository;
  private UserSecurityRepository repo;

  @BeforeEach
  void setUp() {
    appAccountJpaRepository = Mockito.mock(AppAccountJpaRepository.class);
    repo = new UserSecurityRepository(appAccountJpaRepository);
  }

  @Test
  void returnsNullWhenAccountMissing() {
    given(appAccountJpaRepository.findByEmail("x@test")).willReturn(Optional.empty());
    assertNull(repo.findByEmail("x@test"));
  }

  @Test
  void mapsAccountToSecurityRecordWithDefaults() {
    AppAccountEntity e = new AppAccountEntity();
    e.setId(1L);
    e.setEmail("u@test");
    e.setPasswordHash("hash");
    e.setRole(AppRole.USER);
    e.setLinkedUserId(10L);
    e.setLinkedRestaurantId(null);
    e.setLinkedCourierId(7L);
    e.setLinkedAdminId(null);
    e.setIsActive(null); // should default to true
    e.setCreatedAt(LocalDateTime.now());
    e.setUpdatedAt(LocalDateTime.now());
    given(appAccountJpaRepository.findByEmail("u@test")).willReturn(Optional.of(e));

    UserSecurityRepository.SecurityUserRecord rec = repo.findByEmail("u@test");
    assertNotNull(rec);
    assertEquals(1L, rec.id());
    assertEquals("u@test", rec.email());
    assertEquals("hash", rec.passwordHash());
    assertEquals("USER", rec.role());
    assertEquals(10L, rec.linkedUserId());
    assertEquals(7L, rec.linkedCourierId());
    assertTrue(rec.active());
  }
}

