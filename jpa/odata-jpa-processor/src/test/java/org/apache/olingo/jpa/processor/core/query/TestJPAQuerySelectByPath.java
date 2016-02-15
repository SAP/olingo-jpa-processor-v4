package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestJPAQuerySelectByPath extends TestBase {

  @Test
  public void testNavigationToOwnPrimitiveProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Name1");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("Third Org.", org.get("value").asText());
  }

  @Test
  public void testNavigationToOwnPrimitiveDescriptionProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/LocationName");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("Vereinigte Staaten von Amerika", org.get("value").asText());
  }

  @Test
  public void testNavigationToComplexProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("USA", org.get("Country").asText());
  }

  @Test
  public void testNavigationToNestedComplexProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/AdministrativeInformation/Created");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    JsonNode created = org.get("Created");
    assertEquals("99", created.get("By").asText());
  }

  @Test
  public void testNavigationViaComplexAndNaviPropertyToPrimitive() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User/FirstName");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("Max", org.get("value").asText());
  }

  @Test
  public void testNavigationToComplexPropertySelect() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/Address?$select=Country,Region");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals(3, org.size()); // Node "@odata.context" is also counted
    assertEquals("USA", org.get("Country").asText());
    assertEquals("US-UT", org.get("Region").asText());
  }

  @Ignore
  @Test
  public void testNavigationToComplexPropertyExpand() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("USA", org.get("Country").asText());
  }

  @Test
  public void testNavigationToComplexPrimitiveProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address/Region");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("US-UT", org.get("Region").asText());
  }
}
