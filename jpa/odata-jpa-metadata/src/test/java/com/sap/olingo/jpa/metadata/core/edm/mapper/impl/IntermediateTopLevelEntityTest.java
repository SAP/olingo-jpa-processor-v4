package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;

class IntermediateTopLevelEntityTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private JPADefaultEdmNameBuilder nameBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), r);
  }

  @Test
  void checkQueryExtentionProvderPresent() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    assertFalse(singleton.getQueryExtention().isPresent());
  }
}
