package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.database.JPADerbySqlPatternProvider;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;

/**
 * Some databases, like derby do not support LIMIT OFFSET and return an Integer on $count
 * <p>
 * <p>
 * 2024-06-30
 */
class TestTopSkipCountOnDerby {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static EntityManagerFactory emf;
  private static DataSource dataSource;

  @BeforeAll
  public static void setupClass() {
    dataSource = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, dataSource);
  }

  @Test
  void testTop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$top=10", new JPADerbySqlPatternProvider());
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(10, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31003", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5", new JPADerbySqlPatternProvider());
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(243, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31022", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testTopSkip() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$top=10&$skip=5", new JPADerbySqlPatternProvider());
    helper.assertStatus(200);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "AdministrativeDivisions/$count",
      "Persons?$expand=Roles/$count",
      "Persons('97')/InhouseAddress/$count" })
  void testCount(final String query) throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, query);
    helper.assertStatus(200);
  }
}
