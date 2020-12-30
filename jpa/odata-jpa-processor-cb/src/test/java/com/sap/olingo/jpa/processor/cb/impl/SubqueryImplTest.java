package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class SubqueryImplTest extends BuilderBaseTest {

  private SubqueryImpl<Long> cut;
  private ProcessorCriteriaBuilder cb;
  private AliasBuilder ab;
  private CriteriaQueryImpl<?> parent;

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<SubqueryImpl> c = SubqueryImpl.class;
    return Stream.of(
        arguments(c.getMethod("correlate", Join.class)),
        arguments(c.getMethod("correlate", CollectionJoin.class)),
        arguments(c.getMethod("correlate", SetJoin.class)),
        arguments(c.getMethod("correlate", ListJoin.class)),
        arguments(c.getMethod("correlate", MapJoin.class)),
        arguments(c.getMethod("correlate", Root.class)),
        arguments(c.getMethod("getCorrelatedJoins")),
        arguments(c.getMethod("isNull")),
        arguments(c.getMethod("isNotNull")),
        arguments(c.getMethod("in", Object[].class)),
        arguments(c.getMethod("in", Expression[].class)),
        arguments(c.getMethod("in", Collection.class)),
        arguments(c.getMethod("in", Expression.class)),
        arguments(c.getMethod("as", Class.class)),
        arguments(c.getMethod("alias", String.class)),
        arguments(c.getMethod("where", Predicate[].class)),
        arguments(c.getMethod("getAlias")));
  }

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> returnsSelf() throws NoSuchMethodException, SecurityException {
    final Class<SubqueryImpl> c = SubqueryImpl.class;
    return Stream.of(
        arguments(c.getMethod("select", Expression.class), mock(Expression.class)),
        arguments(c.getMethod("where", Expression.class), mock(Expression.class)),
        arguments(c.getMethod("groupBy", List.class), new ArrayList<Expression>()),
        arguments(c.getMethod("groupBy", Expression[].class), new Predicate[] {}),
        arguments(c.getMethod("having", Expression.class), mock(Predicate.class)),
        arguments(c.getMethod("having", Predicate[].class), new Predicate[] {}),
        arguments(c.getMethod("distinct", boolean.class), true),
        arguments(c.getMethod("setMaxResults", Integer.class), new Integer(10)),
        arguments(c.getMethod("setFirstResult", Integer.class), new Integer(10)),
        arguments(c.getMethod("multiselect", List.class), new ArrayList<Selection>()),
        arguments(c.getMethod("multiselect", Selection[].class), new Selection[] {}),
        arguments(c.getMethod("orderBy", List.class), new ArrayList<Order>()),
        arguments(c.getMethod("orderBy", Order[].class), new Order[] {}));

  }

  @BeforeEach
  void setup() {
    cb = mock(ProcessorCriteriaBuilder.class);
    ab = mock(AliasBuilder.class);
    parent = mock(CriteriaQueryImpl.class);
    when(parent.getServiceDocument()).thenReturn(sd);
    cut = new SubqueryImpl<>(Long.class, parent, ab, cb);
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException {

    testNotImplemented(m, cut);
  }

  @ParameterizedTest
  @MethodSource("returnsSelf")
  void testReturnsSelf(final Method m, final Object param) throws IllegalAccessException, InvocationTargetException {

    assertEquals(cut, invokeMethod(m, cut, param));
  }

  @Test
  void testGroupByArray() {
    @SuppressWarnings("unchecked")
    final Expression<Long> e = mock(Expression.class);
    cut.groupBy(e);
    assertEquals(e, cut.getGroupList().get(0));
  }

  @Test
  void testGroupByList() {
    @SuppressWarnings("unchecked")
    final Expression<Long> e = mock(Expression.class);
    cut.groupBy(Arrays.asList(e));
    assertEquals(e, cut.getGroupList().get(0));
  }

  @Test
  void testHaving() {
    final Expression<Boolean> e = mock(Predicate.class);
    cut.having(e);
    assertEquals(e, cut.getGroupRestriction());
  }

  @Test
  void testHavingArray() {
    final Predicate p1 = mock(Predicate.class);
    final Predicate[] p2 = { p1 };
    cut.having(p2);
    assertEquals(p1, cut.getGroupRestriction());
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
    when(ab.getNext()).thenReturn("Test");
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
}
