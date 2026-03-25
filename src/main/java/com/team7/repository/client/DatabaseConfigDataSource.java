package com.team7.repository.client;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Fallback DataSource for repository no-arg usage (e.g. legacy userstory code).
 * <p>
 * In Spring Boot runtime you should prefer the Spring-managed DataSource
 * (via constructor injection of JdbcTemplate).
 */
public final class DatabaseConfigDataSource {
  private DatabaseConfigDataSource() {
  }

  public static DriverManagerDataSource createFallbackDataSource() {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl("jdbc:postgresql://localhost:5432/restaurant_db");
    ds.setUsername("restaurant_user");
    ds.setPassword("restaurant_password");
    return ds;
  }
}

