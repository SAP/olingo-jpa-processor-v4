package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateModelElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateNavigationProperty;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateSchema;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;

public class TestIntermediateNavigationProperty extends TestMappingRoot {
  private IntermediateSchema schema;
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkNaviProptertyCanBeCreated() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartner");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME), schema.getStructuredType(jpaAttribute),
        jpaAttribute, schema);
  }

  @Test
  public void checkGetName() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Wrong name", "Roles", property.getEdmItem().getName());
  }

  @Test
  public void checkGetType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Wrong name", PUNIT_NAME + ".BusinessPartnerRole", property.getEdmItem().getType());
  }

  @Test
  public void checkGetIgnoreFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetIgnoreTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("DummyToBeIgnored"),
        "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetPropertyOnDelete() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.Cascade, property.getEdmItem().getOnDelete().getAction());
  }

  @Test
  public void checkGetProptertyFacetsNullableFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartnerRole"),
        "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsCollectionTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsColletionFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartnerRole"),
        "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isCollection());
  }

  @Test
  public void checkGetJoinColumnsSize1BP() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartner");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
  }

  @Test
  public void checkGetPartnerAdmin_Parent() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("AdministrativeDivision");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Children", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerAdmin_Children() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("AdministrativeDivision");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "children");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Parent", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerBP_Roles() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartner");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("BusinessPartner", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerRole_BP() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartnerRole");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Roles", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetJoinColumnFilledCompletely() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartner");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    IntermediateJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"BusinessPartnerID\"", act.getName());
    assertEquals("\"ID\"", act.getReferencedColumnName());
  }

  @Test
  public void checkGetJoinColumnFilledCompletelyInvert() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartnerRole");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    IntermediateJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"BusinessPartnerID\"", act.getName());
    assertEquals("\"ID\"", act.getReferencedColumnName());
  }

  @Test
  public void checkGetJoinColumnsSize1Roles() throws ODataJPAModelException {
    EntityType<?> et = helper.getEntityType("BusinessPartnerRole");

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
  }

  @Test
  public void checkGetJoinColumnsSize2() throws ODataJPAModelException {
    EmbeddableType<?> et = helper.getEmbeddedableType("PostalAddressData");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "administrativeDivision");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getComplexType(et.getJavaType()), jpaAttribute, schema);
    List<IntermediateJoinColumn> columns = property.getJoinColumns();
    assertEquals(3, columns.size());
  }

  @Test
  public void checkGetReferentialConstraintSize() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    assertEquals(1, property.getProperty().getReferentialConstraints().size());
  }

  @Test
  public void checkGetReferentialConstraintBuPaRole() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (CsdlReferentialConstraint c : constraints) {
      assertEquals("ID", c.getProperty());
      assertEquals("BusinessPartnerID", c.getReferencedProperty());
    }
  }

  @Test
  public void checkGetReferentialConstraintRoleBuPa() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartnerRole"),
        "businessPartner");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);
    List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (CsdlReferentialConstraint c : constraints) {
      assertEquals("BusinessPartnerID", c.getProperty());
      assertEquals("ID", c.getReferencedProperty());
    }
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.setPostProcessor(spy);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        "BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    property.getEdmItem();
    assertTrue(spy.called);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Wrong name", "RoleAssignment", property.getEdmItem().getName());
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("BusinessPartner"), "roles");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);

    assertEquals("Wrong name", "RoleAssignment", property.getExternalName());
  }

  @Test
  public void checkPostProcessorSetOnDelete() throws ODataJPAModelException {
    PostProcessorOneDelete pPDouble = new PostProcessorOneDelete();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType("AdministrativeDivision"),
        "children");
    IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        schema.getEntityType(AdministrativeDivision.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.None, property.getProperty().getOnDelete().getAction());
  }

  private class PostProcessorSpy extends JPAEdmMetadataPostProcessor {
    boolean called = false;

    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {
      called = true;
    }

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {

    }

  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          BUPA_CANONICAL_NAME)) {
        if (property.getInternalName().equals("roles")) {
          property.setExternalName("RoleAssignment");
        }
      }
    }

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {

    }
  }

  private class PostProcessorOneDelete extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADMIN_CANONICAL_NAME)) {
        if (property.getInternalName().equals("children")) {
          CsdlOnDelete oD = new CsdlOnDelete();
          oD.setAction(CsdlOnDeleteAction.None);
          property.setOnDelete(oD);
        }
      }
    }

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {

    }
  }
}
