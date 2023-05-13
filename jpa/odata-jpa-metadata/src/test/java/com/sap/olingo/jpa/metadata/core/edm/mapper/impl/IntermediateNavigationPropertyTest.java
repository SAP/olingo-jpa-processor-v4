package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AssociationOneToManyTarget;
import com.sap.olingo.jpa.processor.core.testmodel.AssociationOneToOneSource;
import com.sap.olingo.jpa.processor.core.testmodel.AssociationOneToOneTarget;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.EntityTypeOnly;
import com.sap.olingo.jpa.processor.core.testmodel.JoinComplex;
import com.sap.olingo.jpa.processor.core.testmodel.JoinSource;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

@Tag(value = "Model")
class IntermediateNavigationPropertyTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;
  private IntermediateAnnotationInformation annotationInfo;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    final Reflections reflections = mock(Reflections.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), reflections,
        annotationInfo);
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
  }

  @Test
  void checkNavigationPropertyCanBeCreated() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final JPAAssociationAttribute act = new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertNotNull(act);
  }

  @Test
  void checkGetName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Roles", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  void checkGetNameIfSource() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(
        helper.getEntityType(BusinessPartnerRole.class), "organization");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("Organization", property.getEdmItem().getName(), "Wrong name");
  }

  @Test
  void checkGetEdmType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(PUNIT_NAME + ".BusinessPartnerRole", property.getEdmItem().getType(), "Wrong name");
  }

  @Test
  void checkGetIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<Object> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertFalse(property.ignore());
  }

  @Test
  void checkGetIgnoreTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(DummyToBeIgnored.class),
        "businessPartner");
    final IntermediateNavigationProperty<Object> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getStructuredType(jpaAttribute), jpaAttribute, schema);
    assertTrue(property.ignore());
  }

  @Test
  void checkGetPropertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyOnDelete() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.Cascade, property.getEdmItem().getOnDelete().getAction());
  }

  @Test
  void checkGetPropertyFacetsNullableFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty<BusinessPartnerRole> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyFacetsCollectionTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<BusinessPartner> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertTrue(property.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyFacetsCollectionFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty<BusinessPartnerRole> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isCollection());
  }

  @Test
  void checkGetPropertyFacetsCollectionFalseOnToOne() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "image");
    final IntermediateNavigationProperty<Person> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(Person.class), jpaAttribute, schema);

    assertFalse(property.getEdmItem().isCollection());
  }

  @Test
  void checkGetJoinColumnsSize1OneToManyMapped() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
    final JPAJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"ID\"", act.getName());
    assertEquals("\"BusinessPartnerID\"", act.getReferencedColumnName());
  }

  @Test
  void checkGetJoinColumnsSize1OneToManyNotMapped() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerProtected.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "rolesProtected");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
    final JPAJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"ID\"", act.getName());
    assertEquals("\"BusinessPartnerID\"", act.getReferencedColumnName());
  }

  @Test
  void checkGetPartnerAdmin_Parent() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Children", property.getEdmItem().getPartner());
  }

  @Test
  void checkGetPartnerAdmin_Children() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "children");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Parent", property.getEdmItem().getPartner());
  }

  @Test
  void checkGetPartnerBP_Roles() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("BusinessPartner", property.getEdmItem().getPartner());
  }

  @Test
  void checkGetPartnerRole_BP() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals("Roles", property.getEdmItem().getPartner());
  }

  @Test
  void checkGetJoinColumnFilledCompletely() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartner.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    final JPAJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"ID\"", act.getName());
    assertEquals("\"BusinessPartnerID\"", act.getReferencedColumnName());
  }

  @Test
  void checkGetJoinColumnFilledCompletelyInvert() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    final JPAJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"BusinessPartnerID\"", act.getName());
    assertEquals("\"ID\"", act.getReferencedColumnName());
  }

  @Test
  void checkGetJoinColumnsSize1ManyToOne() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(BusinessPartnerRole.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "businessPartner");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertEquals(1, property.getJoinColumns().size());
    final JPAJoinColumn act = property.getJoinColumns().get(0);
    assertEquals("\"ID\"", act.getReferencedColumnName());
    assertEquals("\"BusinessPartnerID\"", act.getName());
  }

  @Test
  void checkGetJoinColumnsSize2ManyToOne() throws ODataJPAModelException {
    final EmbeddableType<?> et = helper.getEmbeddableType(PostalAddressData.class);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "administrativeDivision");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getComplexType(et.getJavaType()), jpaAttribute, schema);
    final List<? extends JPAJoinColumn> columns = property.getJoinColumns();
    assertEquals(3, columns.size());
  }

  @Test
  void checkGetReferentialConstraintSize() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    assertTrue(property.isMapped());
    assertEquals(1, property.getProperty().getReferentialConstraints().size());
  }

  @Test
  void checkGetReferentialConstraintBuPaRole() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals("ID", c.getProperty());
      assertEquals("BusinessPartnerID", c.getReferencedProperty());
    }
  }

  @Test
  void checkGetReferentialConstraintRoleBuPa() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartnerRole.class),
        "businessPartner");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartnerRole.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals("BusinessPartnerID", c.getProperty());
      assertEquals("ID", c.getReferencedProperty());
    }
  }

  @Test
  void checkGetReferentialConstraintViaEmbeddedId() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "allDescriptions");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(AdministrativeDivision.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    assertEquals(3, constraints.size());
    for (final CsdlReferentialConstraint c : constraints) {
      assertEquals(c.getReferencedProperty(), c.getProperty());
    }
  }

  @Test
  void checkGetReferentialConstraintNotCreatedIfPropertyToBeIgnored() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(EntityTypeOnly.class),
        "generalSettings");
    final IntermediateNavigationProperty<EntityTypeOnly> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(EntityTypeOnly.class), jpaAttribute, schema);
    final List<CsdlReferentialConstraint> constraints = property.getProperty().getReferentialConstraints();

    assertTrue(constraints.isEmpty());
  }

  @Test
  void checkPostProcessorCalled() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(processor);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        BusinessPartner.class), "roles");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    property.getEdmItem();
    verify(processor, atLeastOnce()).processNavigationProperty(property, BUPA_CANONICAL_NAME);
  }

  @Test
  void checkPostProcessorAnnotationAdded() throws ODataJPAModelException {
    final PostProcessorSetName postProcessorDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "roles");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals(1L, cut.getEdmItem().getAnnotations().stream().filter(a -> a.getTerm().equals("Immutable")).count());
  }

  @Test
  void checkPostProcessorSetOnDelete() throws ODataJPAModelException {
    final PostProcessorOneDelete postProcessorDouble = new PostProcessorOneDelete();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "children");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(AdministrativeDivision.class), jpaAttribute, schema);

    assertEquals(CsdlOnDeleteAction.None, property.getProperty().getOnDelete().getAction());
  }

  @Test
  void checkGetJoinTable() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNotNull(property.getJoinTable());
  }

  @Test
  void checkGetJoinTableName() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertEquals("\"OLINGO\".\"SupportRelationship\"", property.getJoinTable().getTableName());
  }

  @Test
  void checkGetNullIfNoJoinTableGiven() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(AdministrativeDivision.class),
        "parent");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNull(property.getJoinTable());
  }

  @Test
  void checkGetJoinTableJoinColumns() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
  }

  @Test
  void checkGetJoinTableEntityType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Person.class),
        "supportedOrganizations");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME),
        schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertNotNull(property.getJoinTable().getEntityType());
  }

  @Test
  void checkGetJoinTableJoinColumnsNotMapped() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(JoinSource.class),
        "oneToMany");
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
    assertNotNull(property.getJoinTable());
    final IntermediateJoinTable act = (IntermediateJoinTable) property.getJoinTable();
    for (final JPAOnConditionItem item : act.getJoinColumns()) {
      assertNotNull(item.getLeftPath());
      assertNotNull(item.getRightPath());
    }
    for (final JPAOnConditionItem item : act.getInverseJoinColumns()) {
      assertNotNull(item.getLeftPath());
      assertNotNull(item.getRightPath());
    }
  }

  @Test
  void checkGetJoinTableJoinColumnsMapped() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(Organization.class),
        "supportEngineers");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(BusinessPartner.class), jpaAttribute, schema);

    assertFalse(property.getJoinColumns().isEmpty());
  }

  @Test
  void checkGetJoinTableJoinColumnsMissingInverse() throws ODataJPAModelException {
    final EmbeddableType<JoinComplex> jpaEmbeddable = helper.getEmbeddableType(JoinComplex.class);
    final IntermediateStructuredType<?> property = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), jpaEmbeddable, schema);
    final JPAAssociationAttribute association = property.getAssociation("oneToManyComplex");
    final JPAAssociationPath path = association.getPath();
    assertNotNull(path.getJoinTable());
    assertEquals(1, path.getJoinTable().getInverseJoinColumns().size());
    assertNotNull(path.getJoinTable().getInverseJoinColumns().get(0).getLeftPath());
    assertNotNull(path.getJoinTable().getInverseJoinColumns().get(0).getRightPath());
    // path.getJoinTable().getJoinColumns()
  }

  @Test
  void checkGetJoinTableJoinViaComplex() throws ODataJPAModelException {
    final EntityType<JoinSource> jpaEntity = helper.getEntityType(JoinSource.class);

    final IntermediateEntityType<JoinSource> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaEntity, schema);
    et.lazyBuildEdmItem();
    final JPAAssociationPath a = et.getAssociationPath("Complex/OneToManyComplex");
    assertNotNull(a.getJoinTable());
    assertNotNull(a.getJoinTable().getJoinColumns());
    assertEquals(1, a.getJoinTable().getJoinColumns().size());
    assertFalse(a.getInverseLeftJoinColumnsList().isEmpty());
    assertEquals(1, a.getJoinTable().getInverseJoinColumns().size());
    assertEquals("\"TargetID\"", a.getJoinTable().getInverseJoinColumns().get(0).getLeftPath().getDBFieldName());
  }

  @Test
  void checkGetConverterReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getConverter());
  }

  @Test
  void checkGetRawConverterReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getRawConverter());
  }

  @Test
  void checkGetDbTypeReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getDbType());
  }

  @Test
  void checkGetCalculatorConstructorReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getCalculatorConstructor());
  }

  @Test
  void checkGetEdmTypeReturnsNull() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertNull(property.getEdmType());
  }

  @Test
  void checkHasProtectionReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.hasProtection());
  }

  @Test
  void checkIsAssociationReturnsTrue() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.isAssociation());
  }

  @Test
  void checkIsComplexReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isComplex());
  }

  @Test
  void checkIsEnumReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isEnum());
  }

  @Test
  void checkIsKeyReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isKey());
  }

  @Test
  void checkIsSearchableReturnsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isSearchable());
  }

  @Test
  void checkGetProtectionPathReturnsEmptyList() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.getProtectionPath("Bla").isEmpty());
  }

  @Test
  void checkGetProtectionClaimNamesReturnsEmptySet() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.getProtectionClaimNames().isEmpty());
  }

  @Test
  void checkGetType() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertEquals(BusinessPartner.class, property.getType());
  }

  @Test
  void checkIsTransientIsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isTransient());
  }

  @Test
  void checkIsEtagIsFalse() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertFalse(property.isEtag());
  }

  @Test
  void checkGetRequiredProperties() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    assertTrue(property.getRequiredProperties().isEmpty());
  }

  @Test
  void checkToString() throws ODataJPAModelException {
    final IntermediateNavigationProperty<JoinSource> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
    final String act = property.toString();
    assertTrue(act.toLowerCase().contains("willi"));
  }

  @Test
  void checkToStringNoPartner() throws ODataJPAModelException {
    final EmbeddableType<ChangeInformation> jpaEmbeddable = helper.getEmbeddableType(ChangeInformation.class);
    final Attribute<?, ?> attribute = jpaEmbeddable.getAttribute("user");
    final IntermediateNavigationProperty<?> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(Collection.class), attribute, schema);

    assertNotNull(property.toString());
  }

//  @Test
//  void checkSetAnnotations() throws ODataJPAModelException {
//    final IntermediateNavigationProperty property = new IntermediateNavigationProperty(new JPAEdmNameBuilder(
//        PUNIT_NAME), schema.getEntityType(JoinSource.class), createDummyAttribute(), schema);
//    property.getAnnotations(edmAnnotations, member, internalName, property);
//  }

  @Test
  void checkIgnoredEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<DummyToBeIgnored> et = schema.getEntityType(DummyToBeIgnored.class);
    final Attribute<?, ?> attribute = helper.getAttribute(helper.getEntityType(DummyToBeIgnored.class),
        "businessPartner");
    final IntermediateNavigationProperty<DummyToBeIgnored> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), et, attribute, schema);
    final List<JPAJoinColumn> act = property.getJoinColumns();
    assertEquals(1, act.size());
  }

  @Test
  void checkOneToOneCreatesVirtualProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<Person> et = schema.getEntityType(Person.class);
    final Attribute<?, ?> attribute = helper.getAttribute(helper.getEntityType(Person.class), "image");
    final IntermediateNavigationProperty<Person> property = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), et, attribute, schema);
    final List<? extends JPAJoinColumn> act = property.getJoinColumns();
    assertEquals(1, act.size());
    assertEquals("\"Image_ID\"", act.get(0).getName());
    assertTrue(property.getRequiredDbColumns().contains("\"Image_ID\""));
  }

  @Test
  void checkOneToOneWithDefaultColumnName() throws ODataJPAModelException {
    final IntermediateStructuredType<AssociationOneToOneSource> et = schema.getEntityType(
        AssociationOneToOneSource.class);
    final Attribute<?, ?> attribute = helper.getAttribute(helper.getEntityType(AssociationOneToOneSource.class),
        "defaultTarget");
    final IntermediateNavigationProperty<AssociationOneToOneSource> property =
        new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), et, attribute, schema);
    final List<? extends JPAJoinColumn> act = property.getJoinColumns();
    assertEquals(1, act.size());
    assertEquals("DEFAULTTARGET_ID", act.get(0).getName());
    assertTrue(property.getRequiredDbColumns().contains("DEFAULTTARGET_ID"));
  }

  @Test
  void checkOneToOneMappedWithDefaultColumnName() throws ODataJPAModelException {
    final IntermediateStructuredType<AssociationOneToOneTarget> et = schema.getEntityType(
        AssociationOneToOneTarget.class);
    final Attribute<?, ?> attribute = helper.getAttribute(helper.getEntityType(AssociationOneToOneTarget.class),
        "defaultSource");
    final IntermediateNavigationProperty<AssociationOneToOneTarget> property =
        new IntermediateNavigationProperty<>(
            new JPADefaultEdmNameBuilder(PUNIT_NAME), et, attribute, schema);
    final List<? extends JPAJoinColumn> act = property.getJoinColumns();
    assertEquals(1, act.size());
    assertEquals("DEFAULTTARGET_ID", act.get(0).getReferencedColumnName());
    assertTrue(property.getRequiredDbColumns().contains("\"ID\""));
  }

  @Test
  void checkManyTooMappedWithDefaultColumnName() throws ODataJPAModelException {
    final IntermediateStructuredType<AssociationOneToManyTarget> et = schema.getEntityType(
        AssociationOneToManyTarget.class);
    final Attribute<?, ?> attribute = helper.getAttribute(helper.getEntityType(AssociationOneToManyTarget.class),
        "defaultSource");
    final IntermediateNavigationProperty<AssociationOneToManyTarget> property =
        new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), et, attribute, schema);
    final List<? extends JPAJoinColumn> act = property.getJoinColumns();
    assertEquals(1, act.size());
    assertEquals("DEFAULTSOURCE_KEY", act.get(0).getName());
    assertEquals("key", act.get(0).getReferencedColumnName());
  }

  @Test
  void checkConsistencyThrowsExceptionIfProtected() throws ODataJPAModelException {
    final EdmProtectedBy jpaProtectedBy = mock(EdmProtectedBy.class);
    final Attribute<?, ?> attribute = createDummyAttribute();
    final Member member = attribute.getJavaMember();
    when(((AnnotatedElement) member).getAnnotation(EdmProtectedBy.class)).thenReturn(jpaProtectedBy);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty<>(
            new JPADefaultEdmNameBuilder(
                PUNIT_NAME), schema.getEntityType(JoinSource.class), attribute, schema));
    assertEquals(MessageKeys.NOT_SUPPORTED_PROTECTED_NAVIGATION.getKey(), act.getId());
  }

  @Test
  void checkConsistencyThrowsExceptionHaveFieldGroup() throws ODataJPAModelException {
    final EdmVisibleFor jpaFieldGroups = mock(EdmVisibleFor.class);
    final Attribute<?, ?> attribute = createDummyAttribute();
    final Member member = attribute.getJavaMember();
    when(((AnnotatedElement) member).getAnnotation(EdmVisibleFor.class)).thenReturn(jpaFieldGroups);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty<>(
            new JPADefaultEdmNameBuilder(
                PUNIT_NAME), schema.getEntityType(JoinSource.class), attribute, schema));
    assertEquals(MessageKeys.NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP.getKey(), act.getId());
  }

  @Test
  void checkGetStructuredTypeCallsLazyBuild() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    assertNotNull(cut.getStructuredType());
  }

  @Test
  void checkGetTargetEntityCallsLazyBuild() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    assertNotNull(cut.getTargetEntity());
  }

  @Test
  void checkGetRequiredDbColumnsCallsLazyBuild() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    assertFalse(cut.getRequiredDbColumns().isEmpty());
  }

  @Test
  void checkGetJavaTypeReturnsNull() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);

    assertNull(cut.getJavaType());
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivision.class);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "parent");
    final IntermediateNavigationProperty<?> cut = new IntermediateNavigationProperty<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), schema.getEntityType(et.getJavaType()), jpaAttribute, schema);
    assertNull(cut.getAnnotation("Capabilities", "Filter"));
  }

  private Attribute<?, ?> createDummyAttribute() {
    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final ManagedType<?> managedType = mock(ManagedType.class);
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
        return managedType;
      }
    });
    when(managedType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
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
      if (jpaManagedTypeClassName.equals(BUPA_CANONICAL_NAME)) {
        if (property.getInternalName().equals("roles")) {
          final CsdlAnnotation annotation = new CsdlAnnotation();
          annotation.setTerm("Immutable");
          annotation.setExpression(new CsdlConstantExpression(ConstantExpressionType.Bool, "true"));
          property.addAnnotations(Collections.singletonList(annotation));
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

  private class PostProcessorOneDelete extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADMIN_CANONICAL_NAME)) {
        if (property.getInternalName().equals("children")) {
          final CsdlOnDelete onDelete = new CsdlOnDelete();
          onDelete.setAction(CsdlOnDeleteAction.None);
          property.setOnDelete(onDelete);
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
