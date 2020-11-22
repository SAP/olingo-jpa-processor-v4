package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ExpressionPath;

class ExpressionPathTest extends BuilderBaseTest {

  private Optional<String> name;
  private String tableAlias;
  private StringBuilder stmt;

  private ExpressionPath<Long> cut;

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<ExpressionPath> c = ExpressionPath.class;
    return Stream.of(
        arguments(c.getMethod("getModel")),
        arguments(c.getMethod("getParentPath")),
        arguments(c.getMethod("get", SingularAttribute.class)),
        arguments(c.getMethod("get", PluralAttribute.class)),
        arguments(c.getMethod("get", MapAttribute.class)),
        arguments(c.getMethod("get", String.class)),
        arguments(c.getMethod("type")));
  }

  @BeforeEach
  void setup() {
    name = Optional.of("area");
    tableAlias = "X2";
    stmt = new StringBuilder();
    cut = new ExpressionPath<>(name, tableAlias);
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {

    testNotImplemented(m, cut);
  }

  @Test
  void testAsSql() {
    final String exp = "X2.area";
    assertEquals(exp, cut.asSQL(stmt).toString());
  }

  @Test
  void testAsSqlThrowsExceptionOnMissingName() {
    name = Optional.empty();
    cut = new ExpressionPath<>(name, tableAlias);
    assertThrows(IllegalStateException.class, () -> cut.asSQL(stmt));
  }
}
