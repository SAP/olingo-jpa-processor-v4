package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class OrderImplTest {

  private SqlConvertible expression;
  private OrderImpl cut;
  private StringBuilder stmt;

  @BeforeEach
  void setup() {
    expression = mock(SqlConvertible.class, withSettings().extraInterfaces(Expression.class));
    cut = new OrderImpl(true, expression);
  }

  @Test
  void testGetExpression() {
    assertEquals(expression, cut.getExpression());
  }

  @Test
  void testIsAscending() {
    assertTrue(cut.isAscending());
  }

  @Test
  void testRevers() {
    final Order act = cut.reverse();
    assertFalse(act.isAscending());
    assertEquals(expression, act.getExpression());
  }

  @Test
  void testAsSqlSimplePrimitive() {
    stmt = new StringBuilder();
    when(expression.asSQL(stmt)).thenAnswer(new Answer<StringBuilder>() {
      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder sb = invocation.getArgument(0);
        return sb.append("Test");
      }
    });
    final String exp = "Test ASC";
    assertEquals(exp, cut.asSQL(stmt).toString());
  }
}
