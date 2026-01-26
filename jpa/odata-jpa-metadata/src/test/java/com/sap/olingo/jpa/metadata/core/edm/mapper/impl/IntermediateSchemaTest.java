package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.errormodel.MissingCardinalityAnnotation;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.UserType;
import com.sap.olingo.jpa.processor.core.util.TestDataConstants;

class IntermediateSchemaTest extends TestMappingRoot {
  private Reflections reflections;
  private IntermediateAnnotationInformation annotationInfo;

  @BeforeEach
  void setup() {
    reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class,
        AccessRights.class,
        UserType.class)));
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
  }

  @Test
  void checkSchemaCanBeCreated() throws ODataJPAModelException {

    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertNotNull(schema);
  }

  @Test
  void checkSchemaGetAllEntityTypes() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertEquals(TestDataConstants.NO_ENTITY_TYPES.value, schema.getEdmItem().getEntityTypes().size(),
        "Wrong number of entities");
  }

  @Test
  void checkSchemaGetEntityTypeByNameNotNull() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertNotNull(schema.getEdmItem().getEntityType("BusinessPartner"));
  }

  @Test
  void checkSchemaGetEntityTypeByNameRightEntity() throws ODataJPAModelException {
    final EdmEnumType enumType = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(ADMIN_CANONICAL_NAME, ADDR_CANONICAL_NAME);
    when(enumType.getFullQualifiedName()).thenReturn(fqn);
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertEquals("BusinessPartner", schema.getEdmItem().getEntityType("BusinessPartner").getName());
  }

  @Test
  void checkSchemaGetAllComplexTypes() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    // ChangeInformation,CommunicationData,AdministrativeInformation,PostalAddressData
    assertEquals(TestDataConstants.NO_COMPLEX_TYPES.value, schema.getEdmItem().getComplexTypes().size(),
        "Wrong number of complex types");
  }

  @Test
  void checkSchemaGetComplexTypeByNameNotNull() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertNotNull(schema.getEdmItem().getComplexType("CommunicationData"));
  }

  @Test
  void checkSchemaGetComplexTypeByNameRightEntity() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertEquals("CommunicationData", schema.getEdmItem().getComplexType("CommunicationData").getName());
  }

  @Test
  void checkSchemaGetAllFunctions() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    assertEquals(10, schema.getEdmItem().getFunctions().size(), "Wrong number of entities");
  }

  @Test
  void checkSchemaGetEnumerationTypeByType() throws ODataJPAModelException {
    final EdmEnumType type = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "ABCClassification");
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    when(type.getFullQualifiedName()).thenReturn(fqn);
    assertNotNull(schema.getEnumerationType(type));
  }

  @Test
  void checkSchemaGetEnumerationTypeByTypeNotFound() throws ODataJPAModelException {
    final EdmEnumType type = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "Classification");
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);
    when(type.getFullQualifiedName()).thenReturn(fqn);
    assertNull(schema.getEnumerationType(type));
  }

  @Test
  void checkSchemaWrapsException() throws ODataJPAModelException {
    final Metamodel jpaModel = mock(Metamodel.class);
    final EntityType<?> jpaEt = mock(EntityType.class);
    when(jpaModel.getEmbeddables()).thenReturn(Collections.emptySet());
    when(jpaModel.getEntities()).thenReturn(Collections.singleton(jpaEt));
    doReturn(MissingCardinalityAnnotation.class).when(jpaEt).getJavaType();
    when(jpaEt.getDeclaredAttributes()).thenThrow(NullPointerException.class);

    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaModel,
        reflections, annotationInfo, true);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, schema::lazyBuildEdmItem);
    assertTrue(act.getCause() instanceof NullPointerException);
  }

  @Test
  void checkSchemaODataJPAModelExceptionNotWrapped() throws ODataJPAModelException {
    final Metamodel jpaModel = mock(Metamodel.class);
    final EntityType<?> jpaEt = mock(EntityType.class);
    when(jpaModel.getEmbeddables()).thenReturn(Collections.emptySet());
    when(jpaModel.getEntities()).thenReturn(Collections.singleton(errorEmf.getMetamodel().entity(
        MissingCardinalityAnnotation.class)));
    doReturn(MissingCardinalityAnnotation.class).when(jpaEt).getJavaType();

    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaModel,
        reflections, annotationInfo, true);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, schema::lazyBuildEdmItem);
    assertNull(act.getCause());
  }

  @Test
  void checkEmfIsWrapped() throws ODataJPAModelException {
    final Metamodel jpaModel = mock(Metamodel.class);
    final IntermediateSchema cut = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaModel,
        reflections, annotationInfo, false);

    assertFalse(cut.emfIsWrapped());
  }

  @Test
  void checkCopyConstructor() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), reflections, annotationInfo, true);

    final var cut = (IntermediateSchema) schema.asUserGroupRestricted(List.of("Manager"));
    assertTrue(cut.emfIsWrapped());
  }
}
