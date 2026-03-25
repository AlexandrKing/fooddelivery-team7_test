package com.team7.repository.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserSecurityRepository {
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public UserSecurityRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // TODO(legacy-cleanup): remove this fallback constructor in Wave 2.
  @Deprecated(forRemoval = false, since = "1.1")
  public UserSecurityRepository() {
    throw new UnsupportedOperationException(
        "Fallback constructor is disabled. Use Spring DI constructor with JdbcTemplate."
    );
  }

  public SecurityUserRecord findByEmail(String email) {
    List<SecurityUserRecord> users = jdbcTemplate.query(
        "SELECT id, email, password FROM users WHERE email = ?",
        (rs, rowNum) -> new SecurityUserRecord(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("password")
        ),
        email
    );
    return users.isEmpty() ? null : users.get(0);
  }

  public record SecurityUserRecord(Long id, String email, String passwordHash) {
  }
}

