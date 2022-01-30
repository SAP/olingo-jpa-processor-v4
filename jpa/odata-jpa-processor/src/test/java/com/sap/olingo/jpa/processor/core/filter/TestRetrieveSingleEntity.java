package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestRetrieveSingleEntity extends TestBase {

  @Test
   void testRetrieveWithOneKey() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("3", org.get("ID").asText());
  }

  @Test
  void testRetrieveWithTwoKeys() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='1',RoleCategory='A')");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("1", org.get("BusinessPartnerID").asText());
    assertEquals("A", org.get("RoleCategory").asText());
  }

  @Test
void testRetrieveWithEmbeddedKey() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions(DivisionCode='BE1',CodeID='NUTS1',CodePublisher='Eurostat',Language='en')");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("en", org.get("Language").asText());
    assertEquals("NUTS1", org.get("CodeID").asText());
  }
}
