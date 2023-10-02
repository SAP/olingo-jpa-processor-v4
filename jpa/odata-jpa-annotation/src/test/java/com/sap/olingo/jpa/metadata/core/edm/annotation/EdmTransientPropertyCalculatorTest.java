/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.persistence.Tuple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since 1.0.9
 * 18.01.2022
 */
class EdmTransientPropertyCalculatorTest {

  private EdmTransientPropertyCalculator<String> cut;
  private Tuple tuple;

  @BeforeEach
  void setup() {
    cut = new calculatorImpl();
    tuple = mock(Tuple.class);
  }

  @Test
  void testDefaultCalculatePropertyReturnsNull() {
    assertNull(cut.calculateProperty(tuple));
  }

  @Test
  void testDefaultCalculateCollectionPropertyReturnsEmptyList() {
    assertNotNull(cut.calculateCollectionProperty(tuple));
    assertTrue(cut.calculateCollectionProperty(tuple).isEmpty());
  }

  static class calculatorImpl implements EdmTransientPropertyCalculator<String> {}
}
