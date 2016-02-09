package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestJPAQueryWhereClause extends TestBase {

  @Test
  public void testFilterOneEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID eq '3'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  public void testFilterOneEqualsInvert() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter='3' eq ID");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  public void testFilterOneNotEqual() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID ne '3'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
  }

  @Test
  public void testFilterOneGreaterEqualsString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID ge '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  public void testFilterOneGreaterThanString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID gt '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  public void testFilterOneLowerThanString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID lt '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
  }

  @Test
  public void testFilterOneLowerEqualsString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations?$filter=ID le '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(6, orgs.size());
  }

  @Test
  public void testFilterOneGreaterEqualsNumber() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("AdministrativeDivisions?$filter=Area ge 119330610");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterOneAndEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  public void testFilterOneOrEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations?$filter=ID eq '5' or ID eq '10'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterOneNotLower() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions?$filter=not (Area lt 50000000)");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
  }
}
