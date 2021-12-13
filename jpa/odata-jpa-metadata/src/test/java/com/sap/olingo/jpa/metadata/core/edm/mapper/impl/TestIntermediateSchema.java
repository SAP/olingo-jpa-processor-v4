package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;

class TestIntermediateSchema extends TestMappingRoot {
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
    assertEquals(TestDataConstants.NO_ENTITY_TYPES, schema.getEdmItem().getEntityTypes().size(),
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
}
