package com.team7.service.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbConnectionProviderTest {

  @Test
  void getConnectionReturnsConnection() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection conn = mock(Connection.class);
    when(ds.getConnection()).thenReturn(conn);

    DbConnectionProvider provider = new DbConnectionProvider(ds);
    assertSame(conn, provider.getConnection());
  }

  @Test
  void getConnectionPropagatesSQLException() throws Exception {
    DataSource ds = mock(DataSource.class);
    when(ds.getConnection()).thenThrow(new SQLException("nope"));

    DbConnectionProvider provider = new DbConnectionProvider(ds);
    SQLException ex = assertThrows(SQLException.class, provider::getConnection);
    assertEquals("nope", ex.getMessage());
  }
}

