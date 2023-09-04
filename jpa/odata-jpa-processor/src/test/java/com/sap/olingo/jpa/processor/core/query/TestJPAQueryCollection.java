package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryCollection extends TestBase {

  @Test
  void testSelectPropertyAndCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$select=ID,Comment&orderby=ID");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    final ObjectNode organization = (ObjectNode) organizations.get(0);
    assertNotNull(organization.get("ID"));
    final ArrayNode comment = (ArrayNode) organization.get("Comment");
    assertEquals(2, comment.size());
  }

  // @Ignore // See https://issues.apache.org/jira/browse/OLINGO-1231
  @Test
  void testSelectPropertyOfCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/InhouseAddress?$select=Building");
    helper.assertStatus(200);

    final ArrayNode buildings = helper.getValues();
    assertEquals(2, buildings.size());
    final ObjectNode building = (ObjectNode) buildings.get(0);
    final TextNode number = (TextNode) building.get("Building");
    assertNotNull(number);
  }

  @Test
  void testSelectAllWithComplexCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')?$select=*");
    helper.assertStatus(200);

    final ObjectNode person = helper.getValue();
    final ArrayNode comment = (ArrayNode) person.get("InhouseAddress");
    assertEquals(2, comment.size());
  }

  @Test
  void testSelectAllWithPrimitiveCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$select=*");
    helper.assertStatus(200);

    final ObjectNode person = helper.getValue();
    final ArrayNode comment = (ArrayNode) person.get("Comment");
    assertEquals(2, comment.size());
  }

  @Test
  void testSelectWithNestedComplexCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Collections('504')?$select=Nested");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode nested = (ArrayNode) collection.get("Nested");
    assertEquals(1, nested.size());
    final ObjectNode n = (ObjectNode) nested.get(0);
    assertEquals(1L, n.get("Number").asLong());
    assertFalse(n.get("Inner") instanceof NullNode);
    assertEquals(6L, n.get("Inner").get("Figure3").asLong());
  }

  @Test
  void testSelectComplexContainingCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Collections('502')?$select=Complex");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ObjectNode complex = (ObjectNode) collection.get("Complex");
    assertEquals(32L, complex.get("Number").asLong());
    assertFalse(complex.get("Address") instanceof NullNode);
    assertEquals(2, complex.get("Address").size());
    for (int i = 0; i < complex.get("Address").size(); i++) {
      final ObjectNode address = (ObjectNode) complex.get("Address").get(i);
      if (address.get("Building").asText().equals("1")) {
        assertEquals("DEV", address.get("TaskID").asText());
        return;
      }
    }
    fail("Task not found");
  }

  @Test
  void testSelectComplexContainingTwoCollections() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Collections('501')?$select=Complex");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ObjectNode complex = (ObjectNode) collection.get("Complex");
    assertEquals(-1L, complex.get("Number").asLong());
    assertFalse(complex.get("Address") instanceof NullNode);
    assertEquals(1, complex.get("Address").size());
    assertEquals("MAIN", complex.get("Address").get(0).get("TaskID").asText());
    assertFalse(complex.get("Comment") instanceof NullNode);
    assertEquals(1, complex.get("Comment").size());
    assertEquals("This is another test", complex.get("Comment").get(0).asText());
  }

  @Test
  void testSelectAllWithComplexContainingCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Collections('502')");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ObjectNode complex = (ObjectNode) collection.get("Complex");
    assertEquals(32L, complex.get("Number").asLong());
    assertFalse(complex.get("Address") instanceof NullNode);
    assertEquals(2, complex.get("Address").size());
    for (int i = 0; i < complex.get("Address").size(); i++) {
      final ObjectNode address = (ObjectNode) complex.get("Address").get(i);
      if (address.get("Building").asText().equals("1")) {
        assertEquals("DEV", address.get("TaskID").asText());
        return;
      }
    }
    fail("Task not found");
  }

  @Test
  void testSelectAllDeepComplexContainingCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "CollectionDeeps('501')");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ObjectNode complex = (ObjectNode) collection.get("FirstLevel");
    assertEquals(1, complex.get("LevelID").asInt());
    assertFalse(complex.get("SecondLevel") instanceof NullNode);
    final ObjectNode second = (ObjectNode) complex.get("SecondLevel");
    final ArrayNode address = (ArrayNode) second.get("Address");
    assertEquals(32, address.get(0).get("RoomNumber").asInt());
  }

  @Test
  void testSelectOnlyOneCollectionDeepComplex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps('502')?$select=FirstLevel/SecondLevel/Comment");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final TextNode actId = (TextNode) collection.get("ID");
    assertEquals("502", actId.asText());
    final ObjectNode complex = (ObjectNode) collection.get("FirstLevel");
    assertFalse(complex.get("SecondLevel") instanceof NullNode);
    final ObjectNode second = (ObjectNode) complex.get("SecondLevel");
    final ArrayNode comment = (ArrayNode) second.get("Comment");
    assertEquals(2, comment.size());
  }

  @Test
  void testSelectOnlyNoCollectionDeepComplex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps('501')?$select=FirstLevel/SecondLevel/Number");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final TextNode actId = (TextNode) collection.get("ID");
    assertEquals("501", actId.asText());
    final ObjectNode complex = (ObjectNode) collection.get("FirstLevel");
    assertFalse(complex.get("SecondLevel") instanceof NullNode);
    final ObjectNode second = (ObjectNode) complex.get("SecondLevel");
    final IntNode number = (IntNode) second.get("Number");
    assertEquals(-1, number.asInt());
  }

  @Test
  void testSelectCollectionWithoutRequiredGroup() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('1')?$select=Comment");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode act = (ArrayNode) collection.get("Comment");
    assertEquals(0, act.size());

  }

  @Test
  void testSelectCollectionWithRequiredGroup() throws IOException, ODataException {
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('1')?$select=Comment", groups);
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode act = (ArrayNode) collection.get("Comment");
    assertEquals(2, act.size());
  }

  @Test
  void testSelectCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')?$select=Comment");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode act = (ArrayNode) collection.get("Comment");
    assertEquals(2, act.size());
  }

  @Test
  void testSelectCollectionWithTop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=Comment&$top=2");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(2, act.size());
    assertEquals(2, act.get(0).get("Comment").size());
    assertEquals("1", act.get(0).get("ID").asText());
  }

  @Test
  void testSelectCollectionWithTopAndOrderBy() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=Comment&$top=2&orderby=Name1");
    helper.assertStatus(200);

    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(2, act.size());
    assertEquals(2, act.get(0).get("Comment").size());
  }

  @Test
  void testPathWithTransientCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "CollectionDeeps('501')/FirstLevel");
    helper.assertStatus(200);
    // CollectionDeep.class
    final ObjectNode complex = helper.getValue();
    assertEquals(1, complex.get("LevelID").asInt());
    assertFalse(complex.get("SecondLevel") instanceof NullNode);
    assertEquals(2, ((ArrayNode) complex.get("TransientCollection")).size());
  }

  @Test
  void testPathToTransientCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps('501')/FirstLevel/TransientCollection"); // SecondLevel/Address"); //
    helper.assertStatus(200);
    // CollectionDeep.class
    final ObjectNode result = helper.getValue();
    final ArrayNode collection = (ArrayNode) result.get("value");
    assertEquals(2, collection.size());
  }

  @Test
  void testPathToTransientCollectionWoRequired() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionWithTransients('501')/TransientComment");
    helper.assertStatus(200);
    // CollectionDeep.class
    final ObjectNode result = helper.getValue();
    final ArrayNode collection = (ArrayNode) result.get("value");
    assertEquals(2, collection.size());
  }

  @Test
  void testPathWithCollections() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps('501')/FirstLevel/SecondLevel");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode complex = (ArrayNode) collection.get("Address");
    assertEquals(1, complex.size());
  }

  @Test
  void testPathToCollectionWithTransient() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionWithTransients('501')/Nested"); // $select=Log");
    helper.assertStatus(200);
    final ObjectNode result = helper.getValue();
    final ArrayNode collection = (ArrayNode) result.get("value");
    assertEquals(2, collection.size());
    assertFalse(collection.get(0).get("Log") instanceof NullNode);
  }

  @Test
  void testSelectTransientOfCollection() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionWithTransients('501')/Nested$select=Log");
    helper.assertStatus(200);
    final ObjectNode result = helper.getValue();
    final ArrayNode collection = (ArrayNode) result.get("value");
    assertEquals(2, collection.size());
    assertFalse(collection.get(0).get("Log") instanceof NullNode);
  }

  @Test
  void testSelectCollectionContainsTransient() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionWithTransients('501')");
    helper.assertStatus(200);
  }

  @Test
  void testPathToCollections() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps('501')/FirstLevel/SecondLevel/Address");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode complex = (ArrayNode) collection.get("value");
    assertEquals(1, complex.size());
  }

  @Test
  void testSelectCollectionCountSimpleProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations('1')/Comment/$count");
    helper.assertStatus(200);
    final ValueNode count = helper.getSingleValue();
    assertEquals(2, count.asInt());
  }

  @Test
  void testSelectCollectionCountComplexProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/InhouseAddress/$count");
    helper.assertStatus(200);
    final ValueNode count = helper.getSingleValue();
    assertEquals(2, count.asInt());
  }

  @Test
  void testSelectCollectionCountComplexPropertySingleton() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CurrentUser/InhouseAddress/$count");
    helper.assertStatus(200);
    final ValueNode count = helper.getSingleValue();
    assertEquals(1, count.asInt());
  }
}
