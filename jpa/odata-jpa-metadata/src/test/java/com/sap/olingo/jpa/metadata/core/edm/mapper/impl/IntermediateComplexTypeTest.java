package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.metamodel.EmbeddableType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ComplexSubTypeError;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ComplexSubTypeOfIgnore;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.ComplexBaseType;
import com.sap.olingo.jpa.processor.core.testmodel.ComplexSubType;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

class IntermediateComplexTypeTest extends TestMappingRoot {
  private Set<EmbeddableType<?>> etList;
  private IntermediateSchema schema;
  private IntermediateAnnotationInformation annotationInfo;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    etList = emf.getMetamodel().getEmbeddables();
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), mock(
        Reflections.class), annotationInfo);
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
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertEquals(4, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("LandlinePhoneNumber"));
  }

  @Test
  void checkGetPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddableType("CommunicationData"), schema);
    assertEquals("LandlinePhoneNumber", ct.getEdmItem().getProperty("LandlinePhoneNumber").getName());
  }

  @Test
  void checkGetPropertyIsNullable() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType(PostalAddressData.class), schema);
    // In case nullable = true, nullable is not past to $metadata, as this is the default
    assertTrue(ct.getEdmItem().getProperty("POBox").isNullable());
  }

  @Test
  void checkGetAllNaviProperties() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType(PostalAddressData.class), schema);
    assertEquals(1, ct.getEdmItem().getNavigationProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetNaviPropertyByNameNotNull() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType(PostalAddressData.class), schema);
    assertNotNull(ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  void checkGetNaviPropertyByNameRightEntity() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertEquals("AdministrativeDivision", ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  void checkGetPropertiesSkipIgnored() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("CommunicationData"), schema);
    assertEquals(3, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  void checkGetDescriptionPropertyManyToOne() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("CountryName"));
  }

  @Test
  void checkGetDescriptionPropertyManyToMany() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("RegionName"));
  }

  @Test
  void checkDescriptionPropertyType() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    ct.getEdmItem();
    assertTrue(ct.getProperty("countryName") instanceof IntermediateDescriptionProperty);
  }

  @Test
  void checkGetPropertyOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getPath("Created/By"));
  }

  @Test
  void checkGetPropertyDBName() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("PostalAddressData"), schema);
    assertEquals("\"Address.PostOfficeBox\"", ct.getPath("POBox").getDBFieldName());
  }

  @Test
  void checkGetPropertyDBNameOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertEquals("\"CreatedBy\"", ct.getPath("Created/By").getDBFieldName());
  }

  @Test
  void checkGetPropertyWithComplexType() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getEdmItem().getProperty("Created"));
  }

  @Test
  void checkGetPropertiesWithSameComplexTypeNotEqual() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("AdministrativeInformation"), schema);
    assertNotEquals(ct.getEdmItem().getProperty("Created"), ct.getEdmItem().getProperty("Updated"));
    assertNotEquals(ct.getProperty("created"), ct.getProperty("updated"));
  }

  @Disabled("Enumeration Type Property")
  @Test
  void checkGetPropertyWithEnumerationType() {
    fail();
  }

  @Test
  void checkGetPropertyIgnoreTrue() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("DummyEmbeddedToIgnore"), schema);
    assertTrue(ct.ignore());
  }

  @Test
  void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEmbeddableType("ChangeInformation"), schema);
    assertFalse(ct.ignore());
  }

  @Test
  void checkOneSimpleProtectedProperty() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddableType("InhouseAddressWithProtection"), schema);
    final List<JPAProtectionInfo> act = ct.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Building", act.get(0).getAttribute().getExternalName());
    assertEquals("BuildingNumber", act.get(0).getClaimName());
  }

  @Test
  void checkOneComplexProtectedPropertyDeep() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(
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

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(
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

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(nameBuilder,
        getEmbeddableType(CollectionFirstLevelComplex.class), schema);

    assertTrue(ct.getAttribute("transientCollection").get().isTransient());
  }

  @Test
  void checkGetBaseType() throws ODataJPAModelException {
    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(nameBuilder,
        getEmbeddableType(ComplexSubType.class), schema);

    assertNotNull(ct.getBaseType());
    assertEquals(ComplexBaseType.class.getName(), ct.getBaseType().getInternalName());
  }

  @Test
  void checkGetBaseTypeNotFound() throws ODataJPAModelException {
    @SuppressWarnings("unchecked")
    final EmbeddableType<ComplexSubTypeError> complexType = mock(EmbeddableType.class);

    when(complexType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ComplexSubTypeError.class;
      }
    });

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(nameBuilder,
        complexType, schema);
    assertNull(ct.getBaseType());
  }

  @Test
  void checkGetBaseTypeBaseTypeIgnore() throws ODataJPAModelException {
    @SuppressWarnings("unchecked")
    final EmbeddableType<ComplexSubTypeOfIgnore> complexType = mock(EmbeddableType.class);

    when(complexType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ComplexSubTypeError.class;
      }
    });

    final IntermediateStructuredType<?> ct = new IntermediateComplexType<>(nameBuilder,
        complexType, schema);
    assertNull(ct.getBaseType());
  }

  private class PostProcessorSetIgnore implements JPAEdmMetadataPostProcessor {

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
