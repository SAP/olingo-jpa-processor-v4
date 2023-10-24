package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaOneFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaTwoFunctions;

class IntermediateFunctionFactoryTest extends TestMappingRoot {
  private TestHelper helper;

  private Reflections reflections;
  private IntermediateFunctionFactory<?> cut;
  private Set<Class<? extends ODataFunction>> javaFunctions;

  @BeforeEach
  void setUp() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);

    reflections = mock(Reflections.class);
    cut = new IntermediateFunctionFactory<>();
    javaFunctions = new HashSet<>();
    when(reflections.getSubTypesOf(ODataFunction.class)).thenReturn(javaFunctions);
  }

  @Test
  void checkReturnEmptyMapIfReflectionsNull() throws ODataJPAModelException {
    final Reflections r = null;
    assertNotNull(cut.create(new JPADefaultEdmNameBuilder(PUNIT_NAME), r, helper.schema));
  }

  @Test
  void checkReturnEmptyMapIfNoJavaFunctionsFound() throws ODataJPAModelException {
    assertNotNull(cut.create(new JPADefaultEdmNameBuilder(PUNIT_NAME), reflections, helper.schema));
  }

  @Test
  void checkReturnMapWithOneIfOneJavaFunctionsFound() throws ODataJPAModelException {
    javaFunctions.add(ExampleJavaOneFunction.class);
    final Map<? extends String, ? extends IntermediateFunction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(1, act.size());
  }

  @Test
  void checkReturnMapWithTwoIfTwoJavaFunctionsFound() throws ODataJPAModelException {
    javaFunctions.add(ExampleJavaTwoFunctions.class);
    final Map<? extends String, ? extends IntermediateFunction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(2, act.size());
  }

  @Test
  void checkReturnMapWithWithJavaFunctionsFromTwoClassesFound() throws ODataJPAModelException {
    javaFunctions.add(ExampleJavaOneFunction.class);
    javaFunctions.add(ExampleJavaTwoFunctions.class);
    final Map<? extends String, ? extends IntermediateFunction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(3, act.size());
  }

}
