package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryJSONAnnotations extends TestBase {

  @Test
  void testEntityWithMetadataFullContainNavigationLink() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=full");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Roles@odata.navigationLink"));
    assertEquals("Organizations('3')/Roles", org.get("Roles@odata.navigationLink").asText());
  }

  @Test
  void testEntityWithMetadataMinimalWithoutNavigationLink() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=minimal");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNull(org.get("Roles@odata.navigationLink"));
  }

  @Test
  void testEntityWithMetadataNoneWithoutNavigationLink() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=none");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNull(org.get("Roles@odata.navigationLink"));
  }

  @Test
  void testEntityExpandWithMetadataFullContainNavigationLink() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$expand=Roles&$format=application/json;odata.metadata=full");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Roles@odata.navigationLink"));
    assertEquals("Organizations('3')/Roles", org.get("Roles@odata.navigationLink").asText());
  }

  @Test
  void testEntityWithMetadataFullContainNavigationLinkOfComplex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=full");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ObjectNode admin = (ObjectNode) org.get("AdministrativeInformation");
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertNotNull(created.get("User@odata.navigationLink"));
    assertEquals("Organizations('3')/AdministrativeInformation/Created/User", created.get("User@odata.navigationLink")
        .asText());
  }

}
