package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.criteria.Expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;

class ParameterExpressionImplTest {
  private ParameterExpression<?, Object> cut;

  @BeforeEach
  void setup() {
    cut = new ParameterExpression<>(10, "Value");
  }

  @Test
  void testGetNameReturnsNull() {
    assertEquals("10", cut.getName());
  }

  @Test
  void testGetPosition() {
    assertEquals(10, cut.getPosition());
  }

  @Test
  void testGetParameterType() {
    assertEquals(String.class, cut.getParameterType());
  }

  @Test
  void testGetJavaType() {
    assertEquals(String.class, cut.getJavaType());
  }

  @Test
  void testAsSQL() {
    final StringBuilder statement = new StringBuilder();
    final String exp = "?10";
    assertEquals(exp, cut.asSQL(statement).toString());
  }

  @Test
  void testGetValue() {
    assertEquals("Value", cut.getValue());
  }

  @Test
  void testGetValueIsEnum() {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAAttribute leaf = mock(JPAAttribute.class);
    final PathImpl<?> path = new PathImpl<>(jpaPath, Optional.empty(), et, Optional.empty());

    when(jpaPath.getLeaf()).thenReturn(leaf);
    when(leaf.isEnum()).thenReturn(true);

    cut = new ParameterExpression<>(10, ABCClassification.C);
    cut.setPath(path);
    assertEquals(2, cut.getValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetValueWithConverter() {
    final AttributeConverter<Object, Object> converter = mock(AttributeConverter.class);
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath pX = mock(JPAPath.class);
    final JPAAttribute aX = mock(JPAAttribute.class);
    final Expression<?> x = new PathImpl<>(pX, Optional.empty(), et, Optional.of("P1"));
    when(pX.getLeaf()).thenReturn(aX);
    when(aX.getConverter()).thenReturn(null);
    when(aX.getRawConverter()).thenReturn(converter);
    when(converter.convertToDatabaseColumn(any())).thenReturn("db");

    cut = new ParameterExpression<>(2, "attribute", x);
    assertEquals("db", cut.getValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetValueHasConverterDifferentType() {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAAttribute leaf = mock(JPAAttribute.class);
    final AttributeConverter<Object, Object> converter = mock(AttributeConverter.class);
    final PathImpl<?> path = new PathImpl<>(jpaPath, Optional.empty(), et, Optional.empty());

    when(jpaPath.getLeaf()).thenReturn(leaf);
    when(leaf.getConverter()).thenReturn(converter);
    when(leaf.getRawConverter()).thenReturn(converter);
    when(converter.convertToDatabaseColumn("Value")).thenReturn(Integer.valueOf(100));
    cut.setPath(path);
    assertEquals(100, cut.getValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetValueHasConverterSameType() {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAAttribute leaf = mock(JPAAttribute.class);
    final AttributeConverter<Object, Object> converter = mock(AttributeConverter.class);
    final PathImpl<?> path = new PathImpl<>(jpaPath, Optional.empty(), et, Optional.empty());

    when(jpaPath.getLeaf()).thenReturn(leaf);
    when(leaf.getConverter()).thenReturn(converter);
    when(leaf.getRawConverter()).thenReturn(converter);
    when(converter.convertToDatabaseColumn("Value")).thenReturn("eulaV");
    cut.setPath(path);
    assertEquals("eulaV", cut.getValue());
  }

  @TestFactory
  Stream<DynamicTest> testIsEqual() {

    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath pX = mock(JPAPath.class);
    final JPAAttribute aX = mock(JPAAttribute.class);
    final JPAPath pY = mock(JPAPath.class);
    final JPAAttribute aY = mock(JPAAttribute.class);
    when(pX.getLeaf()).thenReturn(aX);
    when(pY.getLeaf()).thenReturn(aY);

    final Expression<?> x = new PathImpl<>(pX, Optional.empty(), et, Optional.of("P1"));
    final Expression<?> y = new PathImpl<>(pY, Optional.empty(), et, Optional.of("P2"));

    cut.setPath(x);
    return Stream.of(
        dynamicTest("Not Equals a String", () -> assertNotEquals("A String", cut)),
        dynamicTest("Not Equals other Value", () -> assertNotEquals(new ParameterExpression<>(2, "Value2", x), cut)),
        dynamicTest("Not Equals other Path", () -> assertNotEquals(new ParameterExpression<>(2, "Value", y), cut)),
        dynamicTest("Is Equal same Value and Path", () -> assertEquals(new ParameterExpression<>(2, "Value", x), cut)),
        dynamicTest("Is Equal Self", () -> assertEquals(cut, cut)));

  }

  @Test
  void testToStringContainsValue() {
    assertTrue(cut.toString().contains("Value"));
  }

  @Test
  void testHashCodeNotZero() {
    assertNotEquals(0, cut.hashCode());
  }

  @Test
  void testConstructorWithPath() {
    final String pathString = "TestString";
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath pX = mock(JPAPath.class);
    final JPAAttribute aX = mock(JPAAttribute.class);
    final Expression<?> x = new PathImpl<>(pX, Optional.empty(), et, Optional.of("P1"));
    when(pX.getLeaf()).thenReturn(aX);
    when(pX.toString()).thenReturn(pathString);
    cut = new ParameterExpression<>(2, "Value2", x);

    assertTrue(cut.toString().contains(pathString));
  }

  @Test
  void testConstructorWithExpression() {
    final Expression<?> x = mock(Expression.class);
    cut = new ParameterExpression<>(2, "Value2", x);

    assertTrue(cut.toString().contains("jpaPath=Optional.empty"));
  }
}
