package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQuerySingleton extends TestBase {

  @Test
  void testSingletonReturnsNoValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Singleton");
    helper.assertStatus(404);
  }

  @Test
  void testSingletonCount() throws IOException, ODataException {
    // Not supported by OData/Olingo
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Singleton/$count");
    assertEquals(400, helper.getStatus());
  }

  @Test
  void testSingletonReturnsValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "CurrentUser");
    helper.assertStatus(200);
    final ObjectNode act = helper.getValue();

    assertEquals("97", act.get("ID").asText());
  }

}
