package com.sap.olingo.jpa.processor.cb.joiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Predicate.BooleanOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpressionJoinerTest {
  private CriteriaBuilder cb;
  private ExpressionJoiner cut;
  private Expression<Boolean> first;
  private Predicate and;
  private Predicate or;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    cb = mock(CriteriaBuilder.class);
    and = mock(Predicate.class);
    or = mock(Predicate.class);
    when(cb.and(any(), any())).thenReturn(and);
    when(cb.or(any(), any())).thenReturn(or);
    first = mock(Expression.class);
    cut = new ExpressionJoiner(cb, BooleanOperator.AND);
  }

  @Test
  void testMergeReturnsThis() {
    assertEquals(cut, cut.merge());
  }

  @Test
  void testAddFirst() {
    assertEquals(cut, cut.add(first));
    assertEquals(first, cut.finish());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddTwoWithAnd() {
    final Expression<Boolean> second = mock(Expression.class);
    cut.add(first);
    assertEquals(cut, cut.add(second));
    assertEquals(and, cut.finish());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddTwoWithOr() {
    final Expression<Boolean> second = mock(Expression.class);
    cut = new ExpressionJoiner(cb, BooleanOperator.OR);
    cut.add(first);
    assertEquals(cut, cut.add(second));
    assertEquals(or, cut.finish());
  }
}
