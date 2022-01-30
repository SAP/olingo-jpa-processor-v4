package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.testmodel.ImageLoader;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQuerySelectByPath extends TestBase {

  @Test
  void testNavigationToOwnPrimitiveProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Name1");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("Third Org.", org.get("value").asText());
  }

  @Test
  void testNavigationToOwnEmptyPrimitiveProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('98')/BirthDay");
    helper.assertStatus(204);
  }

  @Test
  void testNavigationToOwnPrimitivePropertyEntityDoesNotExistEntity() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('9999')/BirthDay");
    helper.assertStatus(404);
  }

  @Test
  void testNavigationToOwnPrimitiveDescriptionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/LocationName");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("Vereinigte Staaten von Amerika", org.get("value").asText());
  }

  @Test
  void testNavigationToComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("USA", org.get("Country").asText());
  }

  @Test
  void testNavigationToNotExistingComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('97')/CommunicationData");
    helper.assertStatus(204);
  }

  @Test
  void testNavigationToNestedComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/AdministrativeInformation/Created");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("98", org.get("By").asText());
  }

  @Test
  void testNavigationViaComplexAndNaviPropertyToPrimitive() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('3')/AdministrativeInformation/Created/User/FirstName");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("Max", org.get("value").asText());
  }

  @Test
  void testNavigationToComplexPropertySelect() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/Address?$select=Country,Region");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals(3, org.size()); // Node "@odata.context" is also counted
    assertEquals("USA", org.get("Country").asText());
    assertEquals("US-UT", org.get("Region").asText());
  }

  @Test
  void testNavigationToComplexPropertyExpand() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("USA", org.get("Country").asText());
  }

  @Test
  void testNavigationToComplexPrimitiveProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/Address/Region");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    assertEquals("US-CA", org.get("value").asText());
    assertTrue(org.get("@odata.context").asText().endsWith("$metadata#Organizations/Address/Region"));
  }

  @Test
  void testNavigationToCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/Comment");
    helper.assertStatus(200);
  }

  @Test
  void testNavigationToCollectionWithoutEntries() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Comment");
    helper.assertStatus(200);

    final ObjectNode org = helper.getValue();
    final ArrayNode act = (ArrayNode) org.get("value");
    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  void testNavigationToSimplePrimitiveGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('1')/Country");
    helper.assertStatus(204);
  }

  @Test
  void testNavigationToSimpleComplexGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('1')/CommunicationData");
    helper.assertStatus(204);
  }

  @Test
  void testNavigationToCollectionComplexGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('99')/InhouseAddress");
    helper.assertStatus(200);
    final ArrayNode act = (ArrayNode) helper.getValue().get("value");
    assertEquals(2, act.size());
    final ObjectNode addr = (ObjectNode) act.get(0);
    assertFalse(addr.get("TaskID").isNull());
    assertTrue(addr.get("RoomNumber").isNull());
  }

  @Test
  void testNavigationToCollcetionGroupedPropertyNoGroups() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('1')/Comment");
    helper.assertStatus(200);
    // Ensure empty result works correct
    helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('1')/Comment");
    helper.assertStatus(200);
  }

  @Test
  void testNoNavigationButGroupsWithoutGroup() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('99')");
    helper.assertStatus(200);

    final ObjectNode act = helper.getValue();
    assertPresentNotNull(act, "ETag");
    assertPresentButNull(act, "CreationDateTime");
    assertPresentButNull(act, "Country");
    assertPresentButAllNull(act, "CommunicationData");
    assertPresentNotEmpty(act, "InhouseAddress", "RoomNumber");
    assertPresentButNull(act, "Comment");
  }

  @Test
  void testNoNavigationButGroupsWithOneGroup() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Person");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('99')", groups);
    helper.assertStatus(200);

    final ObjectNode act = helper.getValue();
    assertPresentNotNull(act, "ETag");
    assertPresentButNull(act, "CreationDateTime");
    assertPresentNotNull(act, "Country");
    assertPresentNotNull(act, "CommunicationData");
    assertPresentNotEmpty(act, "InhouseAddress", "RoomNumber");
    assertPresentButNull(act, "Comment");
  }

  @Test
  void testNavigationToStreamValue() throws IOException, ODataException {
    new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "PersonImages('99')/$value");
    helper.assertStatus(200);

    final byte[] act = helper.getBinaryResult();
    assertEquals(93316, act.length, 0);
  }

  @Test
  void testNavigationToStreamValueVia() throws IOException, ODataException {
    new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('99')/Image/$value");
    helper.assertStatus(200);

    final byte[] act = helper.getBinaryResult();
    assertEquals(93316, act.length, 0);
  }

  @Test
  void testNavigationToComplexAttributeValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/AdministrativeInformation/Created/By/$value");
    helper.assertStatus(200);

    final String act = helper.getRawResult();
    assertEquals("98", act);
  }

  @Test
  void testNavigationToPrimitiveAttributeValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/ID/$value");
    helper.assertStatus(200);

    final String act = helper.getRawResult();
    assertEquals("4", act);
  }

  @Test
  void testNavigationToDerivedEntities() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartners/com.sap.olingo.jpa.Person");

    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();

    assertEquals(3, act.size());
  }

  @Test
  void testNavigationToDerivedEntityRestrictDerived() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartners/com.sap.olingo.jpa.Person('98')");

    helper.assertStatus(200);
    final ObjectNode pers = helper.getValue();
    assertEquals("98", pers.get("ID").asText());
  }

  @Test
  void testNavigationToDerivedEntityRestrictBase() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartners('98')/com.sap.olingo.jpa.Person");

    helper.assertStatus(200);
    final ObjectNode person = helper.getValue();
    assertEquals("98", person.get("ID").asText());
  }

  @Test
  void testNavigationToDerivedEntityNotFound() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartners('1')/com.sap.olingo.jpa.Person");

    helper.assertStatus(404);
  }

  private void assertPresentButNull(final ObjectNode act, final String property) {
    assertTrue(act.has(property));
    final JsonNode target = act.get(property);
    if (target instanceof ArrayNode)
      assertEquals(0, ((ArrayNode) target).size());
    else
      assertTrue(target.isNull());
  }

  private void assertPresentButAllNull(final ObjectNode act, final String property) {
    assertTrue(act.has(property));
    final JsonNode target = act.get(property);
    if (target instanceof ObjectNode) {
      ((ObjectNode) target).forEach(n -> assertTrue(n.isNull()));
    } else {
      assertTrue(target.isNull());
    }
  }

  private void assertPresentNotNull(final ObjectNode act, final String property) {
    assertTrue(act.has(property));
    final JsonNode target = act.get(property);
    if (target instanceof ArrayNode)
      assertNotEquals(0, ((ArrayNode) target).size());
    else
      assertFalse(act.get(property).isNull());
  }

  private void assertPresentNotEmpty(final ObjectNode act, final String property, final String nullProperty) {
    assertTrue(act.has(property));
    final JsonNode target = act.get(property);
    if (target instanceof ArrayNode) {
      assertTrue(((ArrayNode) target).size() > 0);
      ((ArrayNode) target).forEach(n -> assertFalse(n.isNull()));
      assertTrue(target.get(0).get(nullProperty).isNull());
    } else {
      fail();
    }
  }
}
