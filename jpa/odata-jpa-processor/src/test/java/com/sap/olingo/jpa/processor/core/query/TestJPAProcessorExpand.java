package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAProcessorExpand extends TestBase {

  @Test
  void testExpandEntitySet() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=ID&$expand=Roles");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    ObjectNode org = (ObjectNode) orgs.get(0);
    ArrayNode roles = (ArrayNode) org.get("Roles");
    assertEquals(1, roles.size());

    org = (ObjectNode) orgs.get(3);
    roles = (ArrayNode) org.get("Roles");
    assertEquals(3, roles.size());
  }

  @Test
  void testExpandOneEntity() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('2')?$expand=Roles");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ArrayNode roles = (ArrayNode) org.get("Roles");
    assertEquals(2, roles.size());
    int found = 0;
    for (final JsonNode role : roles) {
      final String id = role.get("BusinessPartnerID").asText();
      final String code = role.get("RoleCategory").asText();
      if (id.equals("2") && (code.equals("A") || code.equals("C")))
        found++;
    }
    assertEquals(2, found, "Not all expected results found");
  }

  @Test
  void testExpandOneEntityCompoundKey() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Parent");
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ObjectNode parent = (ObjectNode) division.get("Parent");
    assertEquals("BE2", parent.get("DivisionCode").asText());

  }

  @Test
  void testExpandOneEntityCompoundKeyCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Children($orderby=DivisionCode asc)");
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ArrayNode parent = (ArrayNode) division.get("Children");
    assertEquals(8, parent.size());
    assertEquals("BE251", parent.get(0).get("DivisionCode").asText());

  }

  @Test
  void testExpandEntitySetWithOutParentKeySelection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=Name1&$select=Name1&$expand=Roles");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(9);
    final ArrayNode roles = (ArrayNode) org.get("Roles");
    assertEquals(3, roles.size());

  }

  @Test
  void testExpandEntitySetViaNonKeyField_FieldNotSelected() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created?$select=At&$expand=User");
    helper.assertStatus(200);

    final ObjectNode created = helper.getValue();
    assertNotNull(created.get("User"));
  }

  @Test
  void testExpandEntitySetViaNonKeyFieldNavi2Hops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created?$expand=User");
    helper.assertStatus(200);

    final ObjectNode created = helper.getValue();
    @SuppressWarnings("unused")
    final ObjectNode user = (ObjectNode) created.get("User");
  }

  @Test
  void testExpandEntityViaComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Address?$expand=AdministrativeDivision");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ObjectNode created = (ObjectNode) org.get("AdministrativeDivision");
    assertEquals("USA", created.get("ParentDivisionCode").asText());
  }

  @Test
  void testExpandEntitySetViaNonKeyFieldNavi0Hops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$expand=AdministrativeInformation/Created/User");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ObjectNode admin = (ObjectNode) org.get("AdministrativeInformation");
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertNotNull(created.get("User"));

  }

  @Test
  void testExpandEntitySetViaNonKeyFieldNavi1Hop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation?$expand=Created/User");
    helper.assertStatus(200);

    final ObjectNode admin = helper.getValue();
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertNotNull(created.get("User"));
  }

  @Test
  void testNestedExpandNestedExpand2LevelsSelf() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE253',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent($expand=Children)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode parent = (ObjectNode) div.get("Parent");
    assertNotNull(parent.get("Children"));
    final ArrayNode children = (ArrayNode) parent.get("Children");
    assertEquals(8, children.size());
    assertEquals("NUTS3", children.get(0).get("CodeID").asText());
  }

  @Test
  void testNestedExpandNestedExpand3LevelsSelf() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='33016',CodeID='LAU2',CodePublisher='Eurostat')?$expand=Parent($expand=Parent($expand=Parent))");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode parent = (ObjectNode) div.get("Parent");
    assertNotNull(parent.get("Parent"));
    assertNotNull(parent.get("Parent").get("CodeID"));
    assertEquals("NUTS3", parent.get("CodeID").asText());
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent);
    assertNotNull(grandParent.get("CodeID"));
    assertEquals("NUTS2", grandParent.get("CodeID").asText());
    final ObjectNode greatGrandParent = (ObjectNode) grandParent.get("Parent");
    assertNotNull(greatGrandParent);
    assertNotNull(greatGrandParent.get("CodeID"));
    assertEquals("NUTS1", greatGrandParent.get("CodeID").asText());
  }

  @Test
  void testNestedExpandNestedExpand3Only1PossibleLevelsSelf() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Parent($expand=Parent($expand=Parent))");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode parent = (ObjectNode) div.get("Parent");
    assertNotNull(parent.get("CodeID"));
    assertEquals("NUTS1", parent.get("CodeID").asText());
  }

  @Test
  void testNestedExpandNestedExpand2LevelsMixed() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Address?$select=Country&$expand=AdministrativeDivision($expand=Parent)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode admin = (ObjectNode) div.get("AdministrativeDivision");
    assertNotNull(admin);
    final ObjectNode parent = (ObjectNode) admin.get("Parent");
    assertEquals("3166-1", parent.get("CodeID").asText());
  }

  @Disabled // TODO check how the result should look like
  @Test
  void testExpandWithNavigationToEntity() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE253',CodeID='3',CodePublisher='NUTS')?$expand=Parent/Parent");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode parent = (ObjectNode) div.get("Parent");
    assertNotNull(parent.get("Parent").get("CodeID"));
    assertEquals("1", parent.get("Parent").get("CodeID").asText());
  }

  @Disabled // TODO check with Olingo looks like OData does not support this
  @Test
  void testExpandWithNavigationToProperty() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE253',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent/CodeID");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode parent = (ObjectNode) div.get("Parent");
    assertNotNull(parent.get("CodeID"));
    assertEquals("NUTS2", parent.get("CodeID").asText());
    // TODO: Check how to create the response correctly
    // assertEquals(1, parent.size());
  }

  @Test
  void testExpandAfterNavigationToEntity() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$filter=DivisionCode eq 'BE21'&$expand=Children($orderby=DivisionCode)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("value").get(0).get("Children");
    assertNotNull(children);
    assertEquals(3, children.size());
    assertEquals("BE211", children.get(0).get("DivisionCode").asText());
    assertEquals("BE212", children.get(1).get("DivisionCode").asText());
    assertEquals("BE213", children.get(2).get("DivisionCode").asText());
  }

  @Test
  void testExpandAfterNavigationToEntityWithTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$filter=DivisionCode eq 'BE21'&$top=2&$expand=Children($orderby=DivisionCode)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("value").get(0).get("Children");
    assertNotNull(children);
    assertEquals(3, children.size());
    assertEquals("BE211", children.get(0).get("DivisionCode").asText());
    assertEquals("BE212", children.get(1).get("DivisionCode").asText());
    assertEquals("BE213", children.get(2).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDesc() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("Children");
    assertEquals(5, children.size());
    assertEquals("BE25", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByAsc() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($orderby=DivisionCode asc)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("Children");
    assertEquals(5, children.size());
    assertEquals("BE21", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDescTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($top=2;$orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("Children");
    assertEquals(2, children.size());
    assertEquals("BE25", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDescTopSkip() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($top=2;$skip=2;$orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ArrayNode children = (ArrayNode) div.get("Children");
    assertEquals(2, children.size());
    assertEquals("BE23", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithCount() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($count=true)&$orderby=Roles/$count desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(0);
    assertNotNull(org.get("Roles"));
    final ArrayNode roles = (ArrayNode) org.get("Roles");
    assertNotNull(org.get("Roles@odata.count"));
    assertEquals(roles.size(), org.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandWithCountPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('2')?$expand=Roles/$count");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Roles@odata.count"));
    assertEquals(2, org.get("Roles@odata.count").asInt());
  }

  @Disabled // ODataJsonSerializer.writeExpandedNavigationProperty does not write a "@odata.count" for to 1 relations
  @Test
  void testExpandOppositeDirectionWithCount() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles(BusinessPartnerID='1',RoleCategory='A')?$expand=Organization/$count");
    helper.assertStatus(200);

    final ObjectNode role = helper.getValue();
    assertNotNull(role.get("Organization"));
    assertNotNull(role.get("Organization@odata.count"));
    assertEquals("1", role.get("Organization@odata.count").asText());
  }

  @Test
  void testExpandWithCountAndTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($count=true;$top=1)&$orderby=Roles/$count desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(0);
    assertNotNull(org.get("Roles"));
    assertNotNull(org.get("Roles@odata.count"));
    assertEquals(3, org.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandWithOrderByDescTopSkipAndExternalOrderBy() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($orderby=RoleCategory desc)&$orderby=Roles/$count desc");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(0);
    assertEquals("3", org.get("ID").asText());
    assertNotNull(org.get("Roles"));
    final ArrayNode roles = (ArrayNode) org.get("Roles");
    assertEquals(3, roles.size());
    final ObjectNode firstRole = (ObjectNode) roles.get(0);
    assertEquals("C", firstRole.get("RoleCategory").asText());
  }

  @Test
  void testExpandWithFilter() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Children($filter=DivisionCode eq 'BE252')");

    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    assertEquals("BE25", division.get("DivisionCode").asText());
    assertNotNull(division.get("Children"));
    final ArrayNode children = (ArrayNode) division.get("Children");
    assertEquals(1, children.size());
    final ObjectNode firstChild = (ObjectNode) children.get(0);
    assertEquals("BE252", firstChild.get("DivisionCode").asText());
  }

  @Test
  void testFilterExpandWithFilter() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq 'BE25' and CodeID eq 'NUTS2'&$expand=Children($filter=DivisionCode eq 'BE252')");

    helper.assertStatus(200);

    final JsonNode value = helper.getValue().get("value");
    assertNotNull(value.get(0));
    final ObjectNode division = (ObjectNode) value.get(0);

    assertEquals("BE25", division.get("DivisionCode").asText());
    final ArrayNode children = (ArrayNode) division.get("Children");
    assertEquals(1, children.size());
    final ObjectNode firstChild = (ObjectNode) children.get(0);
    assertEquals("BE252", firstChild.get("DivisionCode").asText());
  }

  @Test
  void testFilterExpandWithFilterOnParentDescription() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=LocationName eq 'Deutschland'&$expand=Roles&$orderby=ID asc");

    helper.assertStatus(200);

    final JsonNode value = helper.getValue().get("value");
    assertNotNull(value.get(0));
    final ArrayNode roles = (ArrayNode) value.get(0).get("Roles");

    assertEquals(1, roles.size());
  }

  @Test
  void testExpandCompleteEntitySet() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$expand=Roles&$orderby=ID");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(0);
    assertEquals("1", org.get("ID").asText());
    assertNotNull(org.get("Roles"));
    final ArrayNode roles = (ArrayNode) org.get("Roles");
    assertEquals(1, roles.size());
    final ObjectNode firstRole = (ObjectNode) roles.get(0);
    assertEquals("A", firstRole.get("RoleCategory").asText());
  }

  @Test
  void testExpandTwoNavigationPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE32',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Parent,Children");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Parent"));
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final ArrayNode children = (ArrayNode) org.get("Children");
    assertEquals(7, children.size());
  }

  @Test
  void testExpandAllNavigationPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE32',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=*");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Parent"));
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final ArrayNode children = (ArrayNode) org.get("Children");
    assertEquals(7, children.size());
  }

  @Test
  void testExpandAllNavigationPathOfPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE32',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=*");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Parent"));
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final ArrayNode children = (ArrayNode) org.get("Children");
    assertEquals(7, children.size());
  }

  @Test
  void testExpandLevel1() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='38025',CodeID='LAU2',CodePublisher='Eurostat')?$expand=Parent($levels=1)");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Parent"));
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final TextNode divCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE258", divCode.asText());
  }

  @Test
  void testExpandLevel2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='38025',CodeID='LAU2',CodePublisher='Eurostat')?$expand=Parent($levels=2)");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertFalse(org.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    final TextNode parentDivCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE258", parentDivCode.asText());

    assertFalse(parent.get("Parent") instanceof NullNode);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent.get("DivisionCode"));
    final TextNode grandparentDivCode = (TextNode) grandParent.get("DivisionCode");
    assertEquals("BE25", grandparentDivCode.asText());
  }

  @Test
  void testExpandLevelMax() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE241',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent($levels=max)");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertFalse(org.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) org.get("Parent");
    final TextNode parentDivCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE24", parentDivCode.asText());

    assertFalse(parent.get("Parent") instanceof NullNode);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent.get("DivisionCode"));
    final TextNode grandparentDivCode = (TextNode) grandParent.get("DivisionCode");
    assertEquals("BE2", grandparentDivCode.asText());
  }

  @Test
  void testExpandAllNavigationPathWithComplex() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')?$expand=*");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("Roles"));

  }

  @Test
  void testExpandCompleteEntitySet2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "AdministrativeDivisions?$expand=Parent");

    helper.assertStatus(200);
  }

  @Test
  void testExpandViaJoinTable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('2')?$select=Name1&$expand=SupportEngineers($select=FirstName,LastName)");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("SupportEngineers"));

  }

  @Test
  void testExpandViaJoinTable2Levels() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$select=Name1&$expand=SupportEngineers($select=FirstName,LastName;$expand=SupportedOrganizations)");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("SupportEngineers"));
    final ArrayNode supportEngs = (ArrayNode) org.get("SupportEngineers");
    for (int i = 0; i < supportEngs.size(); i++) {
      final ObjectNode supportEng = (ObjectNode) supportEngs.get(i);
      final ArrayNode supportOrgs = (ArrayNode) supportEng.get("SupportedOrganizations");
      assertNotNull(supportOrgs);
      if (supportEng.get("ID").asText().equals("98")) {
        assertEquals(1, supportOrgs.size());
      } else if (supportEng.get("ID").asText().equals("97")) {
        assertEquals(2, supportOrgs.size());
      } else
        fail("Unexpected result");
    }
  }

  @Test
  void testExpandViaJoinTable2LevelsWithTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$top=1&$select=Name1&$expand=SupportEngineers($select=FirstName,LastName;$expand=SupportedOrganizations)&orderby=ID");
    helper.assertStatus(200);

    final ArrayNode org = (ArrayNode) helper.getValue().get("value");
    assertNotNull(org);
    assertNotNull(org.get(0));
    assertNotNull(org.get(0).get("SupportEngineers"));
    final ArrayNode supportEngs = (ArrayNode) org.get(0).get("SupportEngineers");
    for (int i = 0; i < supportEngs.size(); i++) {
      final ObjectNode supportEng = (ObjectNode) supportEngs.get(i);
      final ArrayNode supportOrgs = (ArrayNode) supportEng.get("SupportedOrganizations");
      assertNotNull(supportOrgs);
      if (supportEng.get("ID").asText().equals("98")) {
        assertEquals(1, supportOrgs.size());
      } else if (supportEng.get("ID").asText().equals("97")) {
        assertEquals(2, supportOrgs.size());
      } else
        fail("Unexpected result");
    }
  }

  @Test
  void testExpandViaJoinTable1LevelNoSubType() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=LastName&$expand=Teams");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org);
  }

  @Test
  void testExpandViaJoinTable1LevelNoMapped() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)?$expand=OneToMany");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("OneToMany"));
    final ArrayNode oneToMany = (ArrayNode) org.get("OneToMany");
    assertEquals(2, oneToMany.size());
  }

  @Test
  void testExpandViaJoinTable1LevelNoMappedHidden() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(2)?$expand=OneToManyHidden");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("OneToManyHidden"));
    final ArrayNode oneToMany = (ArrayNode) org.get("OneToManyHidden");
    assertEquals(2, oneToMany.size());
  }

  @Test
  void testExpandViaJoinTableComplex() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)/Complex?$expand=OneToManyComplex");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertNotNull(org.get("OneToManyComplex"));
    final ArrayNode oneToMany = (ArrayNode) org.get("OneToManyComplex");
    assertEquals(2, oneToMany.size());
  }

  @Test
  void testExpandWithSelect() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Children($select=Population)");

    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    assertEquals("BE25", division.get("DivisionCode").asText());
    assertNotNull(division.get("Children"));
    final ArrayNode children = (ArrayNode) division.get("Children");
    assertEquals(8, children.size());
    final ObjectNode firstChild = (ObjectNode) children.get(0);
    assertEquals(5, firstChild.size()); // Ref + Key + Population
    assertTrue(firstChild.has("Population"));
    assertFalse(firstChild.has("Area"));
  }

  @Test
  void testExpandGroupNotProvided() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('3')?$expand=Roles");
    helper.assertStatus(200);
    final ObjectNode act = helper.getValue();
    final ArrayNode actRoles = (ArrayNode) act.get("Roles");
    actRoles.forEach(an -> assertTrue(an.get("Details").isNull()));
  }

  @Test
  void testExpandGroupProvided() throws IOException, ODataException {
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('3')?$expand=Roles", groups);
    helper.assertStatus(200);
    final ObjectNode act = helper.getValue();
    final ArrayNode actRoles = (ArrayNode) act.get("Roles");
    actRoles.forEach(an -> assertFalse(an.get("Details").isNull()));
  }

  @Test
  void testExpandOnlyThoseFromTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$top=2&$skip=2&$expand=Roles($count=true;$top=1)");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    final ObjectNode org = (ObjectNode) orgs.get(1);
    assertEquals(2, orgs.size());
    assertNotNull(org.get("Roles"));
    assertNotNull(org.get("Roles@odata.count"));
    assertEquals(3, org.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandReturnsTransientProperties() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Teams?$expand=Member($select=FullName)&$orderby=ID");
    helper.assertStatus(200);

    final ArrayNode teams = helper.getValues();
    final ObjectNode team = (ObjectNode) teams.get(0);
    assertNotNull(team.get("Member"));
    final ArrayNode members = (ArrayNode) team.get("Member");
    assertEquals(2, members.size());
    final ObjectNode member = (ObjectNode) members.get(0);
    assertFalse(member.get("FullName") instanceof NullNode);
  }

}
