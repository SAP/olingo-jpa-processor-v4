package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

class JPAPostgresqlSqlPatternProviderTest {
  private ProcessorSqlPatternProvider cut;

  @BeforeEach
  void setup() {
    cut = new JPAPostgresqlSqlPatternProvider();
  }

  @Test
  void testGetLocatePattern() {
    assertEquals("POSITION", cut.getLocatePattern().function());
    assertEquals(" IN ", cut.getLocatePattern().parameters().get(1).keyword());
  }

  @Test
  void testGetSubStringPattern() {
    assertEquals("SUBSTRING", cut.getSubStringPattern().function());
    assertEquals(2, cut.getLocatePattern().parameters().size());
  }

}
