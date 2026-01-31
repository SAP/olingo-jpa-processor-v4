package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.processor.core.util.Assertions.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPASingleton;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateSingletonAccess;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsParent;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsSingleton;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;

class IntermediateSingletonTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private JPADefaultEdmNameBuilder nameBuilder;
  private IntermediateAnnotationInformation annotationInfo;
  private IntermediateReferences references;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    references = mock(IntermediateReferences.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>(), references);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), reflections, annotationInfo, true);
  }

  @Test
  void checkNewSingleton() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    assertNotNull(singleton);
  }

  @Test
  void checkGetInternalName() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    assertNotNull(singleton.getInternalName());
  }

  @Test
  void checkGetExternalName() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    assertNotNull(singleton.getExternalName());
    assertEquals("Singleton", singleton.getExternalName());
  }

  @Test
  void checkGetExternalFQN() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    assertNotNull(singleton.getExternalFQN());
    assertEquals(PUNIT_NAME, singleton.getExternalFQN().getNamespace());
    assertEquals("Singleton", singleton.getExternalFQN().getName());
  }

  @Test
  void checkGetEdmItem() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    final CsdlSingleton item = singleton.getEdmItem();
    assertNotNull(item);
  }

  @Test
  void checkGetEdmItemValues() throws ODataJPAModelException {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
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
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
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
    final IntermediateSingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    final CsdlSingleton item = singleton.getEdmItem();
    assertEquals(PUNIT_NAME + ".Person", item.getType());
  }

  @Test
  void checkJavaAnnotationsOneAnnotation() {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final Map<String, Annotation> act = et.javaAnnotations(EdmEntityType.class.getPackage().getName());
    assertEquals(1, act.size());
    assertNotNull(act.get("EdmEntityType"));
  }

  @Test
  void checkJavaAnnotationsTwoAnnotations() {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final Map<String, Annotation> act = et.javaAnnotations(Entity.class.getPackage().getName());
    assertEquals(2, act.size());
    assertNotNull(act.get("Table"));
    assertNotNull(act.get("Entity"));
  }

  @Test
  void checkJavaAnnotationsNoAnnotations() {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Singleton.class), schema);
    final Map<String, Annotation> act = et.javaAnnotations(Test.class.getPackage().toString());
    assertTrue(act.isEmpty());
  }

  @Test
  void checkGetAnnotationReturnsExistingAnnotation() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsSingleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsSingleton.class), schema);
    final JPASingleton singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    final CsdlAnnotation act = singleton.getAnnotation("Capabilities", "CountRestrictions");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPASingleton es = new IntermediateSingleton(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotation("Capabilities", "Filter"));
  }

  @Test
  void checkAsUserGroupRestrictedUserRestrictsNavigations() throws ODataJPAModelException {
    final IntermediateEntityType<CurrentUser> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(CurrentUser.class), schema);
    final var singleton = new IntermediateSingleton(nameBuilder, et, annotationInfo);

    final IntermediateSingleton act = singleton.asUserGroupRestricted(List.of("Company"), true);
    assertEquals(singleton.getExternalFQN(), act.getExternalFQN());
    assertEquals(singleton.getExternalName(), act.getExternalName());
    assertEquals(singleton.getInternalName(), act.getInternalName());
    assertListEquals(singleton.getEdmItem().getNavigationPropertyBindings(), act.getEdmItem()
        .getNavigationPropertyBindings(),
        CsdlNavigationPropertyBinding.class);
    final IntermediateSingleton act2 = singleton.asUserGroupRestricted(List.of("Person"), true);
    assertEquals(singleton.getExternalFQN(), act2.getExternalFQN());
    assertEquals(singleton.getExternalName(), act2.getExternalName());
    assertEquals(singleton.getInternalName(), act2.getInternalName());
    assertEquals(singleton.getEdmItem().getNavigationPropertyBindings().size() - 1, act2.getEdmItem()
        .getNavigationPropertyBindings().size());
  }

  private void createAnnotation() {
    final AnnotationProvider annotationProvider = mock(AnnotationProvider.class);
    final List<CsdlAnnotation> annotations = new ArrayList<>();
    final CsdlAnnotation annotation = mock(CsdlAnnotation.class);
    annotations.add(annotation);
    when(references.convertAlias("Capabilities")).thenReturn("Org.OData.Capabilities.V1");
    when(annotation.getTerm()).thenReturn("Org.OData.Capabilities.V1.CountRestrictions");
    annotationInfo.getAnnotationProvider().add(annotationProvider);
    when(annotationProvider.getAnnotations(eq(Applicability.SINGLETON), any(), any()))
        .thenReturn(annotations);
  }

  private static class PostProcessor implements JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals("com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")
          && property.getInternalName().equals("communicationData")) {
        property.setIgnore(true);
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      // Not needed
    }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {
      // Not needed
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      // Not needed
    }

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
