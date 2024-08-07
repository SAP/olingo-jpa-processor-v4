package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;

class TestJPAQueryOrderByClause {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  public static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  protected static EntityManagerFactory emf;
  protected Map<String, List<String>> headers;
  protected static JPAEdmNameBuilder nameBuilder;
  protected static DataSource dataSource;

  @BeforeAll
  public static void setupClass() {
    dataSource = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, dataSource);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
  }

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
  void testOrderByOneComplexPropertyDeepAsc() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$orderby=AdministrativeInformation/Created/By");
    helper.assertStatus(200);
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
    assertEquals("506", deeps.get(0).get("ID").asText());
    assertEquals("501", deeps.get(1).get("ID").asText());
    assertEquals("502", deeps.get(2).get("ID").asText());
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
        "Organizations?$select=ID&$orderby=Roles/$count asc");
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
  void testOrderByOnTransientPrimitiveSimpleProperty() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$orderby=FullName", groups);
    helper.assertStatus(400);
  }

  @Test
  void testOrderByOnTransientSimpleComplexPartProperty() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$orderby=Address/Street", groups);
    helper.assertStatus(400);
  }

  @Test
  void testOrderByOnTransientCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$orderby=FirstLevel/TransientCollection/$count asc");
    helper.assertStatus(400);
  }

  @Test
  void testOrderByToOneProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AssociationOneToOneSources?$orderby=ColumnTarget/Source asc");
    helper.assertStatus(200);
  }

  @Test
  void testOrderByToOnePropertyViaComplex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$orderby=AdministrativeInformation/Created/User/LastName asc");
    helper.assertStatus(200);
  }

  @Test
  void testOrderByToOnePropertyWithCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$expand=BusinessPartner($expand=Roles;$select=ID)&$orderby=BusinessPartner/Country asc");
    helper.assertStatus(200);
  }

  @Test
  void testOrderByToTwoPropertyWithCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$orderby=BusinessPartner/Country asc,BusinessPartner/ID asc");
    helper.assertStatus(200);
  }

  @Test
  void testOrderByDescriptionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$orderby=LocationName asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(10, orgs.size());
    assertEquals("10", orgs.get(0).get("ID").asText());
  }

  @Test
  void testOrderByDescriptionViaToOneProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$orderby=Organization/LocationName desc");
    helper.assertStatus(200);
  }

}
