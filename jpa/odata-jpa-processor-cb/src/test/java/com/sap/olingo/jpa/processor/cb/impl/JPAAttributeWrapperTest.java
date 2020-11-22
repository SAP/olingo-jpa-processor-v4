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
import java.util.Collection;
import java.util.stream.Stream;

import javax.persistence.criteria.Selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAAttributeWrapperTest {

  private Selection<String> selection;
  private JPAAttributeWrapper cut;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    selection = mock(Selection.class);
    when(selection.getJavaType()).thenAnswer(new Answer<Class<String>>() {
      @Override
      public Class<String> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
    cut = new JPAAttributeWrapper(selection);
  }

  static Stream<Arguments> returnsNull() throws NoSuchMethodException, SecurityException {
    final Class<JPAAttributeWrapper> c = JPAAttributeWrapper.class;
    return Stream.of(
        arguments(c.getMethod("getExternalFQN")),
        arguments(c.getMethod("getExternalName")),
        arguments(c.getMethod("getInternalName")),
        arguments(c.getMethod("getConverter")),
        arguments(c.getMethod("getRawConverter")),
        arguments(c.getMethod("getEdmType")),
        arguments(c.getMethod("getProperty")),
        arguments(c.getMethod("getStructuredType")),
        arguments(c.getMethod("getCalculatorConstructor")));
  }

  static Stream<Arguments> returnsFalse() throws NoSuchMethodException, SecurityException {
    final Class<JPAAttributeWrapper> c = JPAAttributeWrapper.class;
    return Stream.of(
        arguments(c.getMethod("isAssociation")),
        arguments(c.getMethod("isCollection")),
        arguments(c.getMethod("isComplex")),
        arguments(c.getMethod("isEnum")),
        arguments(c.getMethod("isEtag")),
        arguments(c.getMethod("isKey")),
        arguments(c.getMethod("isSearchable")),
        arguments(c.getMethod("hasProtection")),
        arguments(c.getMethod("isTransient")));
  }

  static Stream<Arguments> returnsEmptyCollection() throws NoSuchMethodException, SecurityException {
    final Class<JPAAttributeWrapper> c = JPAAttributeWrapper.class;
    return Stream.of(
        arguments(c.getMethod("getProtectionClaimNames")),
        arguments(c.getMethod("getRequiredProperties")));
  }

  @ParameterizedTest
  @MethodSource("returnsNull")
  void testMethodReturnsNull(final Method m) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    assertNull(m.invoke(cut));
  }

  @ParameterizedTest
  @MethodSource("returnsFalse")
  void testMethodReturnsFalse(final Method m) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    assertFalse((Boolean) m.invoke(cut));
  }

  @ParameterizedTest
  @MethodSource("returnsEmptyCollection")
  void testMethodReturnsEmptyCollection(final Method m) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    assertTrue(((Collection<?>) m.invoke(cut)).isEmpty());
  }

  @Test
  void testGetProtectionPathReturnsEmpty() throws ODataJPAModelException {
    assertTrue(cut.getProtectionPath("Test").isEmpty());
  }

  @Test
  void testGetType() {
    assertEquals(String.class, cut.getType());
  }
}
