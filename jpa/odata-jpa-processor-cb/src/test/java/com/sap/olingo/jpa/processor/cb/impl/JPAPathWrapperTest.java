package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.criteria.Selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class JPAPathWrapperTest {

  private static final String ALIAS = "Example";
  private Selection<String> selection;
  private JPAPathWrapper cut;

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setup() {
    selection = mock(Selection.class);
    when(selection.getJavaType()).thenAnswer(new Answer<Class<String>>() {
      @Override
      public Class<String> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
    when(selection.getAlias()).thenReturn(ALIAS);
    cut = new JPAPathWrapper(selection);
  }

  static Stream<Arguments> returnsFalse() throws NoSuchMethodException, SecurityException {
    final Class<JPAPathWrapper> c = JPAPathWrapper.class;
    return Stream.of(
        arguments(c.getMethod("ignore")),
        arguments(c.getMethod("isTransient")));
  }

  @ParameterizedTest
  @MethodSource("returnsFalse")
  public void testMethodReturnsFalse(final Method m) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    assertFalse((Boolean) m.invoke(cut));
  }

  @Test
  public void testIsPartOfGroupsIsFalse() throws ODataJPAModelException {
    assertFalse(cut.isPartOfGroups(Collections.singletonList("Test")));
  }

  @Test
  public void testGetAlias() {
    assertEquals(ALIAS, cut.getAlias());
  }

  @Test
  public void testGetDBFieldNameNull() {
    assertNull(cut.getDBFieldName());
  }

  @Test
  public void testGetPathReturnsLeaf() {
    final List<JPAElement> act = cut.getPath();
    assertEquals(1, act.size());
    assertEquals(String.class, ((JPAAttribute) act.get(0)).getType());
  }

  @Test
  public void testCompareTo() {
    final JPAPath path = mock(JPAPath.class);
    when(path.getAlias()).thenReturn(ALIAS);
    assertEquals(0, cut.compareTo(path));
    when(path.getAlias()).thenReturn("Test.Test");
    assertTrue(cut.compareTo(path) < 0);
    when(path.getAlias()).thenReturn("Alpha");
    assertTrue(cut.compareTo(path) > 0);
  }
}
