package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestJPAQueryOrderByClause extends TestBase {

  @Test
  public void testOrderByOneProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Name1");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("Eighth Org.", orgs.get(0).get("Name1").asText());
    assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  public void testOrderByOneComplexPropertyAsc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Address/Region");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("US-CA", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-UT", orgs.get(9).get("Address").get("Region").asText());
  }

  @Test
  public void testOrderByOneComplexPropertyDesc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Address/Region desc");
    if (helper.getStatus() != 200)
      System.out.println(helper.getRawResult());
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
  }

  @Test
  public void testOrderByTwoPropertiesDescAsc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Address/Region desc,Name1 asc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  public void testOrderByTwoPropertiesDescDesc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Address/Region desc,Name1 desc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("First Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  public void testOrderBy$CountDesc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Roles/$count desc");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(0).get("ID").asText());
    assertEquals("2", orgs.get(1).get("ID").asText());
  }

  @Test
  public void testOrderBy$CountAsc() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$orderby=Roles/$count asc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(9).get("ID").asText());
    assertEquals("2", orgs.get(8).get("ID").asText());
  }

  @Test
  public void testOrderBy$CountDescComplexPropertyAcs() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations?$orderby=Roles/$count desc, Address/Region desc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(0).get("ID").asText());
    assertEquals("2", orgs.get(1).get("ID").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("6", orgs.get(9).get("ID").asText());
  }
}
