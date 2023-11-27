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
  private StringBuilder stmt;
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
    stmt = new StringBuilder();
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

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final List<Path<?>> paths = Arrays.asList(adminDiv.get("codeID"), adminDiv.get("parentCodeID"));
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
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final List<Path<?>> paths = Arrays.asList(adminDiv.get("codeID"), adminDiv.get("codePublisher"));
    @SuppressWarnings("unchecked")
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    when(((SqlConvertible) subQuery).asSQL(stmt)).thenAnswer(new Answer<StringBuilder>() {
      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder stmt = ((StringBuilder) invocation.getArgument(0));
        stmt.append("");
        return stmt;
      }
    });
    final In<?> act = new PredicateImpl.In<>(paths, subQuery);

    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInAsSqlGenerateSubQuery() {
    final String exp = ("() IN (Test)");
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    when(((SqlConvertible) subQuery).asSQL(stmt)).thenAnswer(new Answer<StringBuilder>() {

      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder stmt = ((StringBuilder) invocation.getArgument(0));
        stmt.append("Test");
        return stmt;
      }
    });
    final In<?> act = new PredicateImpl.In<>(Collections.emptyList(), subQuery);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
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
  void testGetExpressionsReturnsEmptyLIst() {
    assertTrue(cut.getExpressions().isEmpty());
  }

  @Test
  void testGetOperator() {
    assertNull(cut.getOperator());
  }
}
