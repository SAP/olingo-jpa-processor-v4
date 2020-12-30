package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.AddressDeepProtected;
import com.sap.olingo.jpa.processor.core.testmodel.AddressDeepThreeProtections;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.DummyEmbeddedToIgnore;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddressWithProtection;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

class TestIntermediateComplexType extends TestMappingRoot {
  private Set<EmbeddableType<?>> etList;
  private IntermediateSchema schema;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    etList = emf.getMetamodel().getEmbeddables();
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), mock(
        Reflections.class));
  }

  @Test
  void checkComplexTypeCanBeCreated() throws ODataJPAModelException {

    assertNotNull(new IntermediateComplexType<>(nameBuilder, getEmbeddableType("CommunicationData"),
        schema));
  }

  private <T> EmbeddableType<T> getEmbeddableType(final Class<T> type) {
    return getEmbeddableType(type.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private <T> EmbeddableType<T> getEmbeddableType(final String typeName) {
    for (final EmbeddableType<?> embeddableType : etList) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return (EmbeddableType<T>) embeddableType;
      }
    }
    return null;
  }

  @Test
  void checkGetAllProperties() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertEquals(4, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("LandlinePhoneNumber"));
  }

  @Test
  void checkGetPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertEquals(ct.getEdmItem().getProperty("LandlinePhoneNumber").getName(), "LandlinePhoneNumber");
  }

  @Test
  void checkGetPropertyIsNullable() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    // In case nullable = true, nullable is not past to $metadata, as this is the default
    assertTrue(ct.getEdmItem().getProperty("POBox").isNullable());
  }

  @Test
  void checkGetAllNaviProperties() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertEquals(1, ct.getEdmItem().getNavigationProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetNaviPropertyByNameNotNull() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  void checkGetNaviPropertyByNameRightEntity() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertEquals("AdministrativeDivision", ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  void checkGetPropertiesSkipIgnored() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<CommunicationData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("CommunicationData"), schema);
    assertEquals(3, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetDescriptionPropertyManyToOne() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("CountryName"));
  }

  @Test
  void checkGetDescriptionPropertyManyToMany() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("RegionName"));
  }

  @Test
  void checkDescriptionPropertyType() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    ct.getEdmItem();
    assertTrue(ct.getProperty("countryName") instanceof IntermediateDescriptionProperty);
  }

  @Test
  void checkGetPropertyOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<AdministrativeInformation> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getPath("Created/By"));
  }

  @Test
  void checkGetPropertyDBName() throws ODataJPAModelException {
    final IntermediateComplexType<PostalAddressData> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertEquals("\"Address.PostOfficeBox\"", ct.getPath("POBox").getDBFieldName());
  }

  @Test
  void checkGetPropertyDBNameOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<AdministrativeInformation> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertEquals("\"CreatedBy\"", ct.getPath("Created/By").getDBFieldName());
  }

  @Test
  void checkGetPropertyWithComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<AdministrativeInformation> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getEdmItem().getProperty("Created"));
  }

  @Test
  void checkGetPropertiesWithSameComplexTypeNotEqual() throws ODataJPAModelException {
    final IntermediateComplexType<AdministrativeInformation> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotEquals(ct.getEdmItem().getProperty("Created"), ct.getEdmItem().getProperty("Updated"));
    assertNotEquals(ct.getProperty("created"), ct.getProperty("updated"));
  }

  @Disabled
  @Test
  void checkGetPropertyWithEnumerationType() {

  }

  @Test
  void checkGetPropertyIgnoreTrue() throws ODataJPAModelException {
    final IntermediateComplexType<DummyEmbeddedToIgnore> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("DummyEmbeddedToIgnore"), schema);
    assertTrue(ct.ignore());
  }

  @Test
  void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {
    final IntermediateComplexType<ChangeInformation> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("ChangeInformation"), schema);
    assertFalse(ct.ignore());
  }

  @Test
  void checkOneSimpleProtectedProperty() throws ODataJPAModelException {
    final IntermediateComplexType<InhouseAddressWithProtection> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("InhouseAddressWithProtection"), schema);
    final List<JPAProtectionInfo> act = ct.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Building", act.get(0).getAttribute().getExternalName());
    assertEquals("BuildingNumber", act.get(0).getClaimName());
  }

  @Test
  void checkOneComplexProtectedPropertyDeep() throws ODataJPAModelException {
    final IntermediateComplexType<AddressDeepProtected> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("AddressDeepProtected"), schema);
    final List<JPAProtectionInfo> act = ct.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Building", act.get(0).getAttribute().getExternalName());
    assertEquals("BuildingNumber", act.get(0).getClaimName());
    assertEquals(2, act.get(0).getPath().getPath().size());
    assertEquals(true, act.get(0).supportsWildcards());
  }

  @ParameterizedTest
  @CsvSource({
      "Building, BuildingNumber",
      "Floor, Floor",
      "RoomNumber, RoomNumber"
  })
  void checkOneComplexProtectedPropertyDeepWoWildcards(final String externalName, final String claim)
      throws ODataJPAModelException {

    final IntermediateComplexType<AddressDeepThreeProtections> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AddressDeepThreeProtections"), schema);
    final List<JPAProtectionInfo> act = ct.getProtections();
    assertNotNull(act);
    assertEquals(3, act.size());
    JPAProtectionInfo targetAttribute = null;
    for (final JPAProtectionInfo a : act) {
      if (a.getAttribute().getExternalName().equals(externalName)) {
        targetAttribute = a;
      }
    }
    assertNotNull(targetAttribute);
    assertEquals(claim, targetAttribute.getClaimName());
    assertEquals(2, act.get(0).getPath().getPath().size());
    assertFalse(act.get(0).supportsWildcards());
  }

  @Test
  void checkTransientCollectionProperty() throws ODataJPAModelException {

    final IntermediateComplexType<CollectionFirstLevelComplex> ct = new IntermediateComplexType<>(nameBuilder,
        getEmbeddableType(CollectionFirstLevelComplex.class), schema);

    assertTrue(ct.getAttribute("transientCollection").get().isTransient());
  }

  private class PostProcessorSetIgnore extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(COMM_CANONICAL_NAME)) {
        if (property.getInternalName().equals("landlinePhoneNumber")) {
          property.setIgnore(true);
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADDR_CANONICAL_NAME)) {
        if (property.getInternalName().equals("countryName")) {
          property.setIgnore(false);
        }
      }
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}
  }

}
