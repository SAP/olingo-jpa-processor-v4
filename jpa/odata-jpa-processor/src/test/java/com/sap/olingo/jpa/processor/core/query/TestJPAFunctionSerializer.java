package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class TestJPAFunctionSerializer {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static DataSource ds;

  protected TestHelper helper;
  protected Map<String, List<String>> headers;

  @BeforeEach
  void setup() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    final Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.nonJtaDataSource", ds);
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    emf.getProperties();
  }

  @Test
  void testFunctionReturnsEntityType() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "EntityType(A=1250)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    assertNotNull(r.get("Area"));
    assertEquals(1250, r.get("Area").asInt());
  }

  @Test
  void testFunctionReturnsEntityTypeNull() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "EntityType(A=0)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(204);
  }

  @Test
  void testFunctionReturnsEntityTypeCollection() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ListOfEntityType(A=1250)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    final ArrayNode values = (ArrayNode) r.get("value");
    assertNotNull(values.get(0));
    assertNotNull(values.get(0).get("Area"));
    assertEquals(1250, values.get(0).get("Area").asInt());
    assertNotNull(values.get(1));
    assertNotNull(values.get(1).get("Area"));
    assertEquals(625, values.get(1).get("Area").asInt());
  }

  @Test
  void testFunctionReturnsPrimitiveType() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "PrimitiveValue(A=124)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    assertNotNull(r);
    assertNotNull(r.get("value"));
    assertEquals(124, r.get("value").asInt());
  }

  @Test
  void testFunctionReturnsPrimitiveTypeNull() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "PrimitiveValue(A=0)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(204);
  }

  @Test
  void testFunctionReturnsPrimitiveTypeCollection() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ListOfPrimitiveValues(A=124)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ArrayNode r = helper.getValues();
    assertNotNull(r);
    assertNotNull(r.get(0));
    assertEquals(124, r.get(0).asInt());
    assertNotNull(r.get(1));
    assertEquals(62, r.get(1).asInt());
  }

  @Test
  void testFunctionReturnsComplexType() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ComplexType(A=124)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    assertNotNull(r);
    assertNotNull(r.get("LandlinePhoneNumber"));
    assertEquals(124, r.get("LandlinePhoneNumber").asInt());
  }

  @Test
  void testFunctionReturnsComplexTypeNull() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ComplexType(A=0)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(204);
  }

  @Test
  void testFunctionReturnsComplexTypeCollection() throws IOException, ODataException, SQLException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ListOfComplexType(A='Willi')",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ArrayNode r = helper.getValues();
    assertNotNull(r);
    assertNotNull(r.get(0));
    assertNotNull(r.get(0).get("Created"));
  }

  @Test
  void testUsesConverter() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ConvertBirthday()",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
  }

  @Test
  void testFunctionReturnsEntityTypeWithCollection() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "ListOfEntityTypeWithCollection(A=1250)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    assertNotNull(r.get("value"));
    final ObjectNode person = (ObjectNode) r.get("value").get(0);
    final ArrayNode addr = (ArrayNode) person.get("InhouseAddress");
    assertEquals(2, addr.size());
  }

  @Test
  void testFunctionReturnsEntityTypeWithDeepCollection() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds, "EntityTypeWithDeepCollection(A=1250)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ObjectNode r = helper.getValue();
    assertNotNull(r);
  }

}
