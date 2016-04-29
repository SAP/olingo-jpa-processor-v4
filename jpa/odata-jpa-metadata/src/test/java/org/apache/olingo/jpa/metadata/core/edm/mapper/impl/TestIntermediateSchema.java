package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.testmodel.TestDataConstants;
import org.junit.Test;

public class TestIntermediateSchema extends TestMappingRoot {

  @Test
  public void checkSchemaCanBeCreated() throws ODataJPAModelException {
    new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
  }

  @Test
  public void checkSchemaGetAllEntityTypes() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertEquals("Wrong number of entities", TestDataConstants.NO_ENTITY_TYPES, schema.getEdmItem().getEntityTypes()
        .size());
  }

  @Test
  public void checkSchemaGetEntityTypeByNameNotNull() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertNotNull(schema.getEdmItem().getEntityType("BusinessPartner"));
  }

  @Test
  public void checkSchemaGetEntityTypeByNameRightEntity() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertEquals("BusinessPartner", schema.getEdmItem().getEntityType("BusinessPartner").getName());
  }

  @Test
  public void checkSchemaGetAllComplexTypes() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    // ChangeInformation,CommunicationData,AdministrativeInformation,PostalAddressData
    assertEquals("Wrong number of entities", 5, schema.getEdmItem().getComplexTypes().size());
  }

  @Test
  public void checkSchemaGetComplexTypeByNameNotNull() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertNotNull(schema.getEdmItem().getComplexType("CommunicationData"));
  }

  @Test
  public void checkSchemaGetComplexTypeByNameRightEntity() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertEquals("CommunicationData", schema.getEdmItem().getComplexType("CommunicationData").getName());
  }

  @Test
  public void checkSchemaGetAllFunctions() throws ODataJPAModelException {
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    assertEquals("Wrong number of entities", 4, schema.getEdmItem().getFunctions().size());
  }
}
