package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestJPAQueryPair {
  private JPAQueryPair cut;
  private JPAAbstractQuery outer;
  private JPAAbstractQuery inner;

  @BeforeEach
  void setup() {
    outer = mock(JPAAbstractQuery.class);
    inner = mock(JPAAbstractQuery.class);
    cut = new JPAQueryPair(inner, outer);
  }

  @Test
  void testGetOuter() {
    assertEquals(outer, cut.outer());
  }

  @Test
  void testGetInner() {
    assertEquals(inner, cut.inner());
  }

  @Test
  void testToString() {
    assertNotNull(cut.toString());
  }
}
