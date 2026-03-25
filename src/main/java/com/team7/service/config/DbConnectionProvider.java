package com.team7.service.config;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection provider that uses Spring-managed {@link DataSource}.
 * <p>
 * Old code can still use {@link DatabaseConfig}. New/bean-based code can switch
 * to this provider without deleting the legacy class.
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

