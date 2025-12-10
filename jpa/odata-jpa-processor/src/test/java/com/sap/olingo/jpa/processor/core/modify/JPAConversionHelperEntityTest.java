package com.sap.olingo.jpa.processor.core.modify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.serializer.SerializerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class JPAConversionHelperEntityTest extends JPAConversionHelperTest {

  @BeforeEach
  void setUp() {
    cut = new JPAConversionHelper();
  }

  @Override
  @Test
  void testConvertSimpleKeyToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {

    final Organization newPOJO = new Organization();
    newPOJO.setID("35");

    prepareConvertSimpleKeyToLocation();
    final String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, newPOJO);
    assertEquals("localhost.test/Organisation('35')", act);
  }

  @Override
  @Test
  void testConvertCompoundKeyToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {

    final BusinessPartnerRole newPOJO = new BusinessPartnerRole();
    newPOJO.setBusinessPartnerID("35");
    newPOJO.setRoleCategory("A");

    prepareConvertCompoundKeyToLocation();
    final String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, newPOJO);
    assertEquals("localhost.test/BusinessPartnerRoles(BusinessPartnerID='35',RoleCategory='A')", act);
  }

  @Override
  @Test
  void testConvertEmbeddedIdToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {

    final AdministrativeDivisionDescription newPOJO = new AdministrativeDivisionDescription();
    final AdministrativeDivisionDescriptionKey primaryKey = new AdministrativeDivisionDescriptionKey();
    primaryKey.setCodeID("NUTS1");
    primaryKey.setCodePublisher("Eurostat");
    primaryKey.setDivisionCode("BE1");
    primaryKey.setLanguage("fr");
    newPOJO.setKey(primaryKey);

    prepareConvertEmbeddedIdToLocation();

    final String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, newPOJO);
    assertEquals(
        "localhost.test/AdministrativeDivisionDescriptions(DivisionCode='BE1',CodeID='NUTS1',CodePublisher='Eurostat',Language='fr')",
        act);

  }
}
