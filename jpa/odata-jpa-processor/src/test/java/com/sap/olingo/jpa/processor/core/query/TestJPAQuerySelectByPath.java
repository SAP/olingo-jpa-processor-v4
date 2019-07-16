package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class TestJPAQuerySelectByPath extends TestBase {

  @Test
  public void testNavigationToOwnPrimitiveProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Name1");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("Third Org.", org.get("value").asText());
  }

  @Test
  public void testNavigationToOwnEmptyPrimitiveProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('99')/BirthDay");
    helper.assertStatus(204);
  }

  @Test
  public void testNavigationToOwnPrimitivePropertyEntityDoesNotExistEntity() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('9999')/BirthDay");
    helper.assertStatus(404);
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
  public void testNavigationToNotExistingComplexProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('97')/CommunicationData");
    helper.assertStatus(204);
  }

  @Test
  public void testNavigationToNestedComplexProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/AdministrativeInformation/Created");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("98", org.get("By").asText());
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

  @Test
  public void testNavigationToComplexPropertyExpand() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("USA", org.get("Country").asText());
  }

  @Test
  public void testNavigationToComplexPrimitiveProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/Address/Region");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    assertEquals("US-CA", org.get("value").asText());
    assertTrue(org.get("@odata.context").asText().endsWith("$metadata#Organizations/Address/Region"));
  }

  @Test
  public void testNavigationToCollcetion() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('1')/Comment");
    helper.assertStatus(200);
  }

  @Test
  public void testNavigationToCollectionWithoutEntries() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Comment");
    helper.assertStatus(200);

    ObjectNode org = helper.getValue();
    ArrayNode act = (ArrayNode) org.get("value");
    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  public void testNavigationToSimplePrimitiveGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('1')/Country");
    helper.assertStatus(204);
  }

  @Test
  public void testNavigationToSimpleComplexGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('1')/CommunicationData");
    helper.assertStatus(204);
  }

  @Test
  public void testNavigationToCollcetionGroupedPropertyNoGroups() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss('1')/Comment");
    helper.assertStatus(200);
  }

  @Test
  public void testNoNavigationButGroupsWithoutGoup() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss?$top=1");
    helper.assertStatus(200);

    final ObjectNode act = (ObjectNode) helper.getValue().get("value").get(0);
    assertPresentNotNull(act, "ETag");
    assertPresentButNull(act, "CreationDateTime");
    assertPresentButNull(act, "Country");
    assertPresentButAllNull(act, "CommunicationData");
    assertPresentButNull(act, "Comment");
  }

  @Test
  public void testNoNavigationButGroupsWithOneGoup() throws IOException, ODataException {
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Person");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "BusinessPartnerWithGroupss?$top=1", groups);
    helper.assertStatus(200);

    ObjectNode act = (ObjectNode) helper.getValue().get("value").get(0);
    assertPresentNotNull(act, "ETag");
    assertPresentButNull(act, "CreationDateTime");
    assertPresentNotNull(act, "Country");
    assertPresentNotNull(act, "CommunicationData");
    assertPresentButNull(act, "Comment");
  }

  @Test
  public void testNavigationToStreamValue() throws IOException, ODataException {
    new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "PersonImages('99')/$value");
    helper.assertStatus(200);

    byte[] act = helper.getBinaryResult();
    assertEquals(93316, act.length, 0);
  }

  @Test
  public void testNavigationToStreamValueVia() throws IOException, ODataException {
    new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('99')/Image/$value");
    helper.assertStatus(200);

    byte[] act = helper.getBinaryResult();
    assertEquals(93316, act.length, 0);
  }

  @Test
  public void testNavigationToComplexAttributeValue() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/AdministrativeInformation/Created/By/$value");
    helper.assertStatus(200);

    String act = helper.getRawResult();
    assertEquals("98", act);
  }

  @Test
  public void testNavigationToPrimitiveAttributeValue() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('4')/ID/$value");
    helper.assertStatus(200);

    String act = helper.getRawResult();
    assertEquals("4", act);
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
      assertFalse(((ArrayNode) target).size() == 0);
    else
      assertFalse(act.get(property).isNull());
  }
}
