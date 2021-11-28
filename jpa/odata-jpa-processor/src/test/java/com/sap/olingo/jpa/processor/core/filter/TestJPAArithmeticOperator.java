package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;

class TestJPAArithmeticOperator {
  private CriteriaBuilder cb;

  private JPAOperationConverter converter;
  private Path<Integer> expression;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {
    converter = mock(JPAOperationConverter.class);
    cb = mock(CriteriaBuilder.class);
    expression = mock(Path.class);
  }

  @Test
  void testMemberLiteralGetLeft_Member() throws ODataApplicationException {
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final JPALiteralOperator right = mock(JPALiteralOperator.class);

    when(right.get()).thenReturn(5);
    when(left.get()).thenAnswer(new Answer<Path<Integer>>() {
      @Override
      public Path<Integer> answer(final InvocationOnMock invocation) throws Throwable {
        return expression;
      }
    });

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(expression, cut.getLeft(cb));
  }

  @Test
  void testLiteralMemberGetLeft_Member() throws ODataApplicationException {
    final JPAMemberOperator right = mock(JPAMemberOperator.class);
    final JPALiteralOperator left = mock(JPALiteralOperator.class);

    when(left.get()).thenReturn(5);
    when(right.get()).thenAnswer(new Answer<Path<Integer>>() {
      @Override
      public Path<Integer> answer(final InvocationOnMock invocation) throws Throwable {
        return expression;
      }
    });

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(expression, cut.getLeft(cb));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetLeftLiteralLiteral_Left() throws ODataApplicationException {
    final JPALiteralOperator right = mock(JPALiteralOperator.class);
    final JPALiteralOperator left = mock(JPALiteralOperator.class);
    final Integer leftValue = Integer.valueOf(5);

    final Expression<Number> result = mock(Expression.class);

    when(left.get()).thenReturn(leftValue);
    when(right.get()).thenReturn(10);

    when(cb.literal(leftValue)).thenAnswer(new Answer<Expression<Number>>() {
      @Override
      public Expression<Number> answer(final InvocationOnMock invocation) throws Throwable {
        invocation.getArguments();
        return result;
      }
    });

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    final Expression<Number> act = cut.getLeft(cb);
    assertEquals(result, act);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetLeftMemberMember_Left() throws ODataApplicationException {
    final JPAMemberOperator right = mock(JPAMemberOperator.class);
    final JPAMemberOperator left = mock(JPAMemberOperator.class);

    final Path<Integer> expressionRight = mock(Path.class);

    when(right.get()).thenAnswer(new Answer<Path<Integer>>() {
      @Override
      public Path<Integer> answer(final InvocationOnMock invocation) throws Throwable {
        return expressionRight;
      }
    });
    when(left.get()).thenAnswer(new Answer<Path<Integer>>() {
      @Override
      public Path<Integer> answer(final InvocationOnMock invocation) throws Throwable {
        return expression;
      }
    });

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(expression, cut.getLeft(cb));

  }

  @Test
  void testMemberLiteralGetRightAsNumber_Right() throws ODataApplicationException {
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final JPALiteralOperator right = mock(JPALiteralOperator.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);

    when(right.get(attribute)).thenReturn(new BigDecimal("5.1"));
    when(left.determineAttribute()).thenReturn(attribute);

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(new BigDecimal("5.1"), cut.getRightAsNumber(cb));
  }

  @Test
  void testLiteralMemberGetRightAsNumber_Left() throws ODataApplicationException {
    final JPAMemberOperator right = mock(JPAMemberOperator.class);
    final JPALiteralOperator left = mock(JPALiteralOperator.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);

    when(left.get(attribute)).thenReturn(new BigDecimal("5.1"));
    when(right.determineAttribute()).thenReturn(attribute);

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(new BigDecimal("5.1"), cut.getRightAsNumber(cb));
  }

  @Test
  void testLiteralLiteralGetRightAsNumber_Right() throws ODataApplicationException {
    final JPALiteralOperator right = mock(JPALiteralOperator.class);
    final JPALiteralOperator left = mock(JPALiteralOperator.class);

    when(left.get()).thenReturn(new BigDecimal("5.1"));
    when(right.get()).thenReturn(new BigDecimal("10.1"));

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);
    assertEquals(new BigDecimal("10.1"), cut.getRightAsNumber(cb));
  }

  @Test
  void testGetMemberMemberGetRightAsNumber_Exception() throws ODataApplicationException {
    final JPAMemberOperator right = mock(JPAMemberOperator.class);
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);

    when(left.determineAttribute()).thenReturn(attribute);
    when(right.determineAttribute()).thenReturn(attribute);

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);

    assertThrows(ODataApplicationException.class, () -> cut.getRightAsNumber(cb));
  }

  @Test
  void testGetBooleanMemberGetRightAsNumber_Exception() throws ODataApplicationException {
    final JPAMemberOperator right = mock(JPAMemberOperator.class);
    final JPABooleanOperatorImp left = mock(JPABooleanOperatorImp.class);

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);

    assertThrows(ODataApplicationException.class, () -> cut.getRightAsNumber(cb));
  }

  @Test
  void testGetMemberBooleanGetRightAsNumber_Exception() throws ODataApplicationException {
    final JPAMemberOperator left = mock(JPAMemberOperator.class);
    final JPABooleanOperatorImp right = mock(JPABooleanOperatorImp.class);

    final JPAArithmeticOperator cut = new JPAArithmeticOperatorImp(converter, BinaryOperatorKind.ADD, left, right);

    assertThrows(ODataApplicationException.class, () -> cut.getRightAsNumber(cb));
  }
}
