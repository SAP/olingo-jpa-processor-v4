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
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testaction.ActionWithOverload;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaOneAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaTwoActions;

class IntermediateActionFactoryTest extends TestMappingRoot {
  private TestHelper helper;

  private Reflections reflections;
  private IntermediateActionFactory cut;
  private Set<Class<? extends ODataAction>> javaActions;

  @BeforeEach
  void setUp() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);

    reflections = mock(Reflections.class);
    cut = new IntermediateActionFactory();
    javaActions = new HashSet<>();
    when(reflections.getSubTypesOf(ODataAction.class)).thenReturn(javaActions);
  }

  @Test
  void checkReturnEmptyMapIfReflectionsNull() throws ODataJPAModelException {
    final Reflections reflections = null;
    assertNotNull(cut.create(new JPADefaultEdmNameBuilder(PUNIT_NAME), reflections, helper.schema));
  }

  @Test
  void checkReturnEmptyMapIfNoJavaActionsFound() throws ODataJPAModelException {
    assertNotNull(cut.create(new JPADefaultEdmNameBuilder(PUNIT_NAME), reflections, helper.schema));
  }

  @Test
  void checkReturnMapWithOneIfOneJavaActionsFound() throws ODataJPAModelException {
    javaActions.add(ExampleJavaOneAction.class);
    final Map<ODataActionKey, IntermediateJavaAction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(1, act.size());
  }

  @Test
  void checkReturnMapWithTwoIfTwoJavaActionsFound() throws ODataJPAModelException {
    javaActions.add(ExampleJavaTwoActions.class);
    final Map<ODataActionKey, IntermediateJavaAction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(2, act.size());
  }

  @Test
  void checkReturnMapWithWithJavaActionsFromTwoClassesFound() throws ODataJPAModelException {
    javaActions.add(ExampleJavaOneAction.class);
    javaActions.add(ExampleJavaTwoActions.class);
    final Map<ODataActionKey, IntermediateJavaAction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(3, act.size());
  }

  @Test
  void checkReturnMapWithWithOverloadedActions() throws ODataJPAModelException {
    javaActions.add(ActionWithOverload.class);
    final Map<ODataActionKey, IntermediateJavaAction> act = cut.create(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), reflections, helper.schema);
    assertEquals(3, act.size());
  }
}
