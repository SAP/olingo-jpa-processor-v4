package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;

class JPAODataDatabaseProcessorFactoryTest {

  private JPAODataDatabaseProcessorFactory cut;
  private DataSource ds;
  private Connection connection;
  private DatabaseMetaData dbMetadata;

  static Stream<Arguments> processorProvider() {
    return Stream.of(
        arguments("H2", JPA_HSQLDB_DatabaseProcessor.class),
        arguments("HSQL Database Engine", JPA_HSQLDB_DatabaseProcessor.class),
        arguments("PostgreSQL", JPA_POSTSQL_DatabaseProcessor.class),
        arguments("HANA", JPADefaultDatabaseProcessor.class));
  }

  @BeforeEach
  void setup() throws SQLException {
    cut = new JPAODataDatabaseProcessorFactory();
    ds = mock(DataSource.class);
    connection = mock(Connection.class);
    dbMetadata = mock(DatabaseMetaData.class);
    when(ds.getConnection()).thenReturn(connection);
    when(connection.getMetaData()).thenReturn(dbMetadata);
  }

  @Test
  void testReturnsDefaultOnNull() throws SQLException {
    final JPAODataDatabaseProcessor act = cut.create(null);
    assertTrue(act instanceof JPADefaultDatabaseProcessor);
  }

  @ParameterizedTest
  @MethodSource("processorProvider")
  void test(final String dbName, final Class<?> processor) throws SQLException {
    when(dbMetadata.getDatabaseProductName()).thenReturn(dbName);
    final JPAODataDatabaseProcessor act = cut.create(ds);
    assertEquals(processor, act.getClass());
  }
}
