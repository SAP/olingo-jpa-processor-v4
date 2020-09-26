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
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

public class TestIntermediateComplexType extends TestMappingRoot {
  private Set<EmbeddableType<?>> etList;
  private IntermediateSchema schema;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    etList = emf.getMetamodel().getEmbeddables();
    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), mock(
        Reflections.class));

  }

  @Test
  public void checkComplexTypeCanBeCreated() throws ODataJPAModelException {

    new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), getEmbeddedableType("CommunicationData"),
        schema);
  }

  private EmbeddableType<?> getEmbeddedableType(final String typeName) {
    for (final EmbeddableType<?> embeddableType : etList) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return embeddableType;
      }
    }
    return null;
  }

  @Test
  public void checkGetAllProperties() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("CommunicationData"), schema);
    assertEquals(4, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetPropertyByNameNotNull() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("CommunicationData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("LandlinePhoneNumber"));
  }

  @Test
  public void checkGetPropertyByNameCorrectEntity() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("CommunicationData"), schema);
    assertEquals(ct.getEdmItem().getProperty("LandlinePhoneNumber").getName(), "LandlinePhoneNumber");
  }

  @Test
  public void checkGetPropertyIsNullable() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    // In case nullable = true, nullable is not past to $metadata, as this is the default
    assertTrue(ct.getEdmItem().getProperty("POBox").isNullable());
  }

  @Test
  public void checkGetAllNaviProperties() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    assertEquals(1, ct.getEdmItem().getNavigationProperties().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetNaviPropertyByNameNotNull() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  public void checkGetNaviPropertyByNameRightEntity() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    assertEquals("AdministrativeDivision", ct.getEdmItem().getNavigationProperty("AdministrativeDivision").getName());
  }

  @Test
  public void checkGetPropertiesSkipIgnored() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType(
            "CommunicationData"), schema);
    assertEquals(3, ct.getEdmItem().getProperties().size(), "Wrong number of entities");
  }

  @Test
  public void checkGetDescriptionPropertyManyToOne() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType(
            "PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("CountryName"));
  }

  @Test
  public void checkGetDescriptionPropertyManyToMany() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    assertNotNull(ct.getEdmItem().getProperty("RegionName"));
  }

  @Test
  public void checkDescriptionPropertyType() throws ODataJPAModelException {
    final PostProcessorSetIgnore pPDouble = new PostProcessorSetIgnore();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("PostalAddressData"), schema);
    ct.getEdmItem();
    assertTrue(ct.getProperty("countryName") instanceof IntermediateDescriptionProperty);
  }

  @Test
  public void checkGetPropertyOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getPath("Created/By"));
  }

  @Test
  public void checkGetPropertyDBName() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType(
            "PostalAddressData"), schema);
    assertEquals("\"Address.PostOfficeBox\"", ct.getPath("POBox").getDBFieldName());
  }

  @Test
  public void checkGetPropertyDBNameOfNestedComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType(
            "AdministrativeInformation"), schema);
    assertEquals("\"CreatedBy\"", ct.getPath("Created/By").getDBFieldName());
  }

  @Test
  public void checkGetPropertyWithComplexType() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("AdministrativeInformation"), schema);
    assertNotNull(ct.getEdmItem().getProperty("Created"));
  }

  @Test
  public void checkGetPropertiesWithSameComplexTypeNotEqual() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("AdministrativeInformation"), schema);
    assertNotEquals(ct.getEdmItem().getProperty("Created"), ct.getEdmItem().getProperty("Updated"));
    assertNotEquals(ct.getProperty("created"), ct.getProperty("updated"));
  }

  @Disabled
  @Test
  public void checkGetPropertyWithEnumerationType() {

  }

  @Test
  public void checkGetProptertyIgnoreTrue() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("DummyEmbeddedToIgnore"), schema);
    assertTrue(ct.ignore());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("ChangeInformation"), schema);
    assertFalse(ct.ignore());
  }

  @Test
  public void checkOneSimpleProtectedProperty() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("InhouseAddressWithProtection"), schema);
    final List<JPAProtectionInfo> act = ct.getProtections();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Building", act.get(0).getAttribute().getExternalName());
    assertEquals("BuildingNumber", act.get(0).getClaimName());
  }

  @Test
  public void checkOneComplexProtectedPropertyDeep() throws ODataJPAModelException {
    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("AddressDeepProtected"), schema);
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
  public void checkOneComplexProtectedPropertyDeepWoWildcards(final String externalName, final String claim)
      throws ODataJPAModelException {

    final IntermediateComplexType<?> ct = new IntermediateComplexType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        getEmbeddedableType("AddressDeepThreeProtections"), schema);
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
