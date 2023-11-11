package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
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
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtectedHidden;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;
import com.sap.olingo.jpa.processor.core.testmodel.SalesTeam;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;
import com.sap.olingo.jpa.processor.core.testmodel.TransientRefComplex;
import com.sap.olingo.jpa.processor.core.testmodel.TransientRefIgnore;
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
  void checkEntityTypeCanBeCreated() throws ODataJPAModelException {

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
  void checkHasETagTrue() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  void checkHasETagTrueIfInherited() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertTrue(et.hasEtag());
  }

  @Test
  void checkHasETagFalse() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertFalse(et.hasEtag());
  }

  @Test
  void checkIgnoreIfAsEntitySet() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BestOrganization.class), schema);
    assertTrue(et.ignore());
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
  void checkEmbeddedIdKeyIsCompound() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivisionDescription.class), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  void checkMultipleKeyIsCompound() throws ODataJPAModelException {
    final IntermediateEntityType<AdministrativeDivision> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    assertTrue(et.hasCompoundKey());
  }

  @Test
  void checkIdIsNotCompound() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertFalse(et.hasCompoundKey());
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
  void checkTransientThrowsExceptionWithReferenceUnknown() throws ODataJPAModelException {
    final EntityType<TeamWithTransientError> jpaEt = errorEmf.getMetamodel().entity(TeamWithTransientError.class);
    final IntermediateEntityType<TeamWithTransientError> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        ERROR_PUNIT), jpaEt, errorSchema);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> et.getAttribute("fullName"));
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
  void checkAsSingletonReturnsTrueIfTypeIsAnnotated() throws ODataJPAModelException {
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
  void checkAsEntitySetWithoutAnnotation() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    assertTrue(et.asEntitySet());
  }

  @Test
  void checkAsSingletonOnly() throws ODataJPAModelException {
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
    final Optional<JPAAttribute> act = bupa.getAttribute("customString1");
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

  @Test
  void checkConvertStringToPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("type");
    assertNotNull(act);
    assertEquals("Type", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithComplexPath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("administrativeInformation/updated/by");
    assertNotNull(act);
    assertEquals("AdministrativeInformation/Updated/By", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithSimpleCollectionPath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Organization.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("comment");
    assertNotNull(act);
    assertEquals("Comment", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathWithComplexCollectionPath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateEntityType<Collection> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(Collection.class), schema);
    final ODataPropertyPath act = et.convertStringToPath("nested");
    assertNotNull(act);
    assertEquals("Nested", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathThrowsExceptionUnknownPart() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath("administrativeInformation/test/by"));
  }

  @Test
  void checkConvertStringToPathThrowsExceptionFirstUnknown() throws ODataJPAModelException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath(
        "test/updated/by"));
    assertNotNull(act.getMessage());
  }

  @Test
  void checkConvertStringToPathThrowsExceptionPartIsNotComplex() throws ODataJPAModelException {
    final IntermediateStructuredType<CollectionDeep> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(CollectionDeep.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et.convertStringToPath(
        "firstLevel/levelID/number"));
    assertNotNull(act.getMessage());
  }

  @Test
  void checkConvertStringToNavigationPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataNavigationPath act = et.convertStringToNavigationPath("roles");
    assertNotNull(act);
    assertEquals("Roles", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathNavigationWithComplexPath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateStructuredType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final ODataNavigationPath act = et.convertStringToNavigationPath("address/administrativeDivision");
    assertNotNull(act);
    assertEquals("Address/AdministrativeDivision", act.getPathAsString());
  }

  @Test
  void checkConvertStringToPathNavigationThrowsExceptionOnMultipleNavigations() throws ODataJPAModelException {
    final IntermediateStructuredType<AdministrativeDivision> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(AdministrativeDivision.class), schema);
    final ODataPathNotFoundException act = assertThrows(ODataPathNotFoundException.class, () -> et
        .convertStringToNavigationPath("parent/children"));
    assertNotNull(act.getMessage());
    assertTrue(act.getMessage().contains("'parent/children'"));
  }

  @Test
  void checkGetJoinColumnsThrowsIfNotExist() throws ODataJPAModelException {
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
    final CsdlAnnotation act = et.getAnnotation("Capabilities", "FilterRestrictions");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    assertNull(et.getAnnotation("Capability", "FilterRestrictions"));
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    assertNull(et.getAnnotation("Capabilities", "Filter"));
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
    final AnnotationProvider annotationProvider = mock(AnnotationProvider.class);
    final List<CsdlAnnotation> annotations = new ArrayList<>();
    final CsdlAnnotation annotation = mock(CsdlAnnotation.class);
    annotations.add(annotation);
    when(references.convertAlias("Capabilities")).thenReturn("Org.OData.Capabilities.V1");
    when(annotation.getTerm()).thenReturn("Org.OData.Capabilities.V1.FilterRestrictions");
    annotationInfo.getAnnotationProvider().add(annotationProvider);
    when(annotationProvider.getAnnotations(eq(Applicability.ENTITY_TYPE), any(), any()))
        .thenReturn(annotations);
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

  private static class PostProcessorSetIgnore implements JPAEdmMetadataPostProcessor {

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