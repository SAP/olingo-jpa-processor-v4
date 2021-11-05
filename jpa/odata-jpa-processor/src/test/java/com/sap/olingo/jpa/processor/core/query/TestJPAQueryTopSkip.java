package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryTopSkip extends TestBase {

  @Test
  void testTop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$top=10");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(10, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31003", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(243, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31022", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testTopSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5&$top=5");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(5, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31022", act.get(0).get("DivisionCode").asText());
  }
}
