package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.DistinctExpression;
import com.sap.olingo.jpa.processor.core.testmodel.Country;

class ExpressionImplTest {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private ExpressionImpl<Long> cut;

  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<ExpressionTest> c = ExpressionImplTest.ExpressionTest.class;
    return Stream.of(
        arguments(c.getMethod("as", Class.class)),
        arguments(c.getMethod("in", Collection.class)),
        arguments(c.getMethod("in", Expression[].class)),
        arguments(c.getMethod("in", Expression.class)),
        arguments(c.getMethod("in", Object[].class)),
        arguments(c.getMethod("isNotNull")),
        arguments(c.getMethod("isNull")),
        arguments(c.getMethod("getCompoundSelectionItems")));
  }

  @BeforeEach
  void setup() {
    cut = new ExpressionTest();
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalArgumentException {
    InvocationTargetException e;
    if (m.getParameterCount() >= 1) {
      final Class<?>[] params = m.getParameterTypes();
      final List<Object> paramValues = new ArrayList<>(m.getParameterCount());
      for (int i = 0; i < m.getParameterCount(); i++) {
        if (params[i] == char.class)
          paramValues.add(' ');
        else
          paramValues.add(null);
      }
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut, paramValues.toArray()));
    } else {
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut));
    }
    assertTrue(e.getCause() instanceof NotImplementedException);
  }

  @Test
  void testIsCompoundSelectionReturnsFalse() {
    assertFalse(cut.isCompoundSelection());
  }

  @Test
  void testGetAliasReturnsEmptyStringIfNotSet() {
    assertEquals("", cut.getAlias());
  }

  @Test
  void testGetAliasReturnsAliceSet() {
    cut.alias(PUNIT_NAME);
    assertEquals(PUNIT_NAME, cut.getAlias());
  }

  @Test
  void testGetAliasThrowsExceptionOnSecondCall() {
    cut.alias(PUNIT_NAME);
    assertThrows(IllegalAccessError.class, () -> cut.alias("Test"));
  }

  @Test
  void testGetJavaTypeReturnsNull() {
    assertNull(cut.getJavaType());
  }

  @Test
  void testDistinctExpressionRethrowsAsIllegalStateException() throws ODataJPAModelException {
    final JPAEntityType type = mock(JPAEntityType.class);
    final AliasBuilder ab = new AliasBuilder();
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    when(type.getKey()).thenThrow(ODataJPAModelException.class);
    when(type.getTypeClass()).thenAnswer(new Answer<Class<Country>>() {
      @Override
      public Class<Country> answer(final InvocationOnMock invocation) throws Throwable {
        return Country.class;
      }
    });

    final FromImpl<?, ?> p = new FromImpl<>(type, ab, cb);
    final DistinctExpression<Long> act = new ExpressionImpl.DistinctExpression<>(p);
    final StringBuilder sb = new StringBuilder();
    assertThrows(IllegalStateException.class, () -> act.asSQL(sb));
  }

  private static class ExpressionTest extends ExpressionImpl<Long> {

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      return statement;
    }

  }
}
