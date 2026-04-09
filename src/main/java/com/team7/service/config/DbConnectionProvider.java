package com.team7.service.config;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection provider that uses Spring-managed {@link DataSource}.
 * <p>
 * Kept for compatibility while legacy static DB access is being phased out.
 */
@Component
public class DbConnectionProvider {
  private final DataSource dataSource;

  public DbConnectionProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}

