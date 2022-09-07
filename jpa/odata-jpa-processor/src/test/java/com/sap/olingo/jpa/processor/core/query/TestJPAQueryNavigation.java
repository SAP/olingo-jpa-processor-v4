package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryNavigation extends TestBase {

  @Test
  void testNavigationOneHop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Roles");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  void testNoNavigationOneEntity() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("Third Org.", org.get("Name1").asText());
  }

  @Test
  void testNoNavigationOneEntityCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ArrayNode comment = (ArrayNode) org.get("Comment");
    assertEquals(2, comment.size());
  }

  @Test
  void testNoNavigationOneEntityNoContent() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1000')");
    helper.assertStatus(404);
  }

  @Test
  void testNavigationToComplexValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created");
    helper.assertStatus(200);

    final ObjectNode created = helper.getValue();
    assertEquals("99", created.get("By").asText());
  }

  @Test
  void testNavigationOneHopAndOrderBy() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Roles?$orderby=RoleCategory desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
    assertEquals("C", orgs.get(0).get("RoleCategory").asText());
    assertEquals("A", orgs.get(2).get("RoleCategory").asText());
  }

  @Test
  void testNavigationOneHopReverse() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='2',RoleCategory='A')/BusinessPartner");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("2", org.get("ID").asText());
  }

  @Test
  void testNavigationViaComplexTypeToComplexType() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User/AdministrativeInformation");
    helper.assertStatus(200);

    final ObjectNode admin = helper.getValue();
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertEquals("99", created.get("By").asText());
  }

  @Test
  void testNavigationViaComplexTypeToPrimitive() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User/AdministrativeInformation/Created/At");
    helper.assertStatus(200);

    final ObjectNode admin = helper.getValue();
    final TextNode at = (TextNode) admin.get("value");
    assertNotNull(at);
  }

  @Test
  void testNavigationViaComplexType() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("99", org.get("ID").asText());
  }

  @Test
  void testNavigationViaComplexTypeTwoHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User/Address/AdministrativeDivision");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("3166-1", org.get("ParentCodeID").asText());
  }

  @Test
  void testNavigationSelfToOneOneHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE352',CodeID='NUTS3',CodePublisher='Eurostat')/Parent");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("NUTS2", org.get("CodeID").asText());
    assertEquals("BE35", org.get("DivisionCode").asText());
  }

  @Test
  void testNavigationSelfToOneTwoHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE352',CodeID='NUTS3',CodePublisher='Eurostat')/Parent/Parent");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("NUTS1", org.get("CodeID").asText());
    assertEquals("BE3", org.get("DivisionCode").asText());
  }

  @Test
  void testNavigationSelfToManyOneHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
    assertEquals("NUTS2", orgs.get(0).get("CodeID").asText());
    assertEquals("BE25", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testNavigationSelfToManyTwoHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')/Children?$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
    assertEquals("NUTS3", orgs.get(0).get("CodeID").asText());
    assertEquals("BE258", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testNavigationSelfToOneThreeHopsNoResult() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Address/AdministrativeDivision/Parent/Parent");
    helper.assertStatus(204);
  }

  @Test
  void testNavigationSelfToManyOneHopsNoResult() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Address/AdministrativeDivision/Children");
    helper.assertStatus(200);
  }

  @Test
  void testNavigationJoinTableDefined() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('97')/SupportedOrganizations");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testNavigationJoinTableDefinedSecondHop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='98',RoleCategory='X')/BusinessPartner/com.sap.olingo.jpa.Person/SupportedOrganizations");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testNavigationWithCast() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='98',RoleCategory='X')/BusinessPartner/com.sap.olingo.jpa.Person");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("Doe", org.get("LastName").asText());
  }

  @Test
  void testNavigationJoinTableMappedBy() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/SupportEngineers");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testNavigationComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/AdministrativeInformation");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();

    assertNotNull(org.get("Created"));
    assertNotNull(org.get("Updated"));
  }

  @Test
  void testNavigationPrimitiveCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/Comment");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("value"));
    assertFalse(org.get("value").isNull());
    final ArrayNode values = (ArrayNode) org.get("value");
    assertEquals(2, values.size());
    assertTrue(values.get(0).asText().equals("This is just a test") || values.get(0).asText().equals(
        "This is another test"));
    assertTrue(values.get(1).asText().equals("This is just a test") || values.get(1).asText().equals(
        "This is another test"));
  }

  @Test
  void testNavigationComplexCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('99')/InhouseAddress");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("value"));
    assertFalse(org.get("value").isNull());
    final ArrayNode values = (ArrayNode) org.get("value");
    assertEquals(2, values.size());
  }

  @Test
  void testNavigationComplexCollectionPropertyEmptyReult() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('98')/InhouseAddress");
    helper.assertStatus(200);
  }

  @Test
  void testNavigationPrimitiveCollectionPropertyTwoHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='1',RoleCategory='A')/Organization/Comment");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("value"));
    assertFalse(org.get("value").isNull());
    final ArrayNode values = (ArrayNode) org.get("value");
    assertEquals(2, values.size());
    assertTrue(values.get(0).asText().equals("This is just a test") || values.get(0).asText().equals(
        "This is another test"));
    assertTrue(values.get(1).asText().equals("This is just a test") || values.get(1).asText().equals(
        "This is another test"));
  }

  @Test
  void testNavigationViaEntitySetOnly() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BestOrganizations");
    helper.assertStatus(200);
  }
}
