package com.team7.security;

import com.team7.repository.client.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class CustomUserDetailsServiceTest {

  private UserSecurityRepository userSecurityRepository;
  private CustomUserDetailsService service;

  @BeforeEach
  void setUp() {
    userSecurityRepository = Mockito.mock(UserSecurityRepository.class);
    service = new CustomUserDetailsService(userSecurityRepository);
  }

  @Test
  void throwsWhenUserNotFound() {
    given(userSecurityRepository.findByEmail("a@test")).willReturn(null);
    UsernameNotFoundException ex = assertThrows(
        UsernameNotFoundException.class,
        () -> service.loadUserByUsername("a@test")
    );
    assertEquals("User not found", ex.getMessage());
  }

  @Test
  void throwsWhenUserInactive() {
    given(userSecurityRepository.findByEmail("a@test")).willReturn(
        new UserSecurityRepository.SecurityUserRecord(
            1L, "a@test", "hash", "USER", 1L, null, null, null, false
        )
    );
    UsernameNotFoundException ex = assertThrows(
        UsernameNotFoundException.class,
        () -> service.loadUserByUsername("a@test")
    );
    assertEquals("User is inactive", ex.getMessage());
  }

  @Test
  void returnsUserDetailsWithRolePrefix() {
    given(userSecurityRepository.findByEmail("a@test")).willReturn(
        new UserSecurityRepository.SecurityUserRecord(
            1L, "a@test", "hash", "ADMIN", null, null, null, null, true
        )
    );
    var details = service.loadUserByUsername("a@test");
    assertEquals("a@test", details.getUsername());
    assertEquals("hash", details.getPassword());
    assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
  }
}

