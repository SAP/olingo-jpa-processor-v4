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

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertEquals("3", organization.get("ID").asText());
  }

  @Test
  void testRetrieveWithTwoKeys() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='1',RoleCategory='A')");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertEquals("1", organization.get("BusinessPartnerID").asText());
    assertEquals("A", organization.get("RoleCategory").asText());
  }

  @Test
  void testRetrieveWithEmbeddedKey() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions(DivisionCode='BE1',CodeID='NUTS1',CodePublisher='Eurostat',Language='en')");
    helper.assertStatus(200);

    final ObjectNode description = helper.getValue();
    assertEquals("en", description.get("Language").asText());
    assertEquals("NUTS1", description.get("CodeID").asText());
  }
}
