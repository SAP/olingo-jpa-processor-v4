package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryOrderByClause extends TestBase {

  @Test
  void testOrderByOneProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Name1");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("Eighth Org.", orgs.get(0).get("Name1").asText());
    assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  void testOrderByOneComplexPropertyAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Address/Region");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("US-CA", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-UT", orgs.get(9).get("Address").get("Region").asText());
  }

  @Test
  void testOrderByOneComplexPropertyDesc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Address/Region desc");
    if (helper.getStatus() != 200)
      System.out.println(helper.getRawResult());
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
  }

  @Test
  void testOrderByTwoPropertiesDescAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=Address/Region desc,Name1 asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  void testOrderByTwoPropertiesDescDesc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=Address/Region desc,Name1 desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("First Org.", orgs.get(9).get("Name1").asText());
  }

  @Test
  void testOrderBy$CountDesc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Roles/$count desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(0).get("ID").asText());
    assertEquals("2", orgs.get(1).get("ID").asText());
  }

  @Test
  void testOrderBy$CountAndSelectAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID,Name1,Name2,Address/CountryName&$orderby=Roles/$count asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(9).get("ID").asText());
    assertEquals("2", orgs.get(8).get("ID").asText());
  }

  @Test
  void testCollectionOrderBy$CountAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$orderby=FirstLevel/SecondLevel/Comment/$count asc");

    helper.assertStatus(200);
    final ArrayNode deeps = helper.getValues();
    assertEquals("501", deeps.get(0).get("ID").asText());
    assertEquals("502", deeps.get(1).get("ID").asText());
  }

  @Test
  void testCollectionOrderBy$CountDesc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$orderby=FirstLevel/SecondLevel/Comment/$count desc");

    helper.assertStatus(200);
    final ArrayNode deeps = helper.getValues();
    assertEquals("502", deeps.get(0).get("ID").asText());
    assertEquals("501", deeps.get(1).get("ID").asText());
  }

  @Test
  void testOrderBy$CountAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=Roles/$count asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(9).get("ID").asText());
    assertEquals("2", orgs.get(8).get("ID").asText());
  }

  @Test
  void testOrderBy$CountDescComplexPropertyAcs() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=Roles/$count desc,Address/Region desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals("3", orgs.get(0).get("ID").asText());
    assertEquals("2", orgs.get(1).get("ID").asText());
    assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
    assertEquals("6", orgs.get(9).get("ID").asText());
  }

  @Test
  void testOrderByAndFilter() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodeID eq 'NUTS' or CodeID eq '3166-1'&$orderby=CountryCode desc");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
    assertEquals("USA", orgs.get(0).get("CountryCode").asText());
  }

  @Test
  void testOrderByAndTopSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodeID eq 'NUTS' or CodeID eq '3166-1'&$orderby=CountryCode desc&$top=1&$skip=2");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("CHE", orgs.get(0).get("CountryCode").asText());
  }

  @Test
  void testOrderByNavigationOneHop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/Roles?$orderby=RoleCategory desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
    assertEquals("C", orgs.get(0).get("RoleCategory").asText());
  }

  @Test
  void testOrderByGroupedPropertyWithoutGroup() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$orderby=Country desc");
    helper.assertStatus(403);
  }

  @Test
  void testOrderByPropertyWithGroupsOneGroup() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Person");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$orderby=Country desc", groups);
    helper.assertStatus(200);
  }

  @Test
  void testOrderByGroupedComplexPropertyWithoutGroup() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$orderby=Address/Country desc");
    helper.assertStatus(403);
  }

  @Test
  void testOrderByGroupedComplexPropertyWithGroupsOneGroup() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$orderby=Address/Country desc", groups);
    helper.assertStatus(200);
  }

  @Test
  void testOrderByOnTransientPrimitveSimpleProperty() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$orderby=FullName", groups);
    helper.assertStatus(501);
  }

  @Test
  void testOrderByOnTransientSimpleComplexPartProperty() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$orderby=Address/Street", groups);
    helper.assertStatus(501);
  }

  @Test
  void testOrderByOnTransientCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$orderby=FirstLevel/TransientCollection/$count asc");
    helper.assertStatus(501);
  }
}
