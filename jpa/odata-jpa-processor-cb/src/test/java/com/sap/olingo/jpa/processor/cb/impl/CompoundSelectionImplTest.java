package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class CompoundSelectionImplTest {
  private CompoundSelectionImpl<Long> cut;
  private Selection<?> expression;
  private List<Selection<?>> selections;

  @BeforeEach
  void setup() {
    expression = mock(ExpressionImpl.class, withSettings()
        .extraInterfaces(Selection.class));
    selections = new ArrayList<>();
    selections.add(expression);
    when(expression.getAlias()).thenReturn("Test");
    when(expression.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Long.class;
      }
    });
    cut = new CompoundSelectionImpl<>(selections, Long.class, new AliasBuilder("S"));
  }

  @Test
  void testIsCompoundSelection() {
    assertTrue(cut.isCompoundSelection());
  }

  @Test
  void testGetCompoundSelectionItems() {
    final List<Selection<?>> act = cut.getCompoundSelectionItems();
    assertEquals(selections.size(), act.size());
    assertEquals(selections.get(0), ((SqlSelection<?>) act.get(0)).getSelection());
  }

  @Test
  void testAsSQLFromExpression() {
    final StringBuilder statement = new StringBuilder();
    when(((SqlConvertible) expression).asSQL(statement)).thenReturn(statement);
    assertEquals(statement, cut.asSQL(statement));
  }

  @Test
  void testAsSQLFromPath() {
    final PathImpl<?> path = mock(PathImpl.class, withSettings()
        .extraInterfaces(Selection.class));
    @SuppressWarnings("unchecked")
    final Path<Object> pathElement = (Path<Object>) mock(ExpressionImpl.class, withSettings()
        .extraInterfaces(SqlConvertible.class, Path.class));
    final StringBuilder statement = new StringBuilder();

    when(path.resolvePathElements()).thenReturn(Collections.singletonList(pathElement));
    when(((SqlConvertible) pathElement).asSQL(statement)).thenReturn(statement);

    selections.clear();
    selections.add(path);
    cut = new CompoundSelectionImpl<>(selections, Long.class, new AliasBuilder("S"));

    assertEquals(statement, cut.asSQL(statement));
  }

  @Test
  void testResolveSelectionLateExpression() {
    final List<Entry<String, JPAPath>> act = cut.resolveSelectionLate();
    assertEquals(1, act.size());
    assertEquals("Test", act.get(0).getKey());
    assertEquals(Long.class, act.get(0).getValue().getLeaf().getType());

  }
}
