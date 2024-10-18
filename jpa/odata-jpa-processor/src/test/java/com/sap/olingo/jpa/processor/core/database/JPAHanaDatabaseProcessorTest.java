package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

class JPAHanaDatabaseProcessorTest extends JPA_XXX_DatabaseProcessorTest {

  @BeforeEach
  void setup() {
    initEach();
    oneParameterResult = "SELECT * FROM Example(?1)";
    twoParameterResult = "SELECT * FROM Example(?1,?2)";
    countResult = "SELECT COUNT(*) FROM Example(?1)";
    cut = new JPAHanaDatabaseProcessor();
  }

  @Test
  void testGetLocatePattern() {
    assertTrue(cut instanceof ProcessorSqlPatternProvider);
    assertEquals("INSTR", ((ProcessorSqlPatternProvider) cut).getLocatePattern().function());
  }
}
