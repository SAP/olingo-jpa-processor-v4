package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

class TestJPAOperationConverter {
  private CriteriaBuilder cb;

  private Expression<Number> expressionLeft;
  private Expression<Number> expressionRight;
  private JPAOperationConverter cut;
  private JPAODataDatabaseOperations extension;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {
    cb = mock(CriteriaBuilder.class);
    extension = mock(JPAODataDatabaseOperations.class);
    cut = new JPAOperationConverter(cb, extension);
    expressionLeft = mock(Path.class);
    expressionRight = mock(Path.class);
  }

  @Test
  void testAddMemberMember() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPAMemberOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.ADD);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsExpression()).thenReturn(expressionRight);
    when(cb.sum(expressionLeft, expressionRight)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testAddMemberLiteral() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPALiteralOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.ADD);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsNumber(cb)).thenReturn(5);
    when(cb.sum(expressionLeft, 5)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testSubMemberMember() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPAMemberOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.SUB);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsExpression()).thenReturn(expressionRight);
    when(cb.diff(expressionLeft, expressionRight)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testSubMemberLiteral() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPALiteralOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.SUB);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsNumber(cb)).thenReturn(5);
    when(cb.diff(expressionLeft, 5)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testDivMemberMember() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPAMemberOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.DIV);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsExpression()).thenReturn(expressionRight);
    when(cb.quot(expressionLeft, expressionRight)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testDivMemberLiteral() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPALiteralOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.DIV);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsNumber(cb)).thenReturn(5);
    when(cb.quot(expressionLeft, 5)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testMulMemberMember() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPAMemberOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.MUL);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsExpression()).thenReturn(expressionRight);
    when(cb.prod(expressionLeft, expressionRight)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testMulMemberLiteral() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    @SuppressWarnings("unchecked")
    final Expression<Number> result = mock(Path.class);
    when(operator.getRight()).thenReturn(mock(JPALiteralOperator.class));
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.MUL);
    when(operator.getLeft(cb)).thenReturn(expressionLeft);
    when(operator.getRightAsNumber(cb)).thenReturn(5);
    when(cb.prod(expressionLeft, 5)).thenReturn(result);

    final Expression<?> act = cut.convert(operator);
    assertEquals(result, act);
  }

  @Test
  void testUnknownOperation_CallExtension() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperatorImp.class);
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.AND);
    when(extension.convert(operator)).thenThrow(new ODataApplicationException(null, HttpStatusCode.NOT_IMPLEMENTED
        .getStatusCode(), null));

    final ODataApplicationException act = assertThrows(ODataApplicationException.class, () -> cut.convert(operator));

    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }
}

//case MOD: