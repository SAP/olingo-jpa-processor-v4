package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;

public class TestIntermediateSchema extends TestMappingRoot {

  @Test
  public void checkSchemaCanBeCreated() throws ODataJPAModelException {
    new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(Reflections.class));
  }

  @Test
  public void checkSchemaGetAllEntityTypes() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertEquals("Wrong number of entities", TestDataConstants.NO_ENTITY_TYPES, schema.getEdmItem().getEntityTypes()
        .size());
  }

  @Test
  public void checkSchemaGetEntityTypeByNameNotNull() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertNotNull(schema.getEdmItem().getEntityType("BusinessPartner"));
  }

  @Test
  public void checkSchemaGetEntityTypeByNameRightEntity() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertEquals("BusinessPartner", schema.getEdmItem().getEntityType("BusinessPartner").getName());
  }

  @Test
  public void checkSchemaGetAllComplexTypes() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    // ChangeInformation,CommunicationData,AdministrativeInformation,PostalAddressData
    assertEquals("Wrong number of entities", 5, schema.getEdmItem().getComplexTypes().size());
  }

  @Test
  public void checkSchemaGetComplexTypeByNameNotNull() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertNotNull(schema.getEdmItem().getComplexType("CommunicationData"));
  }

  @Test
  public void checkSchemaGetComplexTypeByNameRightEntity() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertEquals("CommunicationData", schema.getEdmItem().getComplexType("CommunicationData").getName());
  }

  @Test
  public void checkSchemaGetAllFunctions() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));
    assertEquals("Wrong number of entities", 6, schema.getEdmItem().getFunctions().size());
  }
}
