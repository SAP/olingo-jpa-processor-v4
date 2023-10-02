package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.errormodel.TransientPropertyCalculatorTwoConstructors;

class TransientPropertyCalculatorTwoConstructorsTest {

  private TransientPropertyCalculatorTwoConstructors cut;
  private EntityManager em;

  @Test
  void testConstructor() {
    assertNotNull(new TransientPropertyCalculatorTwoConstructors());
  }

  @Test
  void testConstructorWithParameter() {
    em = mock(EntityManager.class);
    cut = new TransientPropertyCalculatorTwoConstructors(em);
    assertNotNull(cut);
    assertEquals(em, cut.getEntityManager());
  }
}
