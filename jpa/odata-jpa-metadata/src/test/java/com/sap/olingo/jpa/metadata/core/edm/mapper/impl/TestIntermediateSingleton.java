package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateSingletonAccess;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;

class TestIntermediateSingleton extends TestMappingRoot {
  private IntermediateSchema schema;
  private Set<EntityType<?>> etList;
  private JPADefaultEdmNameBuilder nameBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    etList = emf.getMetamodel().getEntities();
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), r);
  }

  @Test
  void checkNewSingleton() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    assertNotNull(singleton);
  }

  @Test
  void checkGetInternalName() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    assertNotNull(singleton.getInternalName());
  }

  @Test
  void checkGetExternalName() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    assertNotNull(singleton.getExternalName());
    assertEquals("Singleton", singleton.getExternalName());
  }

  @Test
  void checkGetExternalFQN() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    assertNotNull(singleton.getExternalFQN());
    assertEquals(PUNIT_NAME, singleton.getExternalFQN().getNamespace());
    assertEquals("Singleton", singleton.getExternalFQN().getName());
  }

  @Test
  void checkGetEdmItem() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    final CsdlSingleton item = singleton.getEdmItem();
    assertNotNull(item);
  }

  @Test
  void checkGetEdmItemValues() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    final CsdlSingleton item = singleton.getEdmItem();
    assertEquals("Singleton", item.getName());
    assertEquals(PUNIT_NAME + ".Singleton", item.getType());
    assertEquals(1, et.getEdmItem().getNavigationProperties().size());
    assertEquals(0, item.getNavigationPropertyBindings().size());
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    final CsdlSingleton item = singleton.getEdmItem();
    final List<CsdlAnnotation> act = item.getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.TopSupported", act.get(0).getTerm());
  }

  @Test
  void checkEntityTypeOfSingletonOnly() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    final IntermediateEntityType<CurrentUser> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CurrentUser.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et);
    final CsdlSingleton item = singleton.getEdmItem();
    assertEquals(PUNIT_NAME + ".BusinessPartner", item.getType());
  }

  @SuppressWarnings("unchecked")
  private <T> EntityType<T> getEntityType(final Class<T> type) {
    for (final EntityType<?> entityType : etList) {
      if (entityType.getJavaType().equals(type)) {
        return (EntityType<T>) entityType;
      }
    }
    return null;
  }

  private class PostProcessor extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("communicationData")) {
          property.setIgnore(true);
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}

    @Override
    public void processSingleton(final IntermediateSingletonAccess singleton) {

      final CsdlConstantExpression mimeType = new CsdlConstantExpression(ConstantExpressionType.Bool, "false");
      final CsdlAnnotation annotation = new CsdlAnnotation();
      annotation.setExpression(mimeType);
      annotation.setTerm("Capabilities.TopSupported");
      final List<CsdlAnnotation> annotations = new ArrayList<>();
      annotations.add(annotation);
      singleton.addAnnotations(annotations);
    }
  }
}
