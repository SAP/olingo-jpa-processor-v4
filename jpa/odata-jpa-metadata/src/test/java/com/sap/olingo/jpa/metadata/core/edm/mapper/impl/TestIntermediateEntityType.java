package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientError;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassifiaction;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BestOrganization;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.DeepProtectedExample;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtectedHidden;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;
import com.sap.olingo.jpa.processor.core.testmodel.SalesTeam;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;
import com.sap.olingo.jpa.processor.core.testmodel.TransientRefComplex;

public class TestIntermediateEntityType extends TestMappingRoot {
  private Set<EntityType<?>> etList;
  private IntermediateSchema schema;
  private IntermediateSchema errorSchema;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(new Class<?>[] {
        ABCClassifiaction.class })));

    etList = emf.getMetamodel().getEntities();
    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), r);
    errorSchema = new IntermediateSchema(new JPADefaultEdmNameBuilder(ERROR_PUNIT), errorEmf.getMetamodel(), r);
  }

  @Test
  public void checkEntityTypeCanBeCreated() {

    assertNotNull(new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema));
  }

  @Test
  public void checkEntityTypeIgnoreSet() throws ODataJPAModelException {

    final IntermediateStructuredType<DummyToBeIgnored> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("DummyToBeIgnored"), schema);
    et.getEdmItem();
    assertTrue(et.ignore());
  }

  @Test
  public void checkGetAllProperties() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals(TestDataConstants.NO_DEC_ATTRIBUTES_BUISNESS_PARTNER, et.getEdmItem()
        .getProperties()
        .size(), "Wrong number of entities");
  }

  @Test
  public void checkGetPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertNotNull(et.getEdmItem().getProperty("Type"));
  }

  @Test
  public void checkGetPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("Type", et.getEdmItem().getProperty("Type").getName());
  }

  @Test
  public void checkGetPropertyByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("ID", et.getEdmItem().getProperty("ID").getName());
  }

  @Test
  public void checkGetPathByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("ID", et.getPath("ID").getLeaf().getExternalName());
  }

  @Test
  public void checkGetPathByNameIgnore() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertNull(et.getPath("CustomString2"));
  }

  @Test
  public void checkGetPathByNameIgnoreCompexType() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertNull(et.getPath("Address/RegionCodePublisher"));
  }

  @Test
  public void checkGetInheritedAttributeByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<Person> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType("Person"), schema);
    assertEquals("ID", et.getPath("ID").getLeaf().getExternalName());
  }

  @Test
  public void checkGetAllNaviProperties() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals(1, et.getEdmItem().getNavigationProperties().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetNaviPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertNotNull(et.getEdmItem().getNavigationProperty("Roles"));
  }

  @Test
  public void checkGetNaviPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("Roles", et.getEdmItem().getNavigationProperty("Roles").getName());
  }

  @Test
  public void checkGetAssoziationOfComplexTypeByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("Address/AdministrativeDivision", et.getAssociationPath("Address/AdministrativeDivision").getAlias());
  }

  @Test
  public void checkGetAssoziationOfComplexTypeByNameJoinColumns() throws ODataJPAModelException {
    int actCount = 0;
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        getEntityType("BusinessPartner"), schema);
    for (final JPAOnConditionItem item : et.getAssociationPath("Address/AdministrativeDivision").getJoinColumnsList()) {
      if (item.getLeftPath().getAlias().equals("Address/Region")) {
        assertTrue(item.getRightPath().getAlias().equals("DivisionCode"));
        actCount++;
      }
      if (item.getLeftPath().getAlias().equals("Address/RegionCodeID")) {
        assertTrue(item.getRightPath().getAlias().equals("CodeID"));
        actCount++;
      }
      if (item.getLeftPath().getAlias().equals("Address/RegionCodePublisher")) {
        assertTrue(item.getRightPath().getAlias().equals("CodePublisher"));
        actCount++;
      }
    }
    assertEquals(3, actCount, "Not all join columns found");
  }

  @Test
  public void checkGetPropertiesSkipIgnored() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals(TestDataConstants.NO_DEC_ATTRIBUTES_BUISNESS_PARTNER - 1, et.getEdmItem()
        .getProperties().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetIsAbstract() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertTrue(et.getEdmItem().isAbstract());
  }

  @Test
  public void checkGetIsNotAbstract() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertFalse(et.getEdmItem().isAbstract());
  }

  @Test
  public void checkGetHasBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertEquals(PUNIT_NAME + ".BusinessPartner", et.getEdmItem().getBaseType());
  }

  @Test
  public void checkGetKeyProperties() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartnerRole> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartnerRole"), schema);
    assertEquals(2, et.getEdmItem().getKey().size(), "Wrong number of key propeties");
  }

  @Test
  public void checkGetAllAttributes() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartnerRole> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("BusinessPartnerRole"), schema);
    assertEquals(2, et.getPathList().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetAllAttributesWithBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    final int exp = TestDataConstants.NO_ATTRIBUTES_BUISNESS_PARTNER
        + TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS
        + TestDataConstants.NO_ATTRIBUTES_COMMUNICATION_DATA
        + 2 * TestDataConstants.NO_ATTRIBUTES_CHANGE_INFO
        + TestDataConstants.NO_ATTRIBUTES_ORGANIZATION;
    assertEquals(exp, et.getPathList().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetAllAttributesWithBaseTypeFields() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);

    assertNotNull(et.getPath("Type"));
    assertNotNull(et.getPath("Name1"));
    assertNotNull(et.getPath("Address" + JPAPath.PATH_SEPARATOR + "Region"));
    assertNotNull(et.getPath("AdministrativeInformation" + JPAPath.PATH_SEPARATOR
        + "Created" + JPAPath.PATH_SEPARATOR + "By"));
  }

  @Test
  public void checkGetAllAttributeIDWithBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertEquals("ID", et.getPath("ID").getAlias());
  }

  @Test
  public void checkGetKeyAttributeFromEmbeddedId() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);

    assertTrue(et.getAttribute("codePublisher").isPresent());
    assertEquals("CodePublisher", et.getAttribute("codePublisher").get().getExternalName());
  }

  @Test
  public void checkGetKeyWithBaseType() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertEquals(1, et.getKey().size());
  }

  @Test
  public void checkEmbeddedIdResovedProperties() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertEquals(5, et.getEdmItem().getProperties().size());
  }

  @Test
  public void checkEmbeddedIdResovedKey() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertEquals(4, et.getEdmItem().getKey().size());
  }

  @Test
  public void checkEmbeddedIdResovedKeyInternal() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertEquals(4, et.getKey().size());
  }

  @Test
  public void checkEmbeddedIdResovedKeyCorrectOrder() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertEquals("Language", et.getKey().get(0).getExternalName());
    assertEquals("DivisionCode", et.getKey().get(1).getExternalName());
    assertEquals("CodeID", et.getKey().get(2).getExternalName());
    assertEquals("CodePublisher", et.getKey().get(3).getExternalName());
  }

  @Test
  public void checkCompoundResovedKeyCorrectOrder() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivision"), schema);
    assertEquals("DivisionCode", et.getKey().get(0).getExternalName());
    assertEquals("CodeID", et.getKey().get(1).getExternalName());
    assertEquals("CodePublisher", et.getKey().get(2).getExternalName());
  }

  @Test
  public void checkEmbeddedIdResovedPath() throws ODataJPAModelException {
    final JPAStructuredType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        "AdministrativeDivisionDescription"), schema);
    assertEquals(5, et.getPathList().size());
  }

  @Test
  public void checkEmbeddedIdResovedPathCodeId() throws ODataJPAModelException {
    final JPAStructuredType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        "AdministrativeDivisionDescription"), schema);
    assertEquals(2, et.getPath("CodeID").getPath().size());
  }

  @Test
  public void checkHasStreamNoProperties() throws ODataJPAModelException {
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("PersonImage"), schema);
    assertEquals(2, et.getEdmItem().getProperties().size());
  }

  @Test
  public void checkHasStreamTrue() throws ODataJPAModelException {
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("PersonImage"), schema);
    assertTrue(et.getEdmItem().hasStream());
  }

  @Test
  public void checkHasStreamFalse() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertFalse(et.getEdmItem().hasStream());
  }

  @Test
  public void checkHasETagTrue() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  public void checkHasETagTrueIfInherited() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  public void checkHasETagFalse() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("AdministrativeDivision"), schema);
    assertFalse(et.hasEtag());
  }

  @Test
  public void checkIgnoreIfAsEntitySet() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);
    assertTrue(et.ignore());
  }

  @Test
  public void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessorSetIgnore());
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("PersonImage"), schema);
    final List<CsdlAnnotation> act = et.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Core.AcceptableMediaTypes", act.get(0).getTerm());
  }

  @Test
  public void checkGetProptertyByDBFieldName() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertEquals("Type", et.getPropertyByDBField("\"Type\"").getExternalName());
  }

  @Test
  public void checkGetProptertyByDBFieldNameFromSuperType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);
    assertEquals("Type", et.getPropertyByDBField("\"Type\"").getExternalName());
  }

  @Test
  public void checkGetProptertyByDBFieldNameFromEmbedded() throws ODataJPAModelException {
    final IntermediateStructuredType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertEquals("CodeID", et.getPropertyByDBField("\"CodeID\"").getExternalName());
  }

  @Test
  public void checkAllPathContainsComplexCollcetion() throws ODataJPAModelException {
    final IntermediateStructuredType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Collection"), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(11, act.size());
    assertNotNull(et.getPath("Complex/Address"));
    assertTrue(et.getPath("Complex/Address").getLeaf().isCollection());
    final IntermediateCollectionProperty actIntermediate = (IntermediateCollectionProperty) et.getPath(
        "Complex/Address").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals(2, actIntermediate.asAssociation().getPath().size());

    for (final JPAPath p : act) {
      if (p.getPath().size() > 1
          && p.getPath().get(0).getExternalName().equals("Complex")
          && p.getPath().get(1).getExternalName().equals("Address")) {
        assertTrue(p.getPath().get(1) instanceof IntermediateCollectionProperty);
        final IntermediateCollectionProperty actProperty = (IntermediateCollectionProperty) p.getPath().get(1);
        assertNotNull(actProperty.asAssociation());
        assertEquals(et, actProperty.asAssociation().getSourceType());
        break;
      }
    }
  }

  @Test
  public void checkAllPathContainsPrimitiveCollcetion() throws ODataJPAModelException {
    final IntermediateStructuredType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Collection"), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(11, act.size());
    assertNotNull(et.getPath("Complex/Comment"));
    assertTrue(et.getPath("Complex/Comment").getLeaf().isCollection());
    final IntermediateCollectionProperty actIntermediate = (IntermediateCollectionProperty) et.getPath(
        "Complex/Comment").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals("Complex/Comment", actIntermediate.asAssociation().getAlias());

    for (final JPAPath p : act) {
      if (p.getPath().size() > 1
          && p.getPath().get(0).getExternalName().equals("Complex")
          && p.getPath().get(1).getExternalName().equals("Comment")) {
        assertTrue(p.getPath().get(1) instanceof IntermediateCollectionProperty);
        final IntermediateCollectionProperty actProperty = (IntermediateCollectionProperty) p.getPath().get(1);
        assertNotNull(actProperty.asAssociation());
        assertEquals(et, actProperty.asAssociation().getSourceType());
        break;
      }
    }
  }

  @Test
  public void checkAllPathContainsDeepComplexWithPrimitiveCollcetion() throws ODataJPAModelException {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("CollectionDeep"), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(9, act.size());
    assertNotNull(et.getPath("FirstLevel/SecondLevel/Comment"));
    assertTrue(et.getPath("FirstLevel/SecondLevel/Comment").getLeaf().isCollection());
    final IntermediateCollectionProperty actIntermediate = (IntermediateCollectionProperty) et.getPath(
        "FirstLevel/SecondLevel/Comment").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals(3, actIntermediate.asAssociation().getPath().size());
    assertEquals("FirstLevel/SecondLevel/Comment", actIntermediate.asAssociation().getAlias());
  }

  @Test
  public void checkAllPathContainsDeepComplexWithComplexCollcetion() throws ODataJPAModelException {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("CollectionDeep"), schema);

    assertNotNull(et.getPath("FirstLevel/SecondLevel/Address"));
    assertTrue(et.getPath("FirstLevel/SecondLevel/Address").getLeaf().isCollection());
    final IntermediateCollectionProperty actIntermediate = (IntermediateCollectionProperty) et.getPath(
        "FirstLevel/SecondLevel/Address").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals(3, actIntermediate.asAssociation().getPath().size());
    assertEquals("FirstLevel/SecondLevel/Address", actIntermediate.asAssociation().getAlias());
    for (final JPAPath path : et.getPathList()) {
      final String[] pathElements = path.getAlias().split("/");
      assertEquals(pathElements.length, path.getPath().size());
    }
  }

  @Test
  public void checkOneSimpleProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartnerProtected> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("BusinessPartnerProtected"), schema);
    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Username", act.get(0).getAttribute().getExternalName());
    assertEquals("UserId", act.get(0).getClaimName());
  }

  @Test
  public void checkOneComplexProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<DeepProtectedExample> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("DeepProtectedExample"), schema);
    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertEquals(3, act.size());
    assertNotNull(act.get(0).toString());
  }

  @Test
  public void checkComplexAndInheritedProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<PersonDeepProtectedHidden> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("PersonDeepProtectedHidden"), schema);

    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertInherited(act);
    assertComplexAnnotated(act, "Creator", "Created");
    assertComplexAnnotated(act, "Updator", "Updated");
    assertComplexDeep(act);
    assertEquals(4, act.size());
  }

  @Test
  public void checkEmbeddedIdKeyIsCompound() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType("AdministrativeDivisionDescription"), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  public void checkMultipleKeyIsCompound() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("AdministrativeDivision"), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  public void checkIdIsNotCompound() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BusinessPartner"), schema);
    assertFalse(et.hasCompoundKey());
  }

  @Test
  public void checkEntityWithMappedSuperClassContainsAllProperties() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType("SalesTeam"), schema);

    assertEquals(4, et.getEdmItem().getProperties().size());
    assertNull(et.getBaseType());
    assertNotNull(et.getPropertyByDBField("\"Name\""));
    assertEquals(1, et.getKey().size());
    final JPAAttribute key = et.getKey().get(0);
    assertEquals("iD", key.getInternalName());
    assertEquals(1, et.getKeyPath().size());
    assertEquals(String.class, et.getKeyType());
  }

  @Test
  public void checkEntityWithMappedSuperClassContainsAllNaviProperties() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType("SalesTeam"), schema);

    assertEquals(1, et.getEdmItem().getNavigationProperties().size());
    assertNull(et.getBaseType());
  }

  @Test
  public void checkTransientWithRefComplex() throws ODataJPAModelException {
    final IntermediateEntityType<TransientRefComplex> et = new IntermediateEntityType<>(nameBuilder,
        getEntityType("TransientRefComplex"), schema);
    assertTrue(et.getAttribute("concatenatedAddr").get().isTransient());
  }

  @Test
  public void checkTransientThrowsExceptionWithReferenceUnknown() throws ODataJPAModelException {
    final EntityType<TeamWithTransientError> jpaEt = errorEmf.getMetamodel().entity(TeamWithTransientError.class);
    final IntermediateEntityType<TeamWithTransientError> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        ERROR_PUNIT), jpaEt, errorSchema);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> et.getAttribute("fullName"));
    assertEquals(PROPERTY_REQUIRED_UNKNOWN.getKey(), act.getId());
  }

  @Test
  public void checkEntityWithMappedSuperClassContainsAllTransient() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType("SalesTeam"), schema);
    final JPAAttribute act = et.getProperty("fullName");
    assertNotNull(act);
    assertTrue(act.isTransient());
  }

  private void assertComplexDeep(final List<JPAProtectionInfo> act) {
    for (final JPAProtectionInfo info : act) {
      if (info.getClaimName().equals("BuildingNumber")) {
        assertEquals("Building", info.getAttribute().getExternalName());
        assertEquals(3, info.getPath().getPath().size());
        assertEquals("InhouseAddress/InhouseAddress/Building", info.getPath().getAlias());
        return;
      }
    }
    fail("Deep protected complex attribute not found");

  }

  private void assertComplexAnnotated(final List<JPAProtectionInfo> act, final String expClaimName,
      final String pathElement) {
    for (final JPAProtectionInfo info : act) {
      if (info.getClaimName().equals(expClaimName)) {
        assertEquals("By", info.getAttribute().getExternalName());
        assertEquals(3, info.getPath().getPath().size());
        assertEquals("ProtectedAdminInfo/" + pathElement + "/By", info.getPath().getAlias());
        return;
      }
    }
    fail("Complex attribute not found for: " + expClaimName);
  }

  private void assertInherited(final List<JPAProtectionInfo> act) {
    for (final JPAProtectionInfo info : act) {
      if (info.getAttribute().getExternalName().equals("Username")) {
        assertEquals("UserId", info.getClaimName());
        assertEquals(1, info.getPath().getPath().size());
        assertEquals("Username", info.getPath().getAlias());
        return;
      }
    }
    fail("Inherited not found");
  }

  @SuppressWarnings("unchecked")
  private <T> EntityType<T> getEntityType(final String typeName) {
    for (final EntityType<?> entityType : etList) {
      if (entityType.getJavaType().getSimpleName().equals(typeName)) {
        return (EntityType<T>) entityType;
      }
    }
    return null;
  }

  private static class PostProcessorSetIgnore extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("communicationData")) {
          property.setIgnore(true);
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {
      if (entity.getExternalName().equals("PersonImage")) {
        final List<CsdlExpression> items = new ArrayList<>();
        final CsdlCollection exp = new CsdlCollection();
        exp.setItems(items);
        final CsdlConstantExpression mimeType = new CsdlConstantExpression(ConstantExpressionType.String, "ogg");
        items.add(mimeType);
        final CsdlAnnotation annotation = new CsdlAnnotation();
        annotation.setExpression(exp);
        annotation.setTerm("Core.AcceptableMediaTypes");
        final List<CsdlAnnotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        entity.addAnnotations(annotations);
      }
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}
  }

}
