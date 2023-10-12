package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.filter.JPAAggregationOperation;
import com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator;
import com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAEnumerationBasedOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAMethodCall;
import com.sap.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator;

class JPADefaultDatabaseProcessorTest extends JPA_XXX_DatabaseProcessorTest {

  @BeforeEach
  void setup() {
    initEach();
    oneParameterResult = "SELECT * FROM Example(?1)";
    twoParameterResult = "SELECT * FROM Example(?1,?2)";
    countResult = "SELECT COUNT(*) FROM Example(?1)";
    cut = new JPADefaultDatabaseProcessor();
  }

  @Test
  void testNotSupportedConvertBooleanOperator() throws ODataApplicationException {
    final JPABooleanOperator operator = mock(JPABooleanOperator.class);
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @Test
  void testNotSupportedConvertAggregationOperator() throws ODataApplicationException {
    final JPAAggregationOperation operator = mock(JPAAggregationOperation.class);
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @Test
  void testNotSupportedConvertArithmeticOperator() throws ODataApplicationException {
    final JPAArithmeticOperator operator = mock(JPAArithmeticOperator.class);
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @Test
  void testNotSupportedConvertMethodCall() throws ODataApplicationException {
    final JPAMethodCall operator = mock(JPAMethodCall.class);
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @Test
  void testNotSupportedConvertUnaryBooleanOperator() throws ODataApplicationException {
    final JPAUnaryBooleanOperator operator = mock(JPAUnaryBooleanOperator.class);
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @Test
  void testNotSupportedConvertComparisonOperatorOthersThenHAS() throws ODataApplicationException {
    @SuppressWarnings("unchecked")
    final JPAComparisonOperator<String> operator = mock(JPAComparisonOperator.class);
    when(operator.getOperator()).then(new Answer<BinaryOperatorKind>() {
      @Override
      public BinaryOperatorKind answer(final InvocationOnMock invocation) throws Throwable {
        return BinaryOperatorKind.SUB;
      }
    });
    assertThrows(ODataJPAFilterException.class, () -> ((JPAODataDatabaseOperations) cut).convert(operator));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSupportedConvertComparisonOperatorOperatorHAS() throws ODataApplicationException {
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    final Expression<Integer> cbResult = mock(Expression.class);
    final Predicate cbPredicate = mock(Predicate.class);
    final JPAComparisonOperator<Long> operator = mock(JPAComparisonOperator.class);
    final Expression<Long> left = mock(Expression.class);
    final JPAEnumerationBasedOperator right = mock(JPAEnumerationBasedOperator.class);

    when(operator.getOperator()).then(new Answer<BinaryOperatorKind>() {
      @Override
      public BinaryOperatorKind answer(final InvocationOnMock invocation) throws Throwable {
        return BinaryOperatorKind.HAS;
      }
    });
    when(operator.getRight()).thenReturn(right);
    when(right.getValue()).thenReturn(5L);
    when(operator.getLeft()).thenReturn(left);

    when(cb.quot(left, 5L)).thenAnswer(new Answer<Expression<Integer>>() {
      @Override
      public Expression<Integer> answer(final InvocationOnMock invocation) throws Throwable {
        return cbResult;
      }
    });
    when(cb.mod(cbResult, 2)).thenReturn(cbResult);
    when(cb.equal(cbResult, 1)).thenReturn(cbPredicate);
    ((JPAODataDatabaseOperations) cut).setCriterialBuilder(cb);
    final Expression<Boolean> act = ((JPAODataDatabaseOperations) cut).convert(operator);
    assertNotNull(act);
  }

  @Test
  void testNotSupportedSearch() throws ODataApplicationException {
    assertThrows(ODataJPADBAdaptorException.class, () -> {
      cut.createSearchWhereClause(null, null, null, null, null);
    });
  }
}
