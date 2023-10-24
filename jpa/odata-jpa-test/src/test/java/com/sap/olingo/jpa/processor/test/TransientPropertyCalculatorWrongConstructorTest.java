package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.errormodel.TransientPropertyCalculatorWrongConstructor;

class TransientPropertyCalculatorWrongConstructorTest {

  private TransientPropertyCalculatorWrongConstructor cut;
  private EntityManager em;

  @Test
  void testConstructorWithParameter() {
    em = mock(EntityManager.class);
    cut = new TransientPropertyCalculatorWrongConstructor(em, "Test");
    assertNotNull(cut);
    assertEquals(em, cut.getEntityManager());
    assertEquals("Test", cut.getDummy());
  }
}