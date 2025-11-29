package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCapabilitiesAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;
import com.sap.olingo.jpa.processor.core.util.Assertions;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAProcessorExpand extends TestBase {

  @BeforeAll
  static void classSetup() {
    System.setProperty("organization.slf4j.simpleLogger.log.com.sap.olingo.jpa.processor.core.query", "TRACE");
  }

  @Test
  void testExpandEntitySet() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=ID&$expand=Roles");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    ObjectNode organization = (ObjectNode) organizations.get(0);
    ArrayNode roles = (ArrayNode) organization.get("Roles");
    assertEquals(1, roles.size());

    organization = (ObjectNode) organizations.get(3);
    roles = (ArrayNode) organization.get("Roles");
    assertEquals(3, roles.size());
  }

  @Test
  void testExpandCompleteEntitySet2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "AdministrativeDivisions?$expand=Parent");

    helper.assertStatus(200);
  }

  @Test
  void testExpandOneEntity() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('2')?$expand=Roles");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
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

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(9);
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
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
  void testExpandEntitySetViaNonKeyFieldNavigation2Hops() throws IOException, ODataException {

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

    final ObjectNode organization = helper.getValue();
    final ObjectNode created = (ObjectNode) organization.get("AdministrativeDivision");
    assertEquals("USA", created.get("ParentDivisionCode").asText());
  }

  @Test
  void testExpandEntitySetViaNonKeyFieldNavigation0Hops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')?$expand=AdministrativeInformation/Created/User");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    final ObjectNode admin = (ObjectNode) organization.get("AdministrativeInformation");
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertNotNull(created.get("User"));

  }

  @Test
  void testExpandEntitySetViaNonKeyFieldNavigation1Hop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation?$expand=Created/User");
    helper.assertStatus(200);

    final ObjectNode admin = helper.getValue();
    final ObjectNode created = (ObjectNode) admin.get("Created");
    assertNotNull(created.get("User"));
  }

  @Test
  void testExpandSingletonViaNonKeyFieldNavigation1Hop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CurrentUser/AdministrativeInformation?$expand=Created/User");
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

    final ObjectNode divisions = helper.getValue();
    final ObjectNode parent = (ObjectNode) divisions.get("Parent");
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

    final ObjectNode divisions = helper.getValue();
    final ObjectNode parent = (ObjectNode) divisions.get("Parent");
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

    final ObjectNode divisions = helper.getValue();
    final ObjectNode parent = (ObjectNode) divisions.get("Parent");
    assertNotNull(parent.get("CodeID"));
    assertEquals("NUTS1", parent.get("CodeID").asText());
  }

  @Test
  void testNestedExpandNestedExpand2LevelsMixed() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Address?$select=Country&$expand=AdministrativeDivision($expand=Parent)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ObjectNode admin = (ObjectNode) divisions.get("AdministrativeDivision");
    assertNotNull(admin);
    final ObjectNode parent = (ObjectNode) admin.get("Parent");
    assertEquals("3166-1", parent.get("CodeID").asText());
  }

  @Disabled("check how the result should look like")
  @Test
  void testExpandWithNavigationToEntity() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE253',CodeID='3',CodePublisher='NUTS')?$expand=Parent/Parent");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ObjectNode parent = (ObjectNode) divisions.get("Parent");
    assertNotNull(parent.get("Parent").get("CodeID"));
    assertEquals("1", parent.get("Parent").get("CodeID").asText());
  }

  @Disabled("Check with Olingo looks like OData does not support this")
  @Test
  void testExpandWithNavigationToProperty() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE253',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent/CodeID");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ObjectNode parent = (ObjectNode) divisions.get("Parent");
    assertNotNull(parent.get("CodeID"));
    assertEquals("NUTS2", parent.get("CodeID").asText());
    // TODO: Check how to create the response correctly
  }

  @Test
  void testExpandAfterNavigationToEntity() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$filter=DivisionCode eq 'BE21'&$expand=Children($orderby=DivisionCode)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("value").get(0).get("Children");
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

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("value").get(0).get("Children");
    assertNotNull(children);
    assertEquals(3, children.size());
    assertEquals("BE211", children.get(0).get("DivisionCode").asText());
    assertEquals("BE212", children.get(1).get("DivisionCode").asText());
    assertEquals("BE213", children.get(2).get("DivisionCode").asText());
  }

  @Test
  void testExpandViaNavigationToOneFails() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Parent/Children");
    helper.assertStatus(400);
  }

  @Test
  void testExpandWithOrderByDesc() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("Children");
    assertEquals(5, children.size());
    assertEquals("BE25", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithTopSkip2Level() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodeID eq 'NUTS1'&$top=4&$skip=1&$expand=Children($top=2;$expand=Children($top=1;$skip=1))&orderby=DivisionCode");
    helper.assertStatus(200);

    final ArrayNode grands = helper.getValues();
    assertEquals(4, grands.size());
    final ObjectNode grand = (ObjectNode) grands.get(1);
    assertEquals("BE3", grand.get("DivisionCode").asText());
    final ArrayNode parents = (ArrayNode) grand.get("Children");
    assertEquals(2, parents.size());
    final ObjectNode parent = (ObjectNode) parents.get(1);
    assertEquals("BE32", parent.get("DivisionCode").asText());
    final ArrayNode children = (ArrayNode) parent.get("Children");
    assertEquals(1, children.size());
  }

  @Test
  void testExpandWithOrderByAsc() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($orderby=DivisionCode asc)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("Children");
    assertEquals(5, children.size());
    assertEquals("BE21", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDescTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$select=CodeID&$expand=Children($top=2;$orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("Children");
    assertEquals(2, children.size());
    assertEquals("BE25", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDescTopSkip() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')?$expand=Children($top=2;$skip=2;$orderby=DivisionCode desc)");
    helper.assertStatus(200);

    final ObjectNode divisions = helper.getValue();
    final ArrayNode children = (ArrayNode) divisions.get("Children");
    assertEquals(2, children.size());
    assertEquals("BE23", children.get(0).get("DivisionCode").asText());
  }

  @Test
  void testExpandWithOrderByDescriptionProperty() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$expand=SupportedOrganizations($select=ID,LocationName;$orderby=LocationName desc)");
    helper.assertStatus(200);
  }

  @Test
  void testExpandWithOrderByToOneDescriptionProperty() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$expand=Roles($orderby=Organization/LocationName desc)");
    helper.assertStatus(200);

  }

  @Test
  void testExpandWithCount() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($count=true)");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertNotNull(organization.get("Roles"));
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
    assertNotNull(organization.get("Roles@odata.count"));
    assertEquals(roles.size(), organization.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandWithCount2Level() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        """
            AdministrativeDivisions?$count=true\
            &$expand=Children($count=true;$expand=Children($count=true))\
            &$filter=CodeID eq 'NUTS1' and startswith(DivisionCode,'BE')""");
    helper.assertStatus(200);

    final ArrayNode grands = helper.getValues();
    final ObjectNode grand = (ObjectNode) grands.get(1);
    assertNotNull(grand.get("Children"));
    final ArrayNode parents = (ArrayNode) grand.get("Children");
    assertNotNull(grand.get("Children@odata.count"));
    assertEquals(parents.size(), grand.get("Children@odata.count").asInt());

    final ObjectNode parent = (ObjectNode) parents.get(2);
    final ArrayNode children = (ArrayNode) parent.get("Children");
    assertNotNull(children);
    assertNotNull(parent.get("Children@odata.count"));
    assertEquals(children.size(), parent.get("Children@odata.count").asInt());
  }

  @Test
  void testExpandWithCountViaJoinTable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)?$expand=OneToMany($count=true)");
    helper.assertStatus(200);

    final ObjectNode source = helper.getValue();
    assertNotNull(source.get("OneToMany"));
    final ArrayNode targets = (ArrayNode) source.get("OneToMany");
    assertNotNull(source.get("OneToMany@odata.count"));
    assertEquals(targets.size(), source.get("OneToMany@odata.count").asInt());
  }

  @Test
  void testExpandWithCountWithOrderBy() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($count=true)&$orderby=Roles/$count desc");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertNotNull(organization.get("Roles"));
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
    assertNotNull(organization.get("Roles@odata.count"));
    assertEquals(roles.size(), organization.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandWithCountOrderBy() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$count=true&$expand=Children($count=true;$orderby=Children/$count desc,DivisionCode desc)&$filter=CodeID eq 'NUTS1' and startswith(DivisionCode,'BE')");
    helper.assertStatus(200);

    final ArrayNode parents = helper.getValues();
    final ObjectNode parent = (ObjectNode) parents.get(1);
    final ArrayNode children = (ArrayNode) parent.get("Children");
    assertNotNull(children);
    assertNotNull(parent.get("Children@odata.count"));
    assertEquals(children.size(), parent.get("Children@odata.count").asInt());

    final ObjectNode child = (ObjectNode) children.get(0);
    assertEquals("BE25", child.get("DivisionCode").asText());

  }

  @Test
  void testExpandWithCountPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('2')?$expand=Roles/$count");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("Roles@odata.count"));
    assertEquals(2, organization.get("Roles@odata.count").asInt());
  }

  @Disabled("ODataJsonSerializer.writeExpandedNavigationProperty does not write a \"@odata.count\" for to 1 relations")
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

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertNotNull(organization.get("Roles"));
    assertNotNull(organization.get("Roles@odata.count"));
    assertEquals(3, organization.get("Roles@odata.count").asInt());
  }

  @Test
  void testExpandWithOrderByDescTopSkipAndExternalOrderBy() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$count=true&$expand=Roles($orderby=RoleCategory desc)&$orderby=Roles/$count desc");

    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertEquals("3", organization.get("ID").asText());
    assertNotNull(organization.get("Roles"));
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
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
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$expand=Roles&$orderby=ID");

    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertEquals("1", organization.get("ID").asText());
    assertNotNull(organization.get("Roles"));
    final ArrayNode roles = (ArrayNode) organization.get("Roles");
    assertEquals(1, roles.size());
    final ObjectNode firstRole = (ObjectNode) roles.get(0);
    assertEquals("A", firstRole.get("RoleCategory").asText());
  }

  @Test
  void testExpandTwoNavigationPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE32',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=Parent,Children");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("Parent"));
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final ArrayNode children = (ArrayNode) organization.get("Children");
    assertEquals(7, children.size());
  }

  @Test
  void testExpandAllNavigationPath() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE32',CodeID='NUTS2',CodePublisher='Eurostat')?$expand=*");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("Parent"));
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final ArrayNode children = (ArrayNode) organization.get("Children");
    assertEquals(7, children.size());
  }

  @Test
  void testExpandLevel1() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='38025',CodeID='LAU2',CodePublisher='Eurostat')?$expand=Parent($levels=1)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("Parent"));
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    assertNotNull(parent.get("DivisionCode"));
    final TextNode divisionCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE258", divisionCode.asText());
  }

  @Test
  void testExpandLevel2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='38025',CodeID='LAU2',CodePublisher='Eurostat')?$expand=Parent($levels=2)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertFalse(organization.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    final TextNode parentDivisionCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE258", parentDivisionCode.asText());

    assertFalse(parent.get("Parent") instanceof NullNode);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent.get("DivisionCode"));
    final TextNode grandparentDivisionCode = (TextNode) grandParent.get("DivisionCode");
    assertEquals("BE25", grandparentDivisionCode.asText());

    assertTrue(grandParent.get("Parent") == null || grandParent.get("Parent") instanceof NullNode);
  }

  @Test
  void testExpandStarAndLevel2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='38025',CodeID='LAU2',CodePublisher='Eurostat')?$expand=*($levels=2)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertFalse(organization.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    final TextNode parentDivisionCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE258", parentDivisionCode.asText());

    assertFalse(parent.get("Parent") instanceof NullNode);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent.get("DivisionCode"));
    final TextNode grandparentDivisionCode = (TextNode) grandParent.get("DivisionCode");
    assertEquals("BE25", grandparentDivisionCode.asText());

    assertTrue(grandParent.get("Parent") == null || grandParent.get("Parent") instanceof NullNode);
  }

  @Test
  void testExpandStarAndLevel1() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE258',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=*($levels=1)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertFalse(organization.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    final TextNode parentDivisionCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE25", parentDivisionCode.asText());

    assertTrue(parent.get("Parent") == null || parent.get("Parent") instanceof NullNode);

    assertFalse(organization.get("Children") instanceof NullNode);
    final ArrayNode children = (ArrayNode) organization.get("AllDescriptions");
    assertTrue(children.size() > 0);

    assertFalse(organization.get("AllDescriptions") instanceof NullNode);
    final ArrayNode allDescriptions = (ArrayNode) organization.get("AllDescriptions");
    assertTrue(allDescriptions.size() > 0);
  }

  @Disabled("Not implemented")
  @Test
  void testExpandLevelMax() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE241',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent($levels=max)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertFalse(organization.get("Parent") instanceof NullNode);
    final ObjectNode parent = (ObjectNode) organization.get("Parent");
    final TextNode parentDivisionsCode = (TextNode) parent.get("DivisionCode");
    assertEquals("BE24", parentDivisionsCode.asText());

    assertFalse(parent.get("Parent") instanceof NullNode);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent.get("DivisionCode"));
    final TextNode grandparentDivisionsCode = (TextNode) grandParent.get("DivisionCode");
    assertEquals("BE2", grandparentDivisionsCode.asText());
  }

  @Test
  void testExpandAllNavigationPathWithComplex() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')?$expand=*");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("Roles"));

  }

  @Test
  void testExpandLevelAndRelated() throws IOException, ODataException {
    // Expected result would be one division plus parent plus children plus parent of children
    // As Olingo has a bug, the parent of children is missing
    // see https://issues.apache.org/jira/browse/OLINGO-1608
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$top=1&$expand=Children($levels=1;$expand=Parent)&$filter=CodeID eq 'NUTS2'");

    helper.assertStatus(200);
    final ObjectNode division = helper.getValue();
    assertNotNull(division.get("value").get(0).get("Children"));
    final ArrayNode children = (ArrayNode) division.get("value").get(0).get("Children");
    assertFalse(children.isEmpty());
    assertNotNull(children.get(0).get("CodePublisher"));
    assertNull(children.get(0).get("Parent"));
  }

  @Test
  void testExpandViaJoinTable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('2')?$select=Name1&$expand=SupportEngineers($select=FirstName,LastName)");

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("SupportEngineers"));
  }

  @Test
  void testExpandViaJoinTable1LevelWithTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)?$expand=OneToMany($top=1)");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("OneToMany"));
    final ArrayNode oneToMany = (ArrayNode) organization.get("OneToMany");
    assertEquals(1, oneToMany.size());
  }

  @Test
  void testExpandViaJoinTable2Levels() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$select=Name1&$expand=SupportEngineers($select=FirstName,LastName;$expand=SupportedOrganizations($select=ID))");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("SupportEngineers"));
    final ArrayNode supportEngineers = (ArrayNode) organization.get("SupportEngineers");
    for (int i = 0; i < supportEngineers.size(); i++) {
      final ObjectNode supportEngineer = (ObjectNode) supportEngineers.get(i);
      final ArrayNode supportOrganizations = (ArrayNode) supportEngineer.get("SupportedOrganizations");
      assertNotNull(supportOrganizations);
      if (supportEngineer.get("ID").asText().equals("98")) {
        assertEquals(1, supportOrganizations.size());
      } else if (supportEngineer.get("ID").asText().equals("97")) {
        assertEquals(2, supportOrganizations.size());
      } else
        fail("Unexpected result");
    }
  }

  @Test
  void testExpandViaJoinTable2LevelsWithTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$top=1&$select=Name1&$expand=SupportEngineers($select=FirstName,LastName;$expand=SupportedOrganizations)&$orderby=ID");
    helper.assertStatus(200);

    final ArrayNode organization = (ArrayNode) helper.getValue().get("value");
    assertNotNull(organization);
    assertNotNull(organization.get(0));
    assertNotNull(organization.get(0).get("SupportEngineers"));
    final ArrayNode supportEngineers = (ArrayNode) organization.get(0).get("SupportEngineers");
    assertEquals(2, supportEngineers.size());
    for (int i = 0; i < supportEngineers.size(); i++) {
      final ObjectNode supportEngineer = (ObjectNode) supportEngineers.get(i);
      final ArrayNode supportOrganizations = (ArrayNode) supportEngineer.get("SupportedOrganizations");
      assertNotNull(supportOrganizations);
      if (supportEngineer.get("ID").asText().equals("98")) {
        assertEquals(1, supportOrganizations.size());
      } else if (supportEngineer.get("ID").asText().equals("97")) {
        assertEquals(2, supportOrganizations.size());
      } else
        fail("Unexpected result");
    }
  }

  @Test
  void testExpandViaJoinTable2LevelsAllTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        // "Organizations?$top=1&$select=Name1&$expand=SupportEngineers($top=1)&$orderby=ID");
        "Organizations?$top=1&$select=Name1&$expand=SupportEngineers($select=FirstName,LastName;$top=1;$expand=SupportedOrganizations($top=1))&$orderby=ID");
    helper.assertStatus(200);

    final ArrayNode organization = (ArrayNode) helper.getValue().get("value");
    assertNotNull(organization);
    assertNotNull(organization.get(0));
    assertNotNull(organization.get(0).get("SupportEngineers"));
    final ArrayNode supportEngineers = (ArrayNode) organization.get(0).get("SupportEngineers");
    assertEquals(1, supportEngineers.size());
    final ObjectNode supportEngineer = (ObjectNode) supportEngineers.get(0);
    assertEquals("97", supportEngineer.get("ID").asText());
    final ArrayNode supportOrganizations = (ArrayNode) supportEngineer.get("SupportedOrganizations");
    assertEquals(1, supportOrganizations.size());

  }

  @Test
  void testExpand2LevelsLastViaJoinTable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created?$expand=User($select=FirstName,LastName;$expand=SupportedOrganizations)");
    helper.assertStatus(200);
  }

  @Test
  void testExpandViaJoinTableWithTopAndOrderBy() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$top=1&$select=Name1&$expand=SupportEngineers($select=FirstName,LastName)&$orderby=ID desc");
    helper.assertStatus(200);

    final ArrayNode organization = (ArrayNode) helper.getValue().get("value");
    assertNotNull(organization);
    assertNotNull(organization.get(0));
    assertEquals("9", organization.get(0).get("ID").asText());
    assertNotNull(organization.get(0).get("SupportEngineers"));
    assertEquals(0, ((ArrayNode) organization.get(0).get("SupportEngineers")).size());
  }

  @Test
  void testExpandViaJoinTable1LevelNoSubType() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=LastName&$expand=Teams");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization);
  }

  @Test
  void testExpandViaJoinTable1LevelNoMapped() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)?$expand=OneToMany");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("OneToMany"));
    final ArrayNode oneToMany = (ArrayNode) organization.get("OneToMany");
    assertEquals(4, oneToMany.size());
  }

  @Test
  void testExpandViaJoinTable1LevelNoMappedHidden() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(2)?$expand=OneToManyHidden");

    final int status = helper.getStatus();
    // Status is either 500 for sub query based expand or 200 for join based expand
    if (status == 500) {
      final ObjectNode organization = helper.getValue();
      final ObjectNode err = (ObjectNode) organization.get("error");
      final String msg = err.get("message").asText();
      assertTrue(msg.contains("JoinHiddenRelation"), msg);
    } else {
      helper.assertStatus(200);

      final ObjectNode organization = helper.getValue();
      assertNotNull(organization.get("OneToManyHidden"));
      final ArrayNode oneToMany = (ArrayNode) organization.get("OneToManyHidden");
      assertEquals(2, oneToMany.size());
    }
  }

  @Test
  void testExpandViaJoinTableComplex() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)/Complex?$expand=OneToManyComplex");
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization.get("OneToManyComplex"));
    final ArrayNode oneToMany = (ArrayNode) organization.get("OneToManyComplex");
    assertEquals(4, oneToMany.size());
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

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(1);
    assertEquals(2, organizations.size());
    assertNotNull(organization.get("Roles"));
    assertNotNull(organization.get("Roles@odata.count"));
    assertEquals(3, organization.get("Roles@odata.count").asInt());
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

  @Test
  void testExpandReturnsCast() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$expand=BusinessPartner/com.sap.olingo.jpa.Person");
    helper.assertStatus(200);

    final ArrayNode roles = helper.getValues();
    for (final JsonNode role : roles) {
      final String id = role.get("BusinessPartnerID").asText();
      final JsonNode bupas = role.get("BusinessPartner");

      if ("99".equals(id) || "98".equals(id) || "97".equals(id)) {
        assertTrue(bupas instanceof ObjectNode);
        assertNotNull(bupas.get("FullName"));
      } else {
        assertTrue(bupas instanceof NullNode, "For id: " + id);
      }
    }
  }

  @Test
  void testExpandReturnsCastWithFilter() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$expand=BusinessPartner/com.sap.olingo.jpa.Person($filter=ID eq '1')");
    helper.assertStatus(200);

    final ArrayNode roles = helper.getValues();
    for (final JsonNode role : roles) {
      final String id = role.get("BusinessPartnerID").asText();
      final JsonNode bupas = role.get("BusinessPartner");

      assertTrue(bupas instanceof NullNode, "For id: " + id);
    }
  }

  @Tag(Assertions.CB_ONLY_TEST)
  @Test
  void testExpandToOneVirtualProperty() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AssociationOneToOneSources?$expand=DefaultTarget");
    helper.assertStatus(200);
    final ArrayNode sources = helper.getValues();
    assertEquals(4, sources.size());
    for (final JsonNode source : sources) {
      source.get("DefaultTarget");
      if ("SD".equals(source.get("ID").asText()))
        assertTrue(source.get("DefaultTarget").isNull());
      else
        assertFalse(source.get("DefaultTarget").isNull());
    }
  }

  @Test
  void testExpandStarIgnoresNonExpandable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents(CodePublisher='Eurostat',CodeID='NUTS2',DivisionCode='BE25')?$expand=*",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ArrayNode children = (ArrayNode) division.get("Children");
    assertTrue(children.isEmpty());
    final ObjectNode parent = (ObjectNode) division.get("Parent");
    assertNotNull(parent);
  }

  @Test
  void testExpandMaxLevelsIgnoresNonExpandable() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents(CodePublisher='Eurostat',CodeID='LAU2',DivisionCode='33011')?$expand=Parent($levels=max)",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ObjectNode parent = (ObjectNode) division.get("Parent");
    assertNotNull(parent);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent);

    final JsonNode grandGrandParent = grandParent.get("Parent");
    assertTrue(grandGrandParent == null || grandGrandParent instanceof NullNode);
  }

  @Test
  void testExpandMaxLevelsIgnoresNonExpandable2() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents(CodePublisher='Eurostat',CodeID='NUTS3',DivisionCode='BE251')?$expand=Parent($levels=max),ActualParent($levels=max)",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ObjectNode parent = (ObjectNode) division.get("Parent");
    assertNotNull(parent);
    final ObjectNode grandParent = (ObjectNode) parent.get("Parent");
    assertNotNull(grandParent);

    final JsonNode grandGrandParent = grandParent.get("Parent");
    assertTrue(grandGrandParent == null || grandGrandParent instanceof NullNode);

    final ObjectNode actualParent = (ObjectNode) division.get("ActualParent");
    assertNotNull(actualParent);
    final ObjectNode actualGrandParent = (ObjectNode) actualParent.get("ActualParent");
    assertNotNull(actualGrandParent);
  }

  @Test
  void testExpandNavigationPathWithStar() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/AdministrativeInformation?$expand=*",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);

    final ObjectNode person = helper.getValue();
    final ObjectNode created = (ObjectNode) person.get("Created");
    assertNotNull(created);
    final ObjectNode user = (ObjectNode) created.get("User");
    assertNotNull(user);

    final ObjectNode updated = (ObjectNode) person.get("Updated");
    assertNotNull(updated);
    final ObjectNode user2 = (ObjectNode) updated.get("User");
    assertNotNull(user2);
  }

  @Test
  void testExpandStarViaExpand() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$expand = SupportEngineers($expand=*)",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);

    final ObjectNode organization = helper.getValue();
    assertNotNull(organization);
  }

  @Test
  void testExpandWithNavigationCountFilter() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/Children/$count eq 2 &$expand=Parent",
        new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(200);
    final ArrayNode divisions = helper.getValues();
    assertEquals(2, divisions.size());
    final ObjectNode parent = (ObjectNode) divisions.get(0).get("Parent");
    assertNotNull(parent);
  }

  @Test
  void testExpandWithPagingOneLevel() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    final JPAODataSkipTokenProvider skipTokens = mock(JPAODataSkipTokenProvider.class);
    when(provider.getFirstPage(any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataPage((UriInfoResource) i.getArguments()[2], 0, 2, "Hugo")));
    when(provider.getFirstPageExpand(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataExpandPage((UriInfoResource) i.getArguments()[2], 0, 4, skipTokens)));
    when(skipTokens.get(any())).thenAnswer(a -> {
      return "Expand";
    })

        .thenReturn("Expand");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq 'BE2'&$top=2&$expand=Children($top=10)",
        provider);
    helper.assertStatus(200);
    final ObjectNode division = (ObjectNode) helper.getValues().get(0);
    final ArrayNode children = (ArrayNode) division.get("Children");

    assertEquals(4, children.size());
    assertTrue(division.get("Children@odata.nextLink").asText().endsWith("Expand"));

  }

  @Test
  void testExpandWithPagingStarOneLevel() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    final JPAODataSkipTokenProvider skipTokens = mock(JPAODataSkipTokenProvider.class);
    when(provider.getFirstPage(any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataPage((UriInfoResource) i.getArguments()[2], 0, 2, "Hugo")));
    when(provider.getFirstPageExpand(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataExpandPage((UriInfoResource) i.getArguments()[2], 0, 4, skipTokens)));
    when(skipTokens.get(any())).thenAnswer(this::getSkipToken);
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq 'BE2'&$top=2&$expand=*",
        provider);
    helper.assertStatus(200);
    final ObjectNode division = (ObjectNode) helper.getValues().get(0);
    final ArrayNode children = (ArrayNode) division.get("Children");

    assertEquals(4, children.size());
    assertTrue(division.get("Children@odata.nextLink").asText().endsWith("Expand"));

  }

  @Test
  void testExpandWithPagingTwoLevel() throws IOException, ODataException {
    final var provider = createPagingProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq 'BE2'&$expand=Children($expand=Children)",
        provider);
    helper.assertStatus(200);
    final ObjectNode division = (ObjectNode) helper.getValues().get(0);
    final ArrayNode children = (ArrayNode) division.get("Children");

    assertEquals(4, children.size());
    assertTrue(division.get("Children@odata.nextLink").asText().endsWith("Expand"));
    for (final var subDivision : children) {
      final var nextLink = subDivision.get("Children@odata.nextLink");
      if (subDivision.get("DivisionCode").asText().equals("BE23"))
        assertTrue(nextLink.asText().endsWith("Second"));
      else
        assertNull(nextLink, "For " + subDivision.get("DivisionCode").asText());
    }

  }

  @Test
  void testExpandWithPagingTwoLevelTop() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq 'BE2'&$expand=Children($expand=Parent;$top=2)");
    helper.assertStatus(200);
    final ObjectNode division = (ObjectNode) helper.getValues().get(0);
    final ArrayNode children = (ArrayNode) division.get("Children");

    assertEquals(2, children.size());
    for (final var subDivision : children) {
      final var parentDivision = subDivision.get("Parent").get("DivisionCode");
      assertEquals("BE2", parentDivision.asText());
    }

  }

  @Test
  void testExpandLevels2WithPaging() throws IOException, ODataException {
    final var provider = createPagingProvider();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(CodePublisher='Eurostat',CodeID='NUTS1',DivisionCode='BE2')?$expand=Children($levels=2)",
        provider);

    helper.assertStatus(200);
    final ObjectNode division = helper.getValue();
    final ArrayNode children = (ArrayNode) division.get("Children");

    assertEquals(4, children.size());
    assertTrue(division.get("Children@odata.nextLink").asText().endsWith("Expand"));
    for (final var subDivision : children) {
      final var nextLink = subDivision.get("Children@odata.nextLink");
      if (subDivision.get("DivisionCode").asText().equals("BE23"))
        assertTrue(nextLink.asText().endsWith("Second"));
      else
        assertNull(nextLink);
    }
  }

  @Test
  void testExpandWithComplexWithPaging() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    final JPAODataSkipTokenProvider skipTokens = mock(JPAODataSkipTokenProvider.class);
    when(provider.getFirstPageExpand(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataExpandPage((UriInfoResource) i.getArguments()[2], 0, 2, skipTokens)));
    when(skipTokens.get(any())).thenAnswer(this::getSkipToken);

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)/Complex?$expand=OneToManyComplex",
        provider);

    helper.assertStatus(200);
    final ObjectNode complex = helper.getValue();
    assertEquals("JoinSources?$skiptoken=Complex/OneToManyComplex", complex.get("OneToManyComplex@odata.nextLink")
        .asText());
  }

  @Test
  void testExpandViaComplexWithPaging() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    final JPAODataSkipTokenProvider skipTokens = mock(JPAODataSkipTokenProvider.class);
    when(provider.getFirstPageExpand(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataExpandPage((UriInfoResource) i.getArguments()[2], 0, 2, skipTokens)));
    when(skipTokens.get(any())).thenAnswer(this::getSkipToken);

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "JoinSources(1)?$expand=Complex/OneToManyComplex",
        provider);

    helper.assertStatus(200);
    final ObjectNode result = helper.getValue();
    final ObjectNode complex = (ObjectNode) result.get("Complex");
    assertEquals("JoinSources?$skiptoken=Complex/OneToManyComplex", complex.get("OneToManyComplex@odata.nextLink")
        .asText());

  }

  @Test
  void testExpandMultipleParentChildReturnsConsistentResult() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('2')?$select=ID,Country&$expand=Roles($orderby=RoleCategory;$expand=BusinessPartner"
            + "($select=ID,Country;$expand=Roles($orderby=RoleCategory)))");
    helper.assertStatus(200);
    final ObjectNode result = helper.getValue();
    final String country = result.get("Country").asText();
    final ArrayNode roles = (ArrayNode) result.get("Roles");
    assertNotNull(roles);
    assertNotNull(country);
    for (final var role : roles) {
      final var partner = role.get("BusinessPartner");
      assertNotNull(partner);
      assertEquals("2", partner.get("ID").asText());
      assertEquals(country, partner.get("Country").asText());
      final ArrayNode roles2 = (ArrayNode) partner.get("Roles");
      assertNotNull(roles2);
      for (int i = 0; i < roles.size(); i++) {
        final var role2 = roles2.get(i);
        assertEquals(roles.get(i).get("BusinessPartnerID"), role2.get("BusinessPartnerID"));
        assertEquals(roles.get(i).get("RoleCategory"), role2.get("RoleCategory"));
      }
    }
  }

  @Test
  void testExpandFromInheritanceByJoin() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "InheritanceSavingAccounts?$select=AccountId&&$expand=Transactions");
    helper.assertStatus(200);
    final ArrayNode result = helper.getValues();
    assertEquals(3, result.size());

    for (var item : result) {
      var transactions = (ArrayNode) item.get("Transactions");
      if (item.get("AccountId").asText().equals("8ce66481d8114db0bbf7f0fc621dade7"))
        assertEquals(0, transactions.size());
      else if (item.get("AccountId").asText().equals("611b57e8b0784b169a0ccefc6379ff0a"))
        assertEquals(1, transactions.size());
      else if (item.get("AccountId").asText().equals("5425c3635eae4548bc82a7adddcad14d"))
        assertEquals(2, transactions.size());
      else
        fail();
    }
  }

  private JPAODataPagingProvider createPagingProvider() throws ODataApplicationException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    final JPAODataSkipTokenProvider skipTokens = mock(JPAODataSkipTokenProvider.class);
    when(provider.getFirstPage(any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataPage((UriInfoResource) i.getArguments()[2], 0, 2, "Hugo")));
    when(provider.getFirstPageExpand(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenAnswer(i -> Optional.of(new JPAODataExpandPage((UriInfoResource) i.getArguments()[2], 0, 4, skipTokens)));
    when(skipTokens.get(any())).thenAnswer(this::getSkipToken);
    return provider;
  }

  private Object getSkipToken(final InvocationOnMock a) {
    final var keys = a.getArgument(0, List.class);
    final var keyPath = ((JPAODataPageExpandInfo) keys.get(keys.size() - 1)).keyPath();
    return switch (keyPath) {
      case "Eurostat/NUTS1/BE2" -> "Expand";
      case "Eurostat/NUTS2/BE23" -> "Second";
      case "1" -> ((JPAODataPageExpandInfo) keys.get(keys.size() - 1)).navigationPropertyPath();
      default -> null;
    };
  }
}
