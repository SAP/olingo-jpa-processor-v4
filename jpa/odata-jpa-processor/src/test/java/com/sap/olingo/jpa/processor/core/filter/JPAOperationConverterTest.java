package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

class JPAOperationConverterTest {
  private CriteriaBuilder cb;

  private Expression<Number> expressionLeft;
  private Expression<Number> expressionRight;
  private JPAOperationConverter cut;
  private JPAODataDatabaseOperations extension;
  private JPAODataQueryDirectives directives;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {

    directives = JPAODataServiceContext.with()
        .useQueryDirectives()
        .build()
        .build()
        .getQueryDirectives();

    cb = mock(CriteriaBuilder.class);
    extension = mock(JPAODataDatabaseOperations.class);
    cut = new JPAOperationConverter(cb, extension, directives);
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

  @SuppressWarnings("unchecked")
  @Test
  void testConvertInOperatorNotInThrowsException() {
    final JPAInOperator<String, JPAOperator> jpaOperator = mock(JPAInOperator.class);
    when(jpaOperator.getOperator()).thenReturn(BinaryOperatorKind.HAS);
    final var act = assertThrows(ODataJPAFilterException.class, () -> cut.convert(jpaOperator));
    assertEquals(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR.getKey(), act.getId());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertInOperatorInNotSupportedThrowsException() {
    final JPAInOperator<String, JPAOperator> jpaOperator = mock(JPAInOperator.class);
    when(jpaOperator.getOperator()).thenReturn(BinaryOperatorKind.IN);
    final var act = assertThrows(ODataJPAFilterException.class, () -> cut.convert(jpaOperator));
    assertEquals(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR.getKey(), act.getId());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertInOperatorTooManyValuesThrowsException() throws ODataException {
    directives = JPAODataServiceContext.with()
        .useQueryDirectives()
        .maxValuesInInClause(10)
        .build()
        .build()
        .getQueryDirectives();
    cut = new JPAOperationConverter(cb, extension, directives);

    final List<JPAOperator> values = buildValuesList(20);
    final In<Object> inOperation = mock(In.class);
    final JPAInOperator<String, JPAOperator> jpaOperator = mock(JPAInOperator.class);
    when(jpaOperator.getOperator()).thenReturn(BinaryOperatorKind.IN);
    when(cb.in(any())).thenReturn(inOperation);
    when(jpaOperator.getFixValues()).thenReturn(values);
    final var act = assertThrows(ODataJPAFilterException.class, () -> cut.convert(jpaOperator));
    assertEquals(ODataJPAFilterException.MessageKeys.NO_VALUES_OUT_OF_LIMIT.getKey(), act.getId());
  }

  @Test
  void testConvertInOperatorWithRelatedEntityFields() throws ODataException {
    directives = JPAODataServiceContext.with()
        .useQueryDirectives()
        .maxValuesInInClause(10)
        .build()
        .build()
        .getQueryDirectives();
    cut = new JPAOperationConverter(cb, extension, directives);

    final List<JPAOperator> values = new ArrayList<>();
    final JPAPathOperator pathOperator = mock(JPAPathOperator.class);
    when(pathOperator.getPathValue()).thenReturn("relatedEntityValue");
    values.add(pathOperator);

    final In<Object> inOperation = mock(In.class);
    final JPAInOperator<String, JPAOperator> jpaOperator = mock(JPAInOperator.class);
    when(jpaOperator.getOperator()).thenReturn(BinaryOperatorKind.IN);
    when(cb.in(any())).thenReturn(inOperation);
    when(jpaOperator.getFixValues()).thenReturn(values);

    cut.convert(jpaOperator);

    // Verify that the related entity value was added to the IN clause
    verify(inOperation).value("relatedEntityValue");
  }

  private List<JPAOperator> buildValuesList(final int i) {
    final List<JPAOperator> result = new ArrayList<>(i);
    for (int count = 0; count < i; count++) {
      result.add(mock(JPAOperator.class));
    }
    return result;
  }

}