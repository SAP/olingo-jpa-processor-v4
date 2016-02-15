package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestIntermediateNavigationProperty extends TestMappingRoot {
  private IntermediateSchema schema;
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkNaviProptertyCanBeCreated() {
    EntityType<?> et = helper.getEntityType("BusinessPartner");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME), schema.getEntityType(jpaAttribute),
        jpaAttribute, schema);
  }

  @Test
  public void checkGetName() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertEquals("Wrong name", "Roles", property.getEdmItem().getName());
  }

  @Test
  public void checkGetType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertEquals("Wrong name", PUNIT_NAME + ".BusinessPartnerRole", property.getEdmItem().getType());
  }

  @Test
  public void checkGetIgnoreFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetIgnoreTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"),
        "customString1");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Ignore // How to express in a 1 : n relationship?
  @Test
  public void checkGetProptertyFacetsNullableFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsCollectionTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.getEdmItem().isNullable());
  }

  @Ignore // No valid example --> Roles backwards!
  @Test
  public void checkGetProptertyFacetsColletionFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.getEdmItem().isCollection());
  }

  @Test
  public void checkGetJoinColumnsSize1() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartner");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
  }

  @Test
  public void checkGetJoinColumnsSize2() throws ODataJPAModelException {
    EmbeddableType<?> et = helper.getEmbeddedableType("PostalAddressData");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "regionName");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(helper.getEntityType("BusinessPartner").getJavaType()), jpaAttribute, schema);
    assertEquals(2, property.getJoinColumns().size());
  }

//  @Test
//  public void checkGetJoinColumnsLeft() throws ODataJPAModelException {
//    EntityType<?> et = helper.getEntityType("BusinessPartner");
//
//    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
//    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
//        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
//    assertEquals("ID", property.getJoinColumns().get(0).getLeftPath().getLeaf().getInternalName());
//  }
//
//  @Test
//  public void checkGetJoinColumnsRight() throws ODataJPAModelException {
//    EntityType<?> et = helper.getEntityType("BusinessPartner");
//
//    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
//    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
//        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
//    assertEquals("businessPartnerID", property.getJoinColumns().get(0).getRightPath().getLeaf().getInternalName());
//  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.SetPostProcessor(spy);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        "BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);

    property.getEdmItem();
    assertTrue(spy.called);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.SetPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);

    assertEquals("Wrong name", "RoleAssignment", property.getEdmItem().getName());
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.SetPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(jpaAttribute), jpaAttribute, schema);

    assertEquals("Wrong name", "RoleAssignment", property.getExternalName());
  }

  private class PostProcessorSpy extends JPAEdmMetadataPostProcessor {
    boolean called = false;

    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {
      called = true;
    }

  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          BuPa_CANONICAL_NAME)) {
        if (property.getInternalName().equals("roles")) {
          property.setExternalName("RoleAssignment");
        }
      }
    }
  }
}
