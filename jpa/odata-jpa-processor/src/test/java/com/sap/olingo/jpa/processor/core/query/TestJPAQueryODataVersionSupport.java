package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryODataVersionSupport extends TestBase {

  @Test
  void testEntityWithMetadataFullVersion400() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.00");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@odata.context"));
    assertNotNull(org.get("@odata.type"));
  }

  @Test
  void testEntityWithMetadataFullVersion401() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.01");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@context"));
    assertNotNull(org.get("@type"));
  }

  @Test
  void testEntityNavigationWithMetadataFullVersion400() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.00");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Roles?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode act = helper.getValue();
    assertNotNull(act.get("@odata.context"));
    assertNotNull(act.get("value").get(1).get("@odata.type"));
  }

  @Test
  void testEntityNavigationWithMetadataFullVersion401() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.01");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Roles?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode act = helper.getValue();
    assertNotNull(act.get("@context"));
    assertNotNull(act.get("value").get(1).get("@type"));
  }

  @Test
  void testComplexPropertyWithMetadataFullVersion400() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.00");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/CommunicationData?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@odata.context"));
    assertNotNull(org.get("@odata.type"));
  }

  @Test
  void testComplexPropertyWithMetadataFullVersion401() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.01");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/CommunicationData?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@context"));
    assertNotNull(org.get("@type"));
  }

  @Test
  void testPrimitivePropertyWithMetadataFullVersion400() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.00");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/LocationName?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@odata.context"));
  }

  @Test
  void testPrimitivePropertyWithMetadataFullVersion401() throws IOException, ODataException {
    createHeaders();
    addHeader(HttpHeader.ODATA_MAX_VERSION, "4.01");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/LocationName?$format=application/json;odata.metadata=full", headers);
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("@context"));
  }
}
