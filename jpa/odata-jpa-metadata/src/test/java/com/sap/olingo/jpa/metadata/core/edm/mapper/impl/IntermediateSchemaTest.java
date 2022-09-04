package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.util.TestDataConstants;

class IntermediateSchemaTest extends TestMappingRoot {
  private Reflections r;

  @BeforeEach
  void setup() {
    r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(ABCClassification.class,
        AccessRights.class)));
  }

  @Test
  void checkSchemaCanBeCreated() throws ODataJPAModelException {

    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertNotNull(schema);
  }

  @Test
  void checkSchemaGetAllEntityTypes() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertEquals(TestDataConstants.NO_ENTITY_TYPES.value, schema.getEdmItem().getEntityTypes().size(),
        "Wrong number of entities");
  }

  @Test
  void checkSchemaGetEntityTypeByNameNotNull() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertNotNull(schema.getEdmItem().getEntityType("BusinessPartner"));
  }

  @Test
  void checkSchemaGetEntityTypeByNameRightEntity() throws ODataJPAModelException {
    final EdmEnumType enumType = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(ADMIN_CANONICAL_NAME, ADDR_CANONICAL_NAME);
    when(enumType.getFullQualifiedName()).thenReturn(fqn);
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertEquals("BusinessPartner", schema.getEdmItem().getEntityType("BusinessPartner").getName());
  }

  @Test
  void checkSchemaGetAllComplexTypes() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    // ChangeInformation,CommunicationData,AdministrativeInformation,PostalAddressData
    assertEquals(23, schema.getEdmItem().getComplexTypes().size(), "Wrong number of complex types");
  }

  @Test
  void checkSchemaGetComplexTypeByNameNotNull() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertNotNull(schema.getEdmItem().getComplexType("CommunicationData"));
  }

  @Test
  void checkSchemaGetComplexTypeByNameRightEntity() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertEquals("CommunicationData", schema.getEdmItem().getComplexType("CommunicationData").getName());
  }

  @Test
  void checkSchemaGetAllFunctions() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    assertEquals(10, schema.getEdmItem().getFunctions().size(), "Wrong number of entities");
  }

  @Test
  void checkSchemaGetEnumerationTypeByType() throws ODataJPAModelException {
    final EdmEnumType type = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "ABCClassification");
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    when(type.getFullQualifiedName()).thenReturn(fqn);
    assertNotNull(schema.getEnumerationType(type));
  }

  @Test
  void checkSchemaGetEnumerationTypeByTypeNotFound() throws ODataJPAModelException {
    final EdmEnumType type = mock(EdmEnumType.class);
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "Classification");
    final IntermediateSchema schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf
        .getMetamodel(), r);
    when(type.getFullQualifiedName()).thenReturn(fqn);
    assertNull(schema.getEnumerationType(type));
  }
}
