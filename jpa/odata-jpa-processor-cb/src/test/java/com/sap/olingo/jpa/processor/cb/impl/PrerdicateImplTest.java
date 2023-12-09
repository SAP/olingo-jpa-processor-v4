package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.PredicateImpl.NotPredicate;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;

class PredicateImplTest extends BuilderBaseTest {
  private PredicateImpl cut;
  private StringBuilder statement;
  private CriteriaQuery<Tuple> q;

  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<PredicateImpl> c = PredicateImpl.class;
    return Stream.of(
        arguments(c.getMethod("in", Expression.class)),
        arguments(c.getMethod("in", Collection.class)),
        arguments(c.getMethod("in", Expression[].class)),
        arguments(c.getMethod("in", Object[].class)),
        arguments(c.getMethod("getCompoundSelectionItems")),
        arguments(c.getMethod("getJavaType")),
        arguments(c.getMethod("as", Class.class)));
  }

  @BeforeEach
  void setup() {
    cut = new PredicateImpl.NotPredicate(mock(SqlConvertible.class));

    final CriteriaBuilder cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    statement = new StringBuilder();
    q = cb.createTupleQuery();
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {

    testNotImplemented(m, cut);
  }

  @Test
  void testAliasSet() {
    cut.alias("Test");
    assertEquals("Test", cut.getAlias());
  }

  @Test
  void testAliasReset() {
    cut.alias("Test");
    cut.alias(null);
    assertNull(cut.getAlias());
  }

  @Test
  void testInCreated() {
    @SuppressWarnings("unchecked")
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final Predicate in = new PredicateImpl.In<>(Collections.emptyList(), subQuery);
    assertNotNull(in);
  }

  @Test
  void testInCreatedRequiresSubquery() {

    assertThrows(NullPointerException.class, () -> new PredicateImpl.In<>(Collections.emptyList(), null));
  }

  @Test
  void testInGetExpressionReturnsFirstPath() {

    final Root<?> adminDivision = q.from(AdministrativeDivision.class);
    final List<Path<?>> paths = Arrays.asList(adminDivision.get("codeID"), adminDivision.get("parentCodeID"));
    @SuppressWarnings("unchecked")
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<?> act = new PredicateImpl.In<>(paths, subQuery);

    assertEquals(paths.get(0), act.getExpression());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInValueNotImplemented() {

    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<Integer> act = new PredicateImpl.In<>(Collections.emptyList(), subQuery);

    assertThrows(NotImplementedException.class, () -> act.value(Integer.valueOf(5)));
    assertThrows(NotImplementedException.class, () -> act.value(mock(Expression.class)));
  }

  @Test
  void testInAsSqlGeneratePath() {
    final String exp = ("(E0.\"CodeID\", E0.\"CodePublisher\") IN ()");
    final Root<?> adminDivision = q.from(AdministrativeDivision.class);
    final List<Path<?>> paths = Arrays.asList(adminDivision.get("codeID"), adminDivision.get("codePublisher"));
    @SuppressWarnings("unchecked")
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    when(((SqlConvertible) subQuery).asSQL(statement)).thenAnswer(new Answer<StringBuilder>() {
      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder statement = ((StringBuilder) invocation.getArgument(0));
        statement.append("");
        return statement;
      }
    });
    final In<?> act = new PredicateImpl.In<>(paths, subQuery);

    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInAsSqlGenerateSubQuery() {
    final String exp = ("() IN (Test)");
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    when(((SqlConvertible) subQuery).asSQL(statement)).thenAnswer(new Answer<StringBuilder>() {

      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder statement = ((StringBuilder) invocation.getArgument(0));
        statement.append("Test");
        return statement;
      }
    });
    final In<?> act = new PredicateImpl.In<>(Collections.emptyList(), subQuery);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }
  
  @Test
  void testInCreatedFromExpression() {
    @SuppressWarnings("unchecked")
    final Path<String> path = mock(Path.class);
    final Predicate in = new PredicateImpl.In<>(path);
    assertNotNull(in);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  void testInAddValue() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path);
    assertNotNull(in.value(subQuery));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  void testInThrowsExceptionOnMultipleAddValue() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path);
    in.value(subQuery);
    assertThrows(NotImplementedException.class, () ->  in.value(subQuery));
  }

  @Test
  void testIsCompoundSelectionFalse() {
    assertFalse(cut.isCompoundSelection());
  }

  @Test
  void testNotReturnsNegation() {
    assertTrue(cut.not() instanceof NotPredicate);
  }

  @Test
  void testGetExpressionsReturnsEmptyList() {
    assertTrue(cut.getExpressions().isEmpty());
  }

  @Test
  void testGetOperator() {
    assertNull(cut.getOperator());
  }
}
