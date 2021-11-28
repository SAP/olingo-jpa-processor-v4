package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.persistence.AttributeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    assertNull(cut.getName());
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
    final StringBuilder stmt = new StringBuilder();
    final String exp = "?10";
    assertEquals(exp, cut.asSQL(stmt).toString());
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
  void testGetValueHasConverterDifferentType() {
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAAttribute leaf = mock(JPAAttribute.class);
    final AttributeConverter<Object, Object> converter = mock(AttributeConverter.class);
    final PathImpl<?> path = new PathImpl<>(jpaPath, Optional.empty(), et, Optional.empty());

    when(jpaPath.getLeaf()).thenReturn(leaf);
    when(leaf.getConverter()).thenReturn(converter);
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
    when(converter.convertToDatabaseColumn("Value")).thenReturn("eulaV");
    cut.setPath(path);
    assertEquals("eulaV", cut.getValue());
  }
}
