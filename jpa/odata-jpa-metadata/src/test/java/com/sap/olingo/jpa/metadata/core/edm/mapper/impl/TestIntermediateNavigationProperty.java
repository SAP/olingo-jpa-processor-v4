package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassifiaction;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.JoinSource;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestIntermediateNavigationProperty extends TestMappingRoot {
  private IntermediateSchema schema;
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(new Class<?>[] {
        ABCClassifiaction.class })));

    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), r);
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
  }

  @Test
  public void checkNaviProptertyCanBeCreated() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty act = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertNotNull(act);
  }

  @Test
  public void checkGetName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Roles", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetEdmType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(PUNIT_NAME + ".BusinessPartnerRole", property.getEdmItem().getType(), "Wrong name");
  }

  @Test
  public void checkGetIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetIgnoreTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(DummyToBeIgnored.class),
        "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetPropertyOnDelete() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.Cascade, property.getEdmItem().getOnDelete().getAction());
  }

  @Test
  public void checkGetProptertyFacetsNullableFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsCollectionTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyFacetsCollectionFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isCollection());
  }

  @Test
  public void checkGetJoinColumnsSize1BP() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
  }

  @Test
  public void checkGetPartnerAdmin_Parent() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Children", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerAdmin_Children() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "children");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Parent", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerBP_Roles() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("BusinessPartner", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetPartnerRole_BP() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Roles", property.getEdmItem().getPartner());
  }

  @Test
  public void checkGetJoinColumnFilledCompletely() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    final IntermediateJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"BusinessPartnerID\"", act.getName());
    assertEquals("\"ID\"", act.getReferencedColumnName());
  }

  @Test
  public void checkGetJoinColumnFilledCompletelyInvert() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    final IntermediateJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"BusinessPartnerID\"", act.getName());
    assertEquals("\"ID\"", act.getReferencedColumnName());
  }

  @Test
  public void checkGetJoinColumnsSize1Roles() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
  }

  @Test
  public void checkGetJoinColumnsSize2() throws ODataJPAModelException {
    final EmbeddableType<?> et = helper.getEmbeddedableType("PostalAddressData");
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "administrativeDivision");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getComplexType(et.getJavaType()), jpaAttribute, schema);
    final List<IntermediateJoinColumn> columns = property.getJoinColumns();
    assertEquals(3, columns.size());
  }

  @Test
  public void checkGetReferentialConstraintSize() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    assertEquals(1, property.getProperty().getReferentialConstraints().size());
  }

  @Test
  public void checkGetReferentialConstraintBuPaRole() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals("ID", c.getProperty());
      assertEquals("BusinessPartnerID", c.getReferencedProperty());
    }
  }

  @Test
  public void checkGetReferentialConstraintRoleBuPa() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals("BusinessPartnerID", c.getProperty());
      assertEquals("ID", c.getReferencedProperty());
    }
  }

  @Test
  public void checkGetReferentialConstraintViaEmbeddedId() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "allDescriptions");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(AdministrativeDivision.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    assertEquals(3, constraints.size());
    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals(c.getReferencedProperty(), c.getProperty());
    }
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(processor);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        BusinessPartner.class), "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    property.getEdmItem();
    verify(processor, atLeastOnce()).processNavigationProperty(property, BUPA_CANONICAL_NAME);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("RoleAssignment", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final JPAAssociationAttribute property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);

    assertEquals("RoleAssignment", property.getExternalName(), "Wrong name");
  }

  @Test
  public void checkPostProcessorSetOnDelete() throws ODataJPAModelException {
    final PostProcessorOneDelete pPDouble = new PostProcessorOneDelete();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "children");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(AdministrativeDivision.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.None, property.getProperty().getOnDelete().getAction());
  }

  @Test
  public void checkGetJoinTable() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNotNull(property.getJoinTable());
  }

  @Test
  public void checkGetJoinTableName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("\"SupportRelationship\"", property.getJoinTable().getTableName());
  }

  @Test
  public void checkGetNullIfNoJoinTableGiven() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "parent");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNull(property.getJoinTable());
  }

  @Test
  public void checkGetJoinTableJoinColumns() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
  }

  @Test
  public void checkGetJoinTableEntityType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNotNull(property.getJoinTable().getEntityType());
  }

  @Test
  public void checkGetJoinTableJoinColumnsNotMapped() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(JoinSource.class),
        "oneToMany");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(JoinSource.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
    assertNotNull(property.getJoinTable());
    final IntermediateJoinTable act = (IntermediateJoinTable) property.getJoinTable();
    for (final JPAOnConditionItem item : act.getJoinColumns()) {
      assertNotNull(item.getLeftPath());
      assertNotNull(item.getRightPath());
    }
  }

  @Test
  public void checkGetJoinTableJoinColumnsMapped() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Organization.class),
        "supportEngineers");
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
  }

  @Test
  public void checkGetConverterReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getConverter());
  }

  @Test
  public void checkGetEdmTypeReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getEdmType());
  }

  @Test
  public void checkHasProtectionReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.hasProtection());
  }

  @Test
  public void checkIsAssociationReturnsTrue() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.isAssociation());
  }

  @Test
  public void checkIsComplexReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isComplex());
  }

  @Test
  public void checkIsEnumReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isEnum());
  }

  @Test
  public void checkIsKeyReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isKey());
  }

  @Test
  public void checkIsSearchableReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isSearchable());
  }

  @Test
  public void checkGetProtectionPathReturnsEmptyList() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.getProtectionPath("Bla").isEmpty());
  }

  @Test
  public void checkGetProtectionClaimNamesReturnsEmptySet() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.getProtectionClaimNames().isEmpty());
  }

  @Test
  public void checkGetType() throws ODataJPAModelException {
    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertEquals(BusinessPartner.class, property.getType());
  }

//  @Test
//  public void checkSetAnnotations() throws ODataJPAModelException {
//    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(
//        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
//    property.getAnnotations(edmAnnotations, member, internalName, property);
//  }

  private Attribute<?, ?> createDummyAttribute() {
    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final ManagedType<?> mgrType = mock(ManagedType.class);
    final Member member = mock(Member.class, withSettings().extraInterfaces(AnnotatedElement.class));
    when(jpaAttribute.getName()).thenReturn("willi");
    when(jpaAttribute.isCollection()).thenReturn(false);
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return mgrType;
      }
    });
    when(mgrType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    when(jpaAttribute.getJavaMember()).thenReturn(member);
    when(((AnnotatedElement) member).getAnnotation(EdmProtectedBy.class)).thenReturn(null);
    return jpaAttribute;
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          BUPA_CANONICAL_NAME)) {
        if (property.getInternalName().equals("roles")) {
          property.setExternalName("RoleAssignment");
        }
      }
    }

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {

    }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}
  }

  private class PostProcessorOneDelete extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADMIN_CANONICAL_NAME)) {
        if (property.getInternalName().equals("children")) {
          final CsdlOnDelete oD = new CsdlOnDelete();
          oD.setAction(CsdlOnDeleteAction.None);
          property.setOnDelete(oD);
        }
      }
    }

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}
  }
}
