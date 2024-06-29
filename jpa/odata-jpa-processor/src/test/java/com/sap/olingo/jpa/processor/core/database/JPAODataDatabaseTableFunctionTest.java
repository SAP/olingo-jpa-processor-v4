package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAODataDatabaseTableFunctionTest {
  private JPAODataDatabaseTableFunction cut;

  @BeforeEach
  void setup() {
    cut = new testClass();
  }

  @SuppressWarnings("removal")
  @Test
  void testExecuteFunctionQueryOldReturnsEmptyList() throws ODataApplicationException {
    final var act = cut.executeFunctionQuery(null, null, null);
    assertNotNull(act);
    assertTrue(act.isEmpty());
  }

  @SuppressWarnings("rawtypes")
  @Test
  void testExecuteFunctionQueryNewReturnsEmptyList() throws ODataApplicationException {
    final var act = cut.executeFunctionQuery(null, null, null, null, null);
    assertNotNull(act);
    assertTrue(act instanceof List);
    assertTrue(((List) act).isEmpty());
  }

  private static class testClass implements JPAODataDatabaseTableFunction {

  }
}
