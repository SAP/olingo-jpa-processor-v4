package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestJPAQueryNavigation extends TestBase {

  @Test
  public void testNavigationOneHop() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations('3')/Roles");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  public void testNoNavigationOneEntity() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations('3')");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("Third Org.", org.get("Name1").asText());
  }

  @Test
  public void testNavigationOneHopAndOrderBy() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper("Organizations('3')/Roles?$orderby=RoleCategory desc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
    assertEquals("C", orgs.get(0).get("RoleCategory").asText());
    assertEquals("A", orgs.get(2).get("RoleCategory").asText());
  }

  @Test
  public void testNavigationOneHopReverse() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "BusinessPartnerRoles(BusinessPartnerID='2',RoleCategory='A')/BusinessPartner");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("2", org.get("ID").asText());
  }

  @Test
  public void testNavigationViaComplexType() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations('3')/AdministrativeInformation/Created/User");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("99", org.get("ID").asText());
  }

  @Test
  public void testNavigationViaComplexTypeTwoHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations('3')/AdministrativeInformation/Created/User/Address/AdministrativeDivision");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("3166-1", org.get("ParentCodeID").asText());
  }

  @Test
  public void testNavigationSelfToOneOneHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions(DivisionCode='BE352',CodeID='3',CodePublisher='NUTS')/Parent");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("2", org.get("CodeID").asText());
    assertEquals("BE35", org.get("DivisionCode").asText());
  }

  @Test
  public void testNavigationSelfToOneTwoHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions(DivisionCode='BE352',CodeID='3',CodePublisher='NUTS')/Parent/Parent");
    assertEquals(200, helper.getStatus());

    ObjectNode org = helper.getValue();
    assertEquals("1", org.get("CodeID").asText());
    assertEquals("BE3", org.get("DivisionCode").asText());
  }

  @Test
  public void testNavigationSelfToManyOneHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='1',CodePublisher='NUTS')/Children?$orderby=DivisionCode desc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
    assertEquals("2", orgs.get(0).get("CodeID").asText());
    assertEquals("BE25", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  public void testNavigationSelfToManyTwoHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='1',CodePublisher='NUTS')/Children(DivisionCode='BE25',CodeID='2',CodePublisher='NUTS')/Children?$orderby=DivisionCode desc");
    assertEquals(200, helper.getStatus());

    ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
    assertEquals("3", orgs.get(0).get("CodeID").asText());
    assertEquals("BE258", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  public void testNavigationSelfToOneThreeHopsNoResult() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations('3')/Address/AdministrativeDivision/Parent/Parent");
    helper.assertStatus(204);
  }

  @Test
  public void testNavigationSelfToManyOneHopsNoResult() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Organizations('3')/Address/AdministrativeDivision/Children");
    helper.assertStatus(204);
  }
}
