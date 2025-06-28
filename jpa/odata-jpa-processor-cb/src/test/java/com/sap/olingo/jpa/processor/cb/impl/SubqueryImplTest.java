package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class SubqueryImplTest extends BuilderBaseTest {

  private SubqueryImpl<Long> cut;
  private ProcessorCriteriaBuilder cb;
  private AliasBuilder aliasBuilder;
  private CriteriaQueryImpl<?> parent;
  private ProcessorSqlPatternProvider patternProvider;

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<SubqueryImpl> clazz = SubqueryImpl.class;
    return Stream.of(
        arguments(clazz.getMethod("correlate", Join.class)),
        arguments(clazz.getMethod("correlate", CollectionJoin.class)),
        arguments(clazz.getMethod("correlate", SetJoin.class)),
        arguments(clazz.getMethod("correlate", ListJoin.class)),
        arguments(clazz.getMethod("correlate", MapJoin.class)),
        arguments(clazz.getMethod("correlate", Root.class)),
        arguments(clazz.getMethod("getCorrelatedJoins")),
        arguments(clazz.getMethod("isNull")),
        arguments(clazz.getMethod("isNotNull")),
        arguments(clazz.getMethod("in", Object[].class)),
        arguments(clazz.getMethod("in", Expression[].class)),
        arguments(clazz.getMethod("in", Collection.class)),
        arguments(clazz.getMethod("in", Expression.class)),
        arguments(clazz.getMethod("as", Class.class)),
        arguments(clazz.getMethod("alias", String.class)),
        arguments(clazz.getMethod("where", Predicate[].class)),
        arguments(clazz.getMethod("getAlias")));
  }

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> returnsSelf() throws NoSuchMethodException, SecurityException {
    final Class<SubqueryImpl> clazz = SubqueryImpl.class;
    return Stream.of(
        arguments(clazz.getMethod("select", Expression.class), mock(Expression.class)),
        arguments(clazz.getMethod("where", Expression.class), mock(Expression.class)),
        arguments(clazz.getMethod("groupBy", List.class), new ArrayList<>()),
        arguments(clazz.getMethod("groupBy", Expression[].class), new Predicate[] {}),
        arguments(clazz.getMethod("having", Expression.class), mock(Predicate.class)),
        arguments(clazz.getMethod("having", Predicate[].class), new Predicate[] {}),
        arguments(clazz.getMethod("distinct", boolean.class), true),
        arguments(clazz.getMethod("setMaxResults", Integer.class), Integer.valueOf(10)),
        arguments(clazz.getMethod("setFirstResult", Integer.class), Integer.valueOf(10)),
        arguments(clazz.getMethod("multiselect", List.class), new ArrayList<>()),
        arguments(clazz.getMethod("multiselect", Selection[].class), new Selection[] {}),
        arguments(clazz.getMethod("orderBy", List.class), new ArrayList<>()),
        arguments(clazz.getMethod("orderBy", Order[].class), new Order[] {}));

  }

  @BeforeEach
  void setup() {
    cb = mock(ProcessorCriteriaBuilder.class);
    aliasBuilder = mock(AliasBuilder.class);
    parent = mock(CriteriaQueryImpl.class);
    patternProvider = spy(SqlDefaultPattern.class);
    when(parent.getServiceDocument()).thenReturn(sd);
    cut = new SubqueryImpl<>(Long.class, parent, aliasBuilder, cb, patternProvider, sd);
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method method) throws IllegalAccessException {

    testNotImplemented(method, cut);
  }

  @ParameterizedTest
  @MethodSource("returnsSelf")
  void testReturnsSelf(final Method method, final Object param) throws IllegalAccessException,
      InvocationTargetException {

    assertEquals(cut, invokeMethod(method, cut, param));
  }

  @Test
  void testGroupByArray() {
    @SuppressWarnings("unchecked")
    final Expression<Long> expression = mock(Expression.class);
    cut.groupBy(expression);
    assertEquals(expression, cut.getGroupList().get(0));
  }

  @Test
  void testGroupByList() {
    @SuppressWarnings("unchecked")
    final Expression<Long> expression = mock(Expression.class);
    cut.groupBy(Arrays.asList(expression));
    assertEquals(expression, cut.getGroupList().get(0));
  }

  @Test
  void testHaving() {
    final Expression<Boolean> expression = mock(Predicate.class);
    cut.having(expression);
    assertEquals(expression, cut.getGroupRestriction());
  }

  @Test
  void testHavingArray() {
    final Predicate predicate = mock(Predicate.class);
    final Predicate[] predicates = { predicate };
    cut.having(predicates);
    assertEquals(predicate, cut.getGroupRestriction());
  }

  @Test
  void testDistinct() {
    cut.distinct(true);
    assertTrue(cut.isDistinct());
  }

  @Test
  void testGetParent() {
    assertEquals(parent, cut.getParent());
  }

  @Test
  void testGetContainingQuery() {
    assertEquals(parent, cut.getContainingQuery());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFromReturnsRoot() throws ODataJPAModelException {
    final JPAEntityType et = mock(JPAEntityType.class);
    final EntityType<Person> jpaEt = mock(EntityType.class);
    when(jpaEt.getJavaType()).thenReturn(Person.class);
    when(sd.getEntity(Person.class)).thenReturn(et);
    when(et.getTypeClass()).thenAnswer(new ClassAnswer(Person.class));
    when(aliasBuilder.getNext()).thenReturn("Test");
    assertNotNull(cut.from(jpaEt));
    assertNotNull(cut.getRoots());
    assertEquals(1, cut.getRoots().size());
  }

  @Test
  void testGetSelection() {
    @SuppressWarnings("unchecked")
    final Expression<Long> expression = mock(Expression.class);
    cut.select(expression);
    assertNotNull(cut.getSelection());
    assertEquals(expression, cut.getSelection());
  }

  @Test
  void testGetRestriction() {
    final Expression<Boolean> restriction = mock(Predicate.class);
    cut.where(restriction);
    assertNotNull(cut.getRestriction());
    assertEquals(restriction, cut.getRestriction());
  }

  @Test
  void testGetCompoundSelectionItemsReturnsEmptyList() {
    assertNotNull(cut.getCompoundSelectionItems());
    assertTrue(cut.getCompoundSelectionItems().isEmpty());
  }

  @Test
  void testGetCompoundSelectionItemsReturnsResolvedSelection() {
    @SuppressWarnings("unchecked")
    final Expression<Long> expression = mock(Expression.class, withSettings().extraInterfaces(CompoundSelection.class));
    when(expression.getCompoundSelectionItems())
        .thenReturn(Arrays.asList(mock(Expression.class), mock(Expression.class)));
    cut.select(expression);
    assertNotNull(cut.getCompoundSelectionItems());
    assertEquals(1, cut.getCompoundSelectionItems().size());
  }

  @Test
  void testGetCompoundSelectionItemsReturnsSelection() {
    @SuppressWarnings("unchecked")
    final Expression<Long> expression = mock(Expression.class);
    cut.select(expression);
    assertNotNull(cut.getCompoundSelectionItems());
    assertEquals(1, cut.getCompoundSelectionItems().size());
    assertEquals(expression, ((SelectionImpl<?>) cut.getCompoundSelectionItems().get(0)).selection);
  }

  @Test
  void testAsSQLUsesSqlPatternForLimit() {
    var statement = new StringBuilder();
    @SuppressWarnings("unchecked")
    final ExpressionImpl<Long> expression = mock(ExpressionImpl.class);
    when(expression.asSQL(any())).thenReturn(statement);
    cut.select(expression);

    cut.setNumberOfResults(10);
    cut.asSQL(statement);
    verify(patternProvider, times(1)).getMaxResultsPattern();
  }

  @Test
  void testAsSQLUsesSqlPatternForOffset() {
    var statement = new StringBuilder();
    @SuppressWarnings("unchecked")
    final ExpressionImpl<Long> expression = mock(ExpressionImpl.class);
    when(expression.asSQL(any())).thenReturn(statement);
    cut.select(expression);

    cut.setFirstResult(8);
    cut.asSQL(statement);
    verify(patternProvider, times(1)).getFirstResultPattern();
  }
}
