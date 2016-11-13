package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

public class TestIntermediateProperty extends TestMappingRoot {
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkProptertyCanBeCreated() throws ODataJPAModelException {
    EmbeddableType<?> et = helper.getEmbeddedableType("CommunicationData");
    Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "landlinePhoneNumber");
    new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema);
  }

  @Test
  public void checkGetProptertyName() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong name", "Type", property.getEdmItem().getName());
  }

  @Test
  public void checkGetProptertyDBFieldName() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong name", "\"Type\"", property.getDBFieldName());
  }

  @Test
  public void checkGetProptertyType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong type", EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        property.getEdmItem().getType());
  }

  @Test
  public void checkGetProptertyIsTransentTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("Person"), "formatedName");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.isTransient());
  }

  @Test
  public void checkGetProptertyIsTransentFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("Person"), "lastName");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertFalse(property.isTransient());
  }

  @Test
  public void checkGetProptertyComplexType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "communicationData");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong type", PUNIT_NAME + ".CommunicationData", property.getEdmItem().getType());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediatePropertyAccess property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyIgnoreTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customString1");
    IntermediatePropertyAccess property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customString1");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrueComplex() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEmbeddedableType("PostalAddressData"), "POBox");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsNullableFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "eTag");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyIsETagTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "eTag");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.isEtag());
  }

  @Test
  public void checkGetProptertyIsETagFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertFalse(property.isEtag());
  }

  @Test
  public void checkGetProptertyMaxLength() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "type");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(1), property.getEdmItem().getMaxLength());
  }

  @Test
  public void checkGetProptertyPrecisionDecimal() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customNum1");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(16), property.getEdmItem().getPrecision());
  }

  @Test
  public void checkGetProptertyScaleDecimal() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customNum1");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(5), property.getEdmItem().getScale());
  }

  @Test
  public void checkGetProptertyPrecisionTime() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "creationDateTime");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(3), property.getEdmItem().getPrecision());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.setPostProcessor(spy);
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "creationDateTime");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    property.getEdmItem();
    assertTrue(spy.called);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customString1");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertEquals("Wrong name", "ContactPersonName", property.getEdmItem().getName());
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("BusinessPartner"), "customString1");
    IntermediatePropertyAccess property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertEquals("Wrong name", "ContactPersonName", property.getExternalName());
  }

  @Test
  public void checkConverterGet() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("Person"), "birthDay");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertNotNull(property.getConverter());
  }

  @Test
  public void checkGetProptertyDefaultValue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "regionCodePublisher");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("ISO", property.getEdmItem().getDefaultValue());
  }

  @Test
  public void checkGetPropertyIsStream() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getAttribute(helper.getEntityType("PersonImage"),
        "image");
    IntermediateProperty property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(property.isStream());
  }

  @Ignore
  @Test
  public void checkGetSRID() {
    // Test for spatial data missing
  }

  private class PostProcessorSpy extends JPAEdmMetadataPostProcessor {
    boolean called = false;

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      called = true;
    }

    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {}
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("customString1")) {
          property.setExternalName("ContactPersonName");
        }
      }
    }

    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {}
  }
}
