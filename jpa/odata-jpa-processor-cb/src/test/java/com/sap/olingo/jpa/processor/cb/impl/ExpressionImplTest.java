package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.criteria.Expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

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
  void setup() throws ODataJPAModelException {
    cut = new ExpressionTest();
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {
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

  private static class ExpressionTest extends ExpressionImpl<Long> {

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      return statement;
    }

  }
}
