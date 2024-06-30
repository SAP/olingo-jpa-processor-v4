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
  private CriteriaQuery<Tuple> query;

  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<PredicateImpl> clazz = PredicateImpl.class;
    return Stream.of(
        arguments(clazz.getMethod("in", Expression.class)),
        arguments(clazz.getMethod("in", Collection.class)),
        arguments(clazz.getMethod("in", Expression[].class)),
        arguments(clazz.getMethod("in", Object[].class)),
        arguments(clazz.getMethod("getCompoundSelectionItems")),
        arguments(clazz.getMethod("getJavaType")),
        arguments(clazz.getMethod("as", Class.class)));
  }

  @BeforeEach
  void setup() {
    cut = new PredicateImpl.NotPredicate(mock(SqlConvertible.class));

    final CriteriaBuilder cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    statement = new StringBuilder();
    query = cb.createTupleQuery();
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method method) throws IllegalAccessException, IllegalArgumentException {

    testNotImplemented(method, cut);
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
  void testInGetExpressionReturnsFirstPath() {

    final Root<?> adminDivision = query.from(AdministrativeDivision.class);
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
    assertThrows(NotImplementedException.class, () -> act.value(mock(Expression.class)));
  }

  @Test
  void testInAsSqlGeneratePath() {
    final String exp = ("(E0.\"CodeID\", E0.\"CodePublisher\") IN ()");
    final Root<?> adminDivision = query.from(AdministrativeDivision.class);
    final List<Path<?>> paths = Arrays.asList(adminDivision.get("codeID"), adminDivision.get("codePublisher"));
    @SuppressWarnings("unchecked")
    final Subquery<Long> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    when(((SqlConvertible) subQuery).asSQL(statement)).thenAnswer(new Answer<StringBuilder>() {
      @Override
      public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
        final StringBuilder stmt = ((StringBuilder) invocation.getArgument(0));
        stmt.append("");
        return stmt;
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
        final StringBuilder stmt = ((StringBuilder) invocation.getArgument(0));
        stmt.append("Test");
        return stmt;
      }
    });
    final In<?> act = new PredicateImpl.In<>(Collections.emptyList(), subQuery);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testInCreatedFromExpression() {
    @SuppressWarnings("unchecked")
    final Path<String> path = mock(Path.class);
    final Predicate in = new PredicateImpl.In<>(path, null);
    assertNotNull(in);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInAddExpressionValue() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path, null);
    assertNotNull(in.value(subQuery));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInThrowsExceptionOnMultipleAddExpressionValue() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path, null);
    in.value(subQuery);
    assertThrows(NotImplementedException.class, () -> in.value(subQuery));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInAddFixValue() {
    final Path<String> path = mock(Path.class);
    final In<String> in = new PredicateImpl.In<>(path, new ParameterBuffer());
    assertNotNull(in.value("Test"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInAddMultipleFixValue() {
    final Path<String> path = mock(Path.class);
    final In<String> in = new PredicateImpl.In<>(path, new ParameterBuffer());
    assertNotNull(in.value("Test1").value("Test2").value("Test3"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInThrowsExceptionOnAddExpressionIfFixValueExists() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path, new ParameterBuffer());
    in.value("Test1");
    assertThrows(IllegalStateException.class, () -> in.value(subQuery));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInThrowsExceptionOnAddFixValueIfExpressionExists() {
    final Path<String> path = mock(Path.class);
    final Subquery<String> subQuery = mock(Subquery.class, withSettings().extraInterfaces(SqlConvertible.class));
    final In<String> in = new PredicateImpl.In<>(path, new ParameterBuffer());
    in.value(subQuery);
    assertThrows(IllegalStateException.class, () -> in.value("Test1"));
  }

  @Test
  void testInAddMultipleFixValueAsSQL() {
    final String exp = ("() IN (?1, ?2, ?3)");
    final ParameterBuffer parameter = new ParameterBuffer();
    final In<String> in = new PredicateImpl.In<>(Collections.emptyList(), parameter);
    in.value("Test1").value("Test2").value("Test3");
    assertEquals(exp, ((PredicateImpl.In<String>) in).asSQL(statement).toString());
    assertEquals(3, parameter.getParameters().size());
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
