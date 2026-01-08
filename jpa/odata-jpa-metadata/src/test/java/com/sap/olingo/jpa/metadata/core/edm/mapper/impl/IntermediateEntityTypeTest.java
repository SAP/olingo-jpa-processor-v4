package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static com.sap.olingo.jpa.processor.core.util.Assertions.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEtagValidator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.util.AnnotationTestHelper;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.ExampleProperties;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Terms;
import com.sap.olingo.jpa.metadata.odata.v4.general.Aliases;
import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCoreAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientError;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsParent;
import com.sap.olingo.jpa.processor.core.testmodel.AssociationOneToOneSource;
import com.sap.olingo.jpa.processor.core.testmodel.BestOrganization;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUserQueryExtension;
import com.sap.olingo.jpa.processor.core.testmodel.DeepProtectedExample;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.EmptyQueryExtensionProvider;
import com.sap.olingo.jpa.processor.core.testmodel.EntityTypeOnly;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinAccount;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCompoundSub;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCompoundSuper;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCurrentAccount;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinLockedSavingAccount;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinSavingAccount;
import com.sap.olingo.jpa.processor.core.testmodel.JoinRelation;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtected;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtectedHidden;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;
import com.sap.olingo.jpa.processor.core.testmodel.RestrictedEntityUnrestrictedSource;
import com.sap.olingo.jpa.processor.core.testmodel.SalesTeam;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;
import com.sap.olingo.jpa.processor.core.testmodel.TransientRefComplex;
import com.sap.olingo.jpa.processor.core.testmodel.TransientRefIgnore;
import com.sap.olingo.jpa.processor.core.testmodel.UnionMembership;
import com.sap.olingo.jpa.processor.core.util.TestDataConstants;

class IntermediateEntityTypeTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private IntermediateSchema errorSchema;
  private IntermediateAnnotationInformation annotationInfo;
  private IntermediateReferences references;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));
    references = mock(IntermediateReferences.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>(), references);
    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), reflections,
        annotationInfo);
    errorSchema = new IntermediateSchema(new JPADefaultEdmNameBuilder(ERROR_PUNIT), errorEmf.getMetamodel(),
        reflections, annotationInfo);
  }

  @Test
  void checkEntityTypeCanBeCreated() {

    assertNotNull(new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema));
  }

  @Test
  void checkEntityTypeIgnoreSet() throws ODataJPAModelException {

    final IntermediateStructuredType<DummyToBeIgnored> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(DummyToBeIgnored.class), schema);
    et.getEdmItem();
    assertTrue(et.ignore());
  }

  @Test
  void checkGetAllProperties() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals(TestDataConstants.NO_DEC_ATTRIBUTES_BUSINESS_PARTNER.value, et.getEdmItem()
        .getProperties()
        .size(), "Wrong number of entities");
  }

  @Test
  void checkGetPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertNotNull(et.getEdmItem().getProperty("Type"));
  }

  @Test
  void checkGetPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("Type", et.getEdmItem().getProperty("Type").getName());
  }

  @Test
  void checkGetPropertyByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("ID", et.getEdmItem().getProperty("ID").getName());
  }

  @Test
  void checkGetPathByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("ID", et.getPath("ID").getLeaf().getExternalName());
  }

  @Test
  void checkGetPathByNameIgnore() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertNull(et.getPath("CustomString2"));
  }

  @Test
  void checkGetDescriptionPropertyByName() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertNotNull(et.getAttribute("locationName"));
    assertNotNull(et.getBaseType().getEdmItem().getProperty("LocationName"));
  }

  @Test
  void checkGetDescriptionPropertyFromComplexByName() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertTrue(et.getAttribute("address").isPresent());
    assertTrue(et.getAttribute("address").get().getStructuredType().getAttribute("countryName").isPresent());
    // Trigger cascade of building the edm items
    assertNotNull(et.getEdmItem().getProperty("Address"));
    final IntermediateDescriptionProperty act = (IntermediateDescriptionProperty) et.getAttribute("address").get()
        .getStructuredType().getAttribute("countryName").get();
    assertNotNull(act.getDescriptionAttribute());
    assertNotNull(act.getTargetEntity());
    assertNotNull(act.getLocaleFieldName());
    assertNotNull(act.getFixedValueAssignment());
  }

  @Test
  void checkGetPathByNameIgnoreComplexType() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertNull(et.getPath("Address/RegionCodePublisher"));
  }

  @Test
  void checkGetInheritedAttributeByNameCorrectEntityID() throws ODataJPAModelException {
    final IntermediateStructuredType<Person> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType(Person.class), schema);
    assertEquals("ID", et.getPath("ID").getLeaf().getExternalName());
  }

  @Test
  void checkGetAllNavigationProperties() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals(1, et.getEdmItem().getNavigationProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetNavigationPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertNotNull(et.getEdmItem().getNavigationProperty("Roles"));
  }

  @Test
  void checkGetNavigationPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("Roles", et.getEdmItem().getNavigationProperty("Roles").getName());
  }

  @Test
  void checkGetAssociationOfComplexTypeByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("Address/AdministrativeDivision", et.getAssociationPath("Address/AdministrativeDivision").getAlias());
  }

  @Test
  void checkGetAssociationOfComplexTypeByNameJoinColumns() throws ODataJPAModelException {
    int actCount = 0;
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    for (final JPAOnConditionItem item : et.getAssociationPath("Address/AdministrativeDivision").getJoinColumnsList()) {
      if (item.getLeftPath().getAlias().equals("Address/Region")) {
        assertEquals("DivisionCode", item.getRightPath().getAlias());
        actCount++;
      }
      if (item.getLeftPath().getAlias().equals("Address/RegionCodeID")) {
        assertEquals("CodeID", item.getRightPath().getAlias());
        actCount++;
      }
      if (item.getLeftPath().getAlias().equals("Address/RegionCodePublisher")) {
        assertEquals("CodePublisher", item.getRightPath().getAlias());
        actCount++;
      }
    }
    assertEquals(3, actCount, "Not all join columns found");
  }

  @Test
  void checkGetAssociationOfMappedByWithVirtualProperty() throws ODataJPAModelException {

    final IntermediateStructuredType<AssociationOneToOneSource> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AssociationOneToOneSource.class), schema);
    final JPAAssociationPath act = et.getAssociationPath("DefaultTarget");
    assertEquals(1, act.getJoinColumnsList().size());
    final JPAOnConditionItem actColumn = act.getJoinColumnsList().get(0);
    assertEquals("ID", actColumn.getRightPath().getAlias());
    assertEquals("Defaulttarget_id", actColumn.getLeftPath().getAlias());
  }

  @Test
  void checkGetPropertiesSkipIgnored() throws ODataJPAModelException {
    final PostProcessorSetIgnore postProcessorDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(postProcessorDouble);

    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals(TestDataConstants.NO_DEC_ATTRIBUTES_BUSINESS_PARTNER.value - 1, et.getEdmItem()
        .getProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetIsAbstract() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertTrue(et.getEdmItem().isAbstract());
  }

  @Test
  void checkGetIsNotAbstract() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertFalse(et.getEdmItem().isAbstract());
  }

  @Test
  void checkGetHasBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertEquals(PUNIT_NAME + ".BusinessPartner", et.getEdmItem().getBaseType());
  }

  @Test
  void checkGetKeyProperties() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartnerRole> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartnerRole.class), schema);
    assertEquals(2, et.getEdmItem().getKey().size(), "Wrong number of key properties");

  }

  @Test
  void checkGetAllAttributes() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartnerRole> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(BusinessPartnerRole.class), schema);
    assertEquals(2, et.getPathList().size(), "Wrong number of entities");
  }

  @Test
  void checkGetAllAttributesWithBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    final int exp =
        TestDataConstants.NO_ATTRIBUTES_BUSINESS_PARTNER.value
            + TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS.value
            + TestDataConstants.NO_ATTRIBUTES_COMMUNICATION_DATA.value
            + 2 * TestDataConstants.NO_ATTRIBUTES_CHANGE_INFO.value
            + TestDataConstants.NO_ATTRIBUTES_ORGANIZATION.value;
    assertEquals(exp, et.getPathList().size(), "Wrong number of entities");
  }

  @Test
  void checkGetAllAttributesWithBaseTypeFields() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);

    assertNotNull(et.getPath("Type"));
    assertNotNull(et.getPath("Name1"));
    assertNotNull(et.getPath("Address" + JPAPath.PATH_SEPARATOR + "Region"));
    assertNotNull(et.getPath("AdministrativeInformation" + JPAPath.PATH_SEPARATOR
        + "Created" + JPAPath.PATH_SEPARATOR + "By"));
  }

  @Test
  void checkGetAllAttributeIDWithBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertEquals("ID", et.getPath("ID").getAlias());
  }

  @Test
  void checkGetKeyAttributeFromEmbeddedId() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);

    assertTrue(et.getAttribute("codePublisher").isPresent());
    assertEquals("CodePublisher", et.getAttribute("codePublisher").get().getExternalName());
  }

  @Test
  void checkGetKeyAttributeFromEmbeddedIdFromUriResource() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);

    final UriResourceProperty uriResourceItem = mock(UriResourceProperty.class);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(uriResourceItem.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("CodePublisher");

    final Optional<JPAAttribute> act = et.getAttribute(uriResourceItem);

    assertTrue(act.isPresent());
    assertEquals("CodePublisher", act.get().getExternalName());
  }

  @Test
  void checkGetKeyWithBaseType() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertEquals(1, et.getKey().size());
  }

  @Test
  void checkGetKeyFromMappedSuperclass() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(SalesTeam.class), schema);
    assertEquals(1, et.getKey().size());
  }

  @Test
  void checkEmbeddedIdResolvedProperties() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertEquals(5, et.getEdmItem().getProperties().size());
  }

  @Test
  void checkEmbeddedIdResolvedKey() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertEquals(4, et.getEdmItem().getKey().size());
  }

  @Test
  void checkEmbeddedIdResolvedKeyInternal() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertEquals(4, et.getKey().size());
  }

  @Test
  void checkEmbeddedIdResolvedKeyCorrectOrder() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);

    assertEquals("CodePublisher", et.getEdmItem().getKey().get(0).getName());
    assertEquals("CodeID", et.getEdmItem().getKey().get(1).getName());
    assertEquals("DivisionCode", et.getEdmItem().getKey().get(2).getName());
    assertEquals("Language", et.getEdmItem().getKey().get(3).getName());
  }

  @Test
  void checkCompoundResolvedKeyCorrectOrder() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);

    assertEquals("CodePublisher", et.getKey().get(0).getExternalName());
    assertEquals("CodeID", et.getKey().get(1).getExternalName());
    assertEquals("DivisionCode", et.getKey().get(2).getExternalName());
  }

  @Test
  void checkEmbeddedIdResolvedPath() throws ODataJPAModelException {
    final JPAStructuredType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        AdministrativeDivisionDescription.class), schema);
    assertEquals(5, et.getPathList().size());
  }

  @Test
  void checkEmbeddedIdResolvedPathCodeId() throws ODataJPAModelException {
    final JPAStructuredType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        AdministrativeDivisionDescription.class), schema);
    assertEquals(2, et.getPath("CodeID").getPath().size());
  }

  @Test
  void checkEmbeddedIdResolvedKeyPath() throws ODataJPAModelException {
    final JPAEntityType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        AdministrativeDivisionDescription.class), schema);
    assertEquals(1, et.getKeyPath().size());
  }

  @Test
  void checkEmbeddedIdInheritedWithMappingKeyPath() throws ODataJPAModelException {
    final JPAEntityType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        InheritanceByJoinCompoundSub.class), schema);

    assertEquals(3, et.getKey().size());
    var divisionKey = et.getKey().stream().filter(key -> "DivisionCode".equals(key.getExternalName())).findFirst();
    assertNotNull(divisionKey);
    assertEquals("\"PartCode\"", ((IntermediateSimpleProperty) divisionKey.get()).dbFieldName);

    assertEquals(3, et.getKeyPath().size());
    var divisionCode = et.getKeyPath().stream().filter(path -> "DivisionCode".equals(path.getAlias()))
        .findFirst();
    assertTrue(divisionCode.isPresent());
    assertEquals("\"PartCode\"", divisionCode.get().getDBFieldName());
  }

  @Test
  void checkInheritedWithMappingKeyPath() throws ODataJPAModelException {
    final JPAEntityType et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(
        InheritanceByJoinLockedSavingAccount.class), schema);

    assertEquals(1, et.getKey().size());
    var key = et.getKey().get(0);
    assertNotNull(key);
    assertEquals("\"AccountId\"", ((IntermediateSimpleProperty) key).dbFieldName);

    assertEquals(1, et.getKeyPath().size());
    var keyPath = et.getKeyPath().get(0);
    assertEquals("\"AccountId\"", keyPath.getDBFieldName());
  }

  @Test
  void checkHasStreamNoProperties() throws ODataJPAModelException {
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(PersonImage.class), schema);
    assertEquals(2, et.getEdmItem().getProperties().size());
  }

  @Test
  void checkHasStreamTrue() throws ODataJPAModelException {
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(PersonImage.class), schema);
    assertTrue(et.getEdmItem().hasStream());
  }

  @Test
  void checkHasStreamFalse() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertFalse(et.getEdmItem().hasStream());
  }

  @Test
  void checkHasEtagTrue() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  void checkHasEtagTrueIfInherited() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  void checkHasEtagFalse() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.hasEtag());
  }

  @Test
  void checkIgnoreIfAsEntitySet() {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BestOrganization.class), schema);
    assertTrue(et.ignore());
  }

  @Test
  void checkEtagValidatorNullWithoutEtag() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertNull(et.getEtagValidator());
  }

  @Test
  void checkEtagValidatorIsStrongForLong() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals(JPAEtagValidator.STRONG, et.getEtagValidator());
  }

  @Test
  void checkEtagValidatorIsWeakForTimestamp() throws ODataJPAModelException {
    final IntermediateEntityType<DeepProtectedExample> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(DeepProtectedExample.class), schema);
    assertEquals(JPAEtagValidator.WEAK, et.getEtagValidator());
  }

  @Test
  void checkEtagValidatorIsInherited() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Person.class), schema);
    assertEquals(JPAEtagValidator.STRONG, et.getEtagValidator());
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessorSetIgnore());
    final IntermediateEntityType<PersonImage> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(PersonImage.class), schema);
    final List<CsdlAnnotation> act = et.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Core.AcceptableMediaTypes", act.get(0).getTerm());
  }

  @Test
  void checkGetPropertyByDBFieldName() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertEquals("Type", et.getPropertyByDBField("\"Type\"").getExternalName());
  }

  @Test
  void checkGetPropertyByDBFieldNameFromSuperType() throws ODataJPAModelException {
    final IntermediateStructuredType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertEquals("Type", et.getPropertyByDBField("\"Type\"").getExternalName());
  }

  @Test
  void checkGetPropertyByDBFieldNameFromEmbedded() throws ODataJPAModelException {
    final IntermediateStructuredType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertEquals("CodeID", et.getPropertyByDBField("\"CodeID\"").getExternalName());
  }

  @Test
  void checkAllPathContainsComplexCollection() throws ODataJPAModelException {
    final IntermediateStructuredType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Collection.class), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(13, act.size());
    assertNotNull(et.getPath("Complex/Address"));
    assertTrue(et.getPath("Complex/Address").getLeaf().isCollection());
    final IntermediateCollectionProperty<?> actIntermediate = (IntermediateCollectionProperty<?>) et.getPath(
        "Complex/Address").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals(2, actIntermediate.asAssociation().getPath().size());

    for (final JPAPath p : act) {
      if (p.getPath().size() > 1
          && p.getPath().get(0).getExternalName().equals("Complex")
          && p.getPath().get(1).getExternalName().equals("Address")) {
        assertTrue(p.getPath().get(1) instanceof IntermediateCollectionProperty);
        final IntermediateCollectionProperty<?> actProperty = (IntermediateCollectionProperty<?>) p.getPath().get(1);
        assertNotNull(actProperty.asAssociation());
        assertEquals(et, actProperty.asAssociation().getSourceType());
        break;
      }
    }
  }

  @Test
  void checkAllPathContainsPrimitiveCollection() throws ODataJPAModelException {
    final IntermediateStructuredType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Collection.class), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(13, act.size());
    assertNotNull(et.getPath("Complex/Comment"));
    assertTrue(et.getPath("Complex/Comment").getLeaf().isCollection());
    final IntermediateCollectionProperty<?> actIntermediate = (IntermediateCollectionProperty<?>) et.getPath(
        "Complex/Comment").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals("Complex/Comment", actIntermediate.asAssociation().getAlias());

    for (final JPAPath p : act) {
      if (p.getPath().size() > 1
          && p.getPath().get(0).getExternalName().equals("Complex")
          && p.getPath().get(1).getExternalName().equals("Comment")) {
        assertTrue(p.getPath().get(1) instanceof IntermediateCollectionProperty);
        final IntermediateCollectionProperty<?> actProperty = (IntermediateCollectionProperty<?>) p.getPath().get(1);
        assertNotNull(actProperty.asAssociation());
        assertEquals(et, actProperty.asAssociation().getSourceType());
        break;
      }
    }
  }

  @Test
  void checkAllPathContainsDeepComplexWithPrimitiveCollection() throws ODataJPAModelException {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CollectionDeep.class), schema);
    final List<JPAPath> act = et.getPathList();

    assertEquals(9, act.size());
    assertNotNull(et.getPath("FirstLevel/SecondLevel/Comment"));
    assertTrue(et.getPath("FirstLevel/SecondLevel/Comment").getLeaf().isCollection());
    final IntermediateCollectionProperty<?> actIntermediate = (IntermediateCollectionProperty<?>) et.getPath(
        "FirstLevel/SecondLevel/Comment").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals(3, actIntermediate.asAssociation().getPath().size());
    assertEquals("FirstLevel/SecondLevel/Comment", actIntermediate.asAssociation().getAlias());
  }

  @Test
  void checkAllPathContainsDeepComplexWithComplexCollection() throws ODataJPAModelException {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CollectionDeep.class), schema);

    assertNotNull(et.getPath("FirstLevel/SecondLevel/Address"));
    assertTrue(et.getPath("FirstLevel/SecondLevel/Address").getLeaf().isCollection());
    final IntermediateCollectionProperty<?> actIntermediate = (IntermediateCollectionProperty<?>) et.getPath(
        "FirstLevel/SecondLevel/Address").getLeaf();
    assertTrue(actIntermediate.asAssociation().getSourceType() instanceof JPAEntityType);
    assertEquals("FirstLevel/SecondLevel/Address", actIntermediate.asAssociation().getAlias());
    for (final JPAPath path : et.getPathList()) {
      final String[] pathElements = path.getAlias().split("/");
      assertEquals(pathElements.length, path.getPath().size());
    }
  }

  @Test
  void checkAllPathContainsFieldMappingForInheritance() throws ODataJPAModelException {
    final IntermediateStructuredType<InheritanceByJoinLockedSavingAccount> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(
            PUNIT_NAME), getEntityType(InheritanceByJoinLockedSavingAccount.class), schema);
    final var act = et.getPathList();
    final var key = act.stream().filter(path -> "AccountId".equals(path.getAlias())).findFirst();
    assertTrue(key.isPresent());
    assertEquals("\"AccountId\"", key.get().getDBFieldName());
  }

  @Test
  void checkOneSimpleProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartnerProtected> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(BusinessPartnerProtected.class), schema);

    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("UserName", act.get(0).getAttribute().getExternalName());
    assertEquals("UserId", act.get(0).getClaimName());
  }

  @Test
  void checkOneComplexProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<DeepProtectedExample> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(DeepProtectedExample.class), schema);

    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertEquals(3, act.size());
    assertNotNull(act.get(0).toString());
  }

  @Test
  void checkComplexAndInheritedProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<PersonDeepProtectedHidden> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(PersonDeepProtectedHidden.class), schema);

    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertInherited(act);
    assertComplexAnnotated(act, "Creator", "Created");
    assertComplexAnnotated(act, "Updator", "Updated");
    assertComplexDeep(act);
    assertEquals(4, act.size());
  }

  @Test
  void checkComplexAndInheritedProtectedPropertyPath() throws ODataJPAModelException {
    final IntermediateStructuredType<PersonDeepProtected> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(PersonDeepProtected.class), schema);

    final List<JPAProtectionInfo> act = et.getProtections();
    assertNotNull(act);
    assertComplexAnnotated(act, "Creator", "Created");
    assertComplexAnnotated(act, "Updator", "Updated");
    assertComplexDeep(act);
    assertEquals(3, act.size());

    for (var path : et.getPathList()) {
      if ("ProtectedAdminInfo/Created/By".equals(path.getAlias()))
        assertTrue(path.isPartOfGroups(List.of("Creator")));
    }
  }

  @Test
  void checkEmbeddedIdKeyIsCompound() {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  void checkMultipleKeyIsCompound() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  void checkIdIsNotCompound() {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertFalse(et.hasCompoundKey());
  }

  @Test
  void checkEmbeddedIdKeyIsEmbedded() {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertTrue(et.hasEmbeddedKey());
  }

  @Test
  void checkMultipleKeyIsNotEmbedded() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.hasEmbeddedKey());
  }

  @Test
  void checkIdIsNotEmbedded() {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertFalse(et.hasEmbeddedKey());
  }

  @Test
  void checkEntityWithMappedSuperClassContainsAllProperties() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType(SalesTeam.class), schema);

    assertEquals(4, et.getEdmItem().getProperties().size());
    assertNull(et.getBaseType());
    assertNotNull(et.getPropertyByDBField("\"Name\""));
    assertEquals(1, et.getKey().size());
    final JPAAttribute key = et.getKey().get(0);
    assertEquals("id", key.getInternalName());
    assertEquals(1, et.getKeyPath().size());
    assertEquals(String.class, et.getKeyType());
  }

  @Test
  void checkEntityWithMappedSuperClassContainsAllNavigationProperties() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType(SalesTeam.class), schema);

    assertEquals(1, et.getEdmItem().getNavigationProperties().size());
    assertNull(et.getBaseType());
  }

  @Test
  void checkTransientWithReferenceComplex() throws ODataJPAModelException {
    final IntermediateEntityType<TransientRefComplex> et = new IntermediateEntityType<>(nameBuilder,
        getEntityType(TransientRefComplex.class), schema);
    assertTrue(et.getAttribute("concatenatedName").get().isTransient());
  }

  @Test
  void checkTransientWithReferenceIgnore() throws ODataJPAModelException {
    final IntermediateEntityType<TransientRefIgnore> et = new IntermediateEntityType<>(nameBuilder,
        getEntityType(TransientRefIgnore.class), schema);
    assertTrue(et.getAttribute("concatenatedAddr").get().isTransient());
  }

  @Test
  void checkTransientThrowsExceptionWithReferenceUnknown() {
    final EntityType<TeamWithTransientError> jpaEt = errorEmf.getMetamodel().entity(TeamWithTransientError.class);
    final IntermediateEntityType<TeamWithTransientError> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        ERROR_PUNIT), jpaEt, errorSchema);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, et::getEdmItem);
    assertEquals(PROPERTY_REQUIRED_UNKNOWN.getKey(), act.getId());
  }

  @Test
  void checkEntityWithMappedSuperClassContainsAllTransient() throws ODataJPAModelException {
    final IntermediateEntityType<SalesTeam> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType(SalesTeam.class), schema);

    final JPAAttribute act = et.getProperty("fullName");
    assertNotNull(act);
    assertTrue(act.isTransient());
  }

  @Test
  void checkAsSingletonReturnsTrueIfTypeIsAnnotated() {
    final IntermediateEntityType<Singleton> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEntityType(Singleton.class), schema);
    assertTrue(et.asSingleton());
  }

  @Test
  void checkAsSingletonReturnsFalseIfTypeIsNotAnnotated() {
    final IntermediateEntityType<EntityTypeOnly> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(EntityTypeOnly.class), schema);
    assertFalse(et.asSingleton());
  }

  @Test
  void checkAnnotatedAsEntityType() {
    final IntermediateEntityType<EntityTypeOnly> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(EntityTypeOnly.class), schema);
    assertFalse(et.asSingleton());
    assertFalse(et.asEntitySet());
  }

  @Test
  void checkAsEntitySetWithoutAnnotation() {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertTrue(et.asEntitySet());
  }

  @Test
  void checkAsSingletonOnly() {
    final IntermediateEntityType<CurrentUser> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        ERROR_PUNIT), getEntityType(CurrentUser.class), schema);
    assertTrue(et.asSingleton());
    assertFalse(et.asEntitySet());
    assertTrue(et.asTopLevelOnly());
  }

  @Test
  void checkQueryExtensionProviderNotPresent() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    et.getEdmItem();
    assertFalse(et.getQueryExtension().isPresent());
  }

  @Test
  void checkQueryExtensionProviderPresent() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    et.getEdmItem();
    assertTrue(et.getQueryExtension().isPresent());
    assertEquals(EmptyQueryExtensionProvider.class, et.getQueryExtension().get().getConstructor().getDeclaringClass());
  }

  @Test
  void checkQueryExtensionProviderInherited() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Person.class), schema);
    et.getEdmItem();
    assertTrue(et.getQueryExtension().isPresent());
    assertEquals(EmptyQueryExtensionProvider.class, et.getQueryExtension().get().getConstructor().getDeclaringClass());
  }

  @Test
  void checkQueryExtensionProviderOverride() throws ODataJPAModelException {
    final IntermediateEntityType<CurrentUser> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CurrentUser.class), schema);
    et.getEdmItem();
    assertTrue(et.getQueryExtension().isPresent());
    assertEquals(CurrentUserQueryExtension.class, et.getQueryExtension().get().getConstructor().getDeclaringClass());
  }

  @Test
  void checkAddVirtualProperties() throws ODataJPAModelException {
    final IntermediateEntityType<AssociationOneToOneSource> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AssociationOneToOneSource.class), schema);
    et.getEdmItem();
    assertNotNull(et.getPathByDBField("DEFAULTTARGET_ID"));
  }

  @Test
  void checkGetCorrespondingAssociation() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateEntityType<BusinessPartnerRole> role = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartnerRole.class), schema);

    final JPAAssociationAttribute act = bupa.getCorrespondingAssociation(role, "businessPartner");

    assertEquals(role.getExternalName(), act.getTargetEntity().getExternalName());
    assertEquals(bupa.getExternalName(), act.getStructuredType().getExternalName());
  }

  @Test
  void checkGetAttributeReturnsKnownAttribute() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final Optional<JPAAttribute> act = bupa.getAttribute("eTag");
    assertNotNull(act);
    assertTrue(act.isPresent());
    assertEquals("eTag", act.get().getInternalName());
  }

  @Test
  void checkGetAttributeReturnsEmptyWhenIgnore() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final Optional<JPAAttribute> act = bupa.getAttribute("customString1");
    assertNotNull(act);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetAttributeReturnsEmptyWhenUnknown() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final Optional<JPAAttribute> act = bupa.getAttribute("willi");
    assertNotNull(act);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetAttributeReturnsEmptyWhenIgnoreRespected() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final Optional<JPAAttribute> act = bupa.getAttribute("customString1", true);
    assertNotNull(act);
    assertFalse(act.isPresent());
  }

  @Test
  void checkGetAttributeReturnsIgnoreIfNotRespected() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> bupa = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final Optional<JPAAttribute> act = bupa.getAttribute("customString1", false);
    assertNotNull(act);
    assertTrue(act.isPresent());
    assertEquals("customString1", act.get().getInternalName());
  }

  @Test
  void checkConvertStringToPathWithSimplePath() throws ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("type");
    assertNotNull(act);
    assertEquals("Type", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithComplexPath() throws ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("administrativeInformation/updated/by");
    assertNotNull(act);
    assertEquals("AdministrativeInformation/Updated/By", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithSimpleCollectionPath() throws ODataPathNotFoundException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("comment");
    assertNotNull(act);
    assertEquals("Comment", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithComplexCollectionPath() throws ODataPathNotFoundException {
    final IntermediateEntityType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Collection.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("nested");
    assertNotNull(act);
    assertEquals("Nested", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathThrowsExceptionUnknownPart() {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath("administrativeInformation/test/by"));
  }

  @Test
  void checkConvertStringToPathThrowsExceptionFirstUnknown() {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath(
        "test/updated/by"));
    assertNotNull(act.getMessage());
  }

  @Test
  void checkConvertStringToPathThrowsExceptionPartIsNotComplex() {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CollectionDeep.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath(
        "firstLevel/levelID/number"));
    assertNotNull(act.getMessage());
  }

  @Test
  void checkConvertStringToNavigationPathWithSimplePath() throws ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataNavigationPath act = et.convertStringToNavigationPath("roles");
    assertNotNull(act);
    assertEquals("Roles", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathNavigationWithComplexPath() throws ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataNavigationPath act = et.convertStringToNavigationPath("address/administrativeDivision");
    assertNotNull(act);
    assertEquals("Address/AdministrativeDivision", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathNavigationThrowsExceptionOnMultipleNavigations() {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et
        .convertStringToNavigationPath("parent/children"));
    assertNotNull(act.getMessage());
    assertTrue(act.getMessage().contains("'parent/children'"));
  }

  @Test
  void checkGetJoinColumnsThrowsIfNotExist() {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> et.getJoinColumns("test"));
    assertTrue(act.getMessage().contains("test"));
  }

  @Test
  void checkJavaAnnotationsReturnsExistingOnce() {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final String packageName = Table.class.getPackage().getName();
    final Map<String, Annotation> act = et.javaAnnotations(packageName);

    assertEquals(3, act.size());
    assertNotNull(act.get("Table"));
    assertNotNull(act.get("IdClass"));
    assertNotNull(act.get("Entity"));
  }

  @Test
  void checkJavaAnnotationsReturnsEmptyIfNonPresent() {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final Map<String, Annotation> act = et.javaAnnotations("org.example.test");

    assertEquals(0, act.size());
  }

  @Test
  void checkJavaAnnotation() {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final String annotationName = Table.class.getName();
    final Annotation act = et.javaAnnotation(annotationName);

    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsExistingAnnotation() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final CsdlAnnotation act = et.getAnnotation("Core", "Example");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    assertNull(et.getAnnotation("Capability", "Example"));
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    assertNull(et.getAnnotation("Core", "Filter"));
  }

  @Test
  void checkGetAnnotationValueNavigationProperty() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final var act = et.getAnnotationValue(Aliases.CORE, Terms.EXAMPLE, ExampleProperties.EXTERNAL_VALUE, String.class);
    assertNotNull(act);
    assertEquals("../AnnotationsParent?$filter=Parent eq null", act);
  }

  @Test
  void checkGetAnnotationValueReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    assertNull(et.getAnnotationValue(Aliases.CAPABILITIES.alias(), Terms.EXAMPLE.term(),
        ExampleProperties.EXTERNAL_VALUE.property()));
  }

  @Test
  void checkGetSearchablePathReturnsListWithSearchable() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    final List<JPAPath> act = et.getSearchablePath();
    assertEquals(1, act.size());
    assertEquals("Name", act.get(0).getAlias());
  }

  @Test
  void checkGetSearchablePathReturnsEmptyListWhenNoSearchable() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final List<JPAPath> act = et.getSearchablePath();
    assertTrue(act.isEmpty());
  }

  @Test
  void checkDbEqualsSchemaNull() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.dbEquals("Test", null, "\"AdministrativeDivision\""));
  }

  @Test
  void checkDbEqualsCatalogNull() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.dbEquals(null, "\"OLINGO\"", "\"AdministrativeDivision\""));
  }

  @Test
  void checkDbEqualsCatalogSchemaNull() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.dbEquals(null, null, "\"AdministrativeDivision\""));
  }

  @Test
  void checkDbEqualsCatalogEmpty() {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertTrue(et.dbEquals("", "\"OLINGO\"", "\"AdministrativeDivision\""));
  }

  @Test
  void checkDbEqualsTableFromParent() {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);
    assertTrue(et.dbEquals("", "\"OLINGO\"", "\"BusinessPartner\""), et.getTableName());
  }

  @Test
  void checkDbEqualsTableFromParentWithFullQualifiedName() {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);
    assertTrue(et.dbEquals("", "", "\"OLINGO\".\"BusinessPartner\""), et.getTableName());
  }

  @Test
  void checkGetUserGroupsReturnsEmptyListIfNoGroupGiven() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);

    assertTrue(et.getUserGroups().isEmpty());
  }

  @Test
  void checkGetUserGroupsReturnsGivenGroups() throws ODataJPAModelException {
    final IntermediateEntityType<UnionMembership> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(UnionMembership.class), schema);

    assertEquals(1, et.getUserGroups().size());
    assertEquals("Company", et.getUserGroups().get(0));
  }

  private static Stream<Arguments> entityTypeClassProvider() {
    return Stream.of(
        Arguments.of(Person.class),
        Arguments.of(CurrentUser.class),
        Arguments.of(JoinRelation.class),
        Arguments.of(PersonDeepProtectedHidden.class));
  }

  @ParameterizedTest
  @MethodSource("entityTypeClassProvider")
  <T> void checkAsUserGroupRestrictedCopiesUnrestrictedValues(final Class<T> clazz) throws ODataJPAModelException { // NOSONAR
    final IntermediateEntityType<T> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(clazz), schema);

    final IntermediateEntityType<T> act = et.asUserGroupRestricted(List.of("Company"));

    assertEquals(et.getExternalFQN(), act.getExternalFQN());
    assertEquals(et.asTopLevelOnly(), act.asTopLevelOnly());
    assertEquals(et.asSingleton(), act.asSingleton());
    assertEquals(et.asEntitySet(), act.asEntitySet());
    assertListEquals(et.getCollectionAttributesPath(), act.getCollectionAttributesPath(), JPAPath.class);
    assertListEquals(et.getDeclaredCollectionAttributes(), act.getDeclaredCollectionAttributes(),
        JPACollectionAttribute.class);
    assertListEquals(et.getProtections(), act.getProtections(), JPAProtectionInfo.class);
    assertEquals(et.isAbstract(), act.isAbstract());
    assertEquals(et.getContentType(), act.getContentType());
    assertEquals(et.getContentTypeAttributePath(), act.getContentTypeAttributePath());
    assertEquals(et.getEtagPath(), act.getEtagPath());
    assertEquals(et.getEtagValidator(), act.getEtagValidator());
    assertEquals(et.getKey(), act.getKey());
    assertListEquals(et.getKeyPath(), act.getKeyPath(), JPAPath.class);
    assertEquals(et.getQueryExtension(), act.getQueryExtension());
    assertEquals(et.getSearchablePath(), act.getSearchablePath());
    assertEquals(et.getStreamAttributePath(), act.getStreamAttributePath());
    assertEquals(et.getTableName(), act.getTableName());

    assertEquals(et.getUserGroups(), act.getUserGroups());
    assertEquals(et.hasEtag(), act.hasEtag());
    assertEquals(et.hasCompoundKey(), act.hasCompoundKey());
    assertEquals(et.hasEmbeddedKey(), act.hasEmbeddedKey());
    assertEquals(et.hasStream(), act.hasStream());
    assertEquals(et.ignore(), act.ignore());
  }

  @Test
  void checkAsUserGroupRestrictedUserOwnBaseType() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);

    final IntermediateEntityType<Person> act = et.asUserGroupRestricted(List.of("Company"));
    assertNotEquals(et.getBaseType(), act.getBaseType());
  }

  @Test
  void checkAsUserGroupRestrictedUserRestrictsNavigations() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);

    final IntermediateEntityType<Person> act = et.asUserGroupRestricted(List.of("Company"));
    assertListEquals(et.getDeclaredAssociations(), act.getDeclaredAssociations(), JPAAssociationAttribute.class);
    assertListEquals(et.getAssociationPathList(), act.getAssociationPathList(), JPAAssociationPath.class);
    assertListEquals(et.getEdmItem().getNavigationProperties(), act.getEdmItem().getNavigationProperties(),
        CsdlNavigationProperty.class);
    assertAttributesEquals(et.getAttributes(), act.getAttributes());

    final IntermediateEntityType<Person> act2 = et.asUserGroupRestricted(List.of("Person"));
    assertEquals(et.getDeclaredAssociations().size() - 1, act2.getDeclaredAssociations().size());
    assertEquals(et.getAssociationPathList().size() - 1, act2.getAssociationPathList().size());
    assertEquals(et.getEdmItem().getNavigationProperties().size() - 1, act2.getEdmItem().getNavigationProperties()
        .size());
    assertEquals(et.getAttributes().size(), act.getAttributes().size());
    assertEquals(et.getDeclaredAttributes().size(), act.getDeclaredAttributes().size());

  }

  @Test
  void checkGetBaseTypeForSingleTableInheritance() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);

    assertEquals(schema.getEntityType(BusinessPartner.class), et.getBaseType());
    assertEquals(JPAInheritanceType.SINGLE_TABLE, et.getInheritanceInformation().getInheritanceType());
    assertTrue(et.getInheritanceInformation().getJoinColumnsList().isEmpty());
  }

  @Test
  void checkGetBaseTypeForJoinTableInheritance() throws ODataJPAModelException {
    final IntermediateEntityType<InheritanceByJoinCurrentAccount> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(InheritanceByJoinCurrentAccount.class), schema);

    assertEquals(schema.getEntityType(InheritanceByJoinAccount.class), et.getBaseType());
    assertEquals(JPAInheritanceType.JOIN_TABLE, et.getInheritanceInformation().getInheritanceType());
    var joinColumns = et.getInheritanceInformation().getJoinColumnsList();
    assertEquals(1, joinColumns.size());
    assertEquals("AccountId", joinColumns.get(0).getLeftPath().getAlias());
    assertEquals("AccountId", joinColumns.get(0).getRightPath().getAlias());
    var reversedJoinColumns = et.getInheritanceInformation().getReversedJoinColumnsList();
    assertEquals(1, joinColumns.size());
    assertEquals("AccountId", reversedJoinColumns.get(0).getLeftPath().getAlias());
    assertEquals("AccountId", reversedJoinColumns.get(0).getRightPath().getAlias());
  }

  @Test
  void checkGetBaseTypeForJoinTableInheritanceTwoLevel() throws ODataJPAModelException {
    final IntermediateEntityType<InheritanceByJoinLockedSavingAccount> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(InheritanceByJoinLockedSavingAccount.class), schema);

    assertEquals(schema.getEntityType(InheritanceByJoinSavingAccount.class), et.getBaseType());
    assertEquals(JPAInheritanceType.JOIN_TABLE, et.getInheritanceInformation().getInheritanceType());
    var joinColumns = et.getInheritanceInformation().getJoinColumnsList();
    assertEquals(1, joinColumns.size());
    assertEquals("AccountId", joinColumns.get(0).getLeftPath().getAlias());
    assertEquals("\"AccountId\"", joinColumns.get(0).getLeftPath().getDBFieldName());
    assertEquals("AccountId", joinColumns.get(0).getRightPath().getAlias());
    assertEquals("\"ID\"", joinColumns.get(0).getRightPath().getDBFieldName());

    var reversedJoinColumns = et.getInheritanceInformation().getReversedJoinColumnsList();
    assertEquals(1, joinColumns.size());
    assertEquals("AccountId", reversedJoinColumns.get(0).getRightPath().getAlias());
    assertEquals("\"AccountId\"", reversedJoinColumns.get(0).getRightPath().getDBFieldName());
    assertEquals("AccountId", reversedJoinColumns.get(0).getLeftPath().getAlias());
    assertEquals("\"ID\"", reversedJoinColumns.get(0).getLeftPath().getDBFieldName());
  }

  @Test
  void checkGetBaseTypeForJoinTableWithCompoundKey() throws ODataJPAModelException {
    final IntermediateEntityType<InheritanceByJoinCompoundSub> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(InheritanceByJoinCompoundSub.class), schema);

    assertEquals(schema.getEntityType(InheritanceByJoinCompoundSuper.class), et.getBaseType());
    assertEquals(JPAInheritanceType.JOIN_TABLE, et.getInheritanceInformation().getInheritanceType());
    var joinColumns = et.getInheritanceInformation().getJoinColumnsList();
    assertEquals(3, joinColumns.size());
    var codePublisher = joinColumns.stream().filter(column -> "CodePublisher".equals(column.getRightPath()
        .getAlias())).findFirst();
    assertTrue(codePublisher.isPresent());
    assertEquals("\"CodePublisher\"", codePublisher.get().getLeftPath().getDBFieldName());

    var codeID = joinColumns.stream().filter(column -> "CodeID".equals(column.getRightPath()
        .getAlias())).findFirst();
    assertTrue(codeID.isPresent());
    assertEquals("\"CodeID\"", codeID.get().getLeftPath().getDBFieldName());

    var divisionCode = joinColumns.stream().filter(column -> "DivisionCode".equals(column.getRightPath()
        .getAlias())).findFirst();
    assertTrue(divisionCode.isPresent());
    assertEquals("\"PartCode\"", divisionCode.get().getLeftPath().getDBFieldName());

    var reversedJoinColumns = et.getInheritanceInformation().getReversedJoinColumnsList();
    var partCode = reversedJoinColumns.stream().filter(column -> "\"PartCode\"".equals(column.getRightPath()
        .getDBFieldName())).findFirst();
    assertTrue(partCode.isPresent());
    assertEquals("\"DivisionCode\"", partCode.get().getLeftPath().getDBFieldName());
  }

  @Test
  void checkGetAttributesForJoinTableInheritanceTwoLevel() throws ODataJPAModelException {
    final IntermediateEntityType<InheritanceByJoinLockedSavingAccount> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(InheritanceByJoinLockedSavingAccount.class), schema);

    final var act = et.getAttributes();

    assertEquals(6, act.size());
    var key = act.stream().filter(attribute -> "accountId".equals(attribute.getInternalName())).findFirst();
    assertEquals("\"AccountId\"", ((IntermediateProperty) key.get()).getDBFieldName());
  }

  @Test
  void checkGetBaseTypeWithoutInheritance() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);

    assertNull(et.getBaseType());
    assertEquals(JPAInheritanceType.NON, et.getInheritanceInformation().getInheritanceType());
  }

  private static void assertAttributesEquals(final List<JPAAttribute> expList, final List<JPAAttribute> actList)
      throws ODataJPAModelException {
    for (final var exp : expList) {
      for (final var act : actList) {
        if (exp.getInternalName().equals(act.getInternalName())) {
          assertEquals(exp.getInternalName(), act.getInternalName());
          assertEquals(exp.getExternalName(), act.getExternalName());
          assertEquals(exp.getExternalFQN(), act.getExternalFQN());
          assertEquals(((IntermediateModelElement) exp).getAnnotationInformation(), ((IntermediateModelElement) act)
              .getAnnotationInformation());
          assertEquals(exp.getCalculatorConstructor(), act.getCalculatorConstructor());
          assertEquals(exp.getConverter(), act.getConverter());
          assertEquals(exp.getType(), act.getType());
          assertEquals(exp.getDbType(), act.getDbType());
          assertEquals(exp.getJavaType(), act.getJavaType());
          assertEquals(exp.getProtectionClaimNames(), act.getProtectionClaimNames());
          assertEquals(exp.getRawConverter(), act.getRawConverter());
          assertEquals(exp.getRequiredProperties(), act.getRequiredProperties());
          assertEquals(exp.hasProtection(), act.hasProtection());
          assertEquals(exp.isAssociation(), act.isAssociation());
          assertEquals(exp.isCollection(), act.isCollection());
          assertEquals(exp.isComplex(), act.isComplex());
          assertEquals(exp.isEnum(), act.isEnum());
          assertEquals(exp.isEtag(), act.isEtag());
          assertEquals(exp.isKey(), act.isKey());
          assertEquals(exp.isSearchable(), act.isSearchable());
          assertEquals(exp.isTransient(), act.isTransient());
          assertComplexEquals(exp.getStructuredType(), exp.getStructuredType());
          assertEquals(exp.getProperty(), act.getProperty(), exp.getInternalName());
        }
      }
    }

  }

  private static void assertComplexEquals(final JPAStructuredType exp, final JPAStructuredType act)
      throws ODataJPAModelException {
    if (exp != act) {
      assertEquals(exp.getExternalName(), exp.getExternalName());
      assertEquals(exp.getExternalFQN(), exp.getExternalFQN());
      assertEquals(exp.getInternalName(), exp.getInternalName());
      assertEquals(exp.getBaseType(), exp.getBaseType());
      assertEquals(exp.getTypeClass(), exp.getTypeClass());
      assertEquals(exp.getProtections(), exp.getProtections());

      assertAttributesEquals(exp.getAttributes(), exp.getAttributes());

    }
  }

  @Test
  void checkAsUserGroupRestrictedCopiesComplex() throws ODataJPAModelException {
    final IntermediateEntityType<RestrictedEntityUnrestrictedSource> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(RestrictedEntityUnrestrictedSource.class), schema);

    final IntermediateEntityType<RestrictedEntityUnrestrictedSource> act = et.asUserGroupRestricted(List.of("Company"));
    assertNotEquals(et.getProperty("relation"), act.getProperty("relation"));
    assertTrue(act.getProperty("relation").isRestricted());

  }

  void assertComplexAnnotated(final List<JPAProtectionInfo> act, final String expClaimName,
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

  private void createAnnotation() {
    final var reference = annotationInfo.getReferences();
    final var annotationProvider = new JavaBasedCoreAnnotationsProvider();
    final List<CsdlProperty> properties = new ArrayList<>();

    properties.add(AnnotationTestHelper.createTermProperty("Description", "Edm.String"));
    properties.add(AnnotationTestHelper.createTermProperty("ExternalValue", "Edm.String"));

    final var terms = AnnotationTestHelper.addTermToCoreReferences(reference, "Example", "ExternalExampleValue",
        properties);

    when(reference.convertAlias("Core")).thenReturn("Org.OData.Core.V1");
    when(reference.getTerms("Core", Applicability.ENTITY_TYPE))
        .thenReturn(Arrays.asList(terms));

    annotationInfo.getAnnotationProvider().add(annotationProvider);
  }

  private void assertInherited(final List<JPAProtectionInfo> act) {
    for (final JPAProtectionInfo info : act) {
      if (info.getAttribute().getExternalName().equals("UserName")) {
        assertEquals("UserId", info.getClaimName());
        assertEquals(1, info.getPath().getPath().size());
        assertEquals("UserName", info.getPath().getAlias());
        return;
      }
    }
    fail("Inherited not found");
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

  private static class PostProcessorSetIgnore implements JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals("com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")
          && property.getInternalName().equals("communicationData")) {
        property.setIgnore(true);
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      // Not needed
    }

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
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      // Not needed
    }
  }
}