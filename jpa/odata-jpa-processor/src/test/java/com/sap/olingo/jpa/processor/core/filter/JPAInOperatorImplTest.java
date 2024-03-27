package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAInOperatorImplTest {

  private JPAOperationConverter converter;
  private JPAInOperatorImpl<String, JPALiteralOperator> cut;

  @BeforeEach
  void setUp() throws Exception {
    converter = mock(JPAOperationConverter.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCallsConverter() throws ODataApplicationException {
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final List<JPALiteralOperator> right = new ArrayList<>();
    final Expression<Boolean> exp = mock(Expression.class);
    when(converter.convert(any(JPAInOperator.class))).thenReturn(exp);

    cut = new JPAInOperatorImpl<>(converter, left, right);

    assertEquals(exp, cut.get());
    verify(converter).convert(cut);
  }

  @Test
  void testGetName() {
    cut = new JPAInOperatorImpl<>(converter, null, null);
    assertEquals("IN", cut.getName());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetLeft() throws ODataApplicationException {
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final Path<String> exp = mock(Path.class);
    doReturn(exp).when(left).get();
    cut = new JPAInOperatorImpl<>(converter, left, null);
    assertEquals(exp, cut.getLeft());
  }

  @Test
  void testGetFixValues() throws ODataApplicationException {
    final List<JPALiteralOperator> right = new ArrayList<>();
    cut = new JPAInOperatorImpl<>(converter, null, right);
    assertEquals(right, cut.getFixValues());
  }
}
