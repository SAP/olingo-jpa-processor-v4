package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class TestJPACustomScalarFunctions {

  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPADefaultEdmNameBuilder nameBuilder;
  protected static DataSource ds;

  @BeforeAll
  public static void setupClass() throws ODataJPAModelException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    CreateDensityFunction();
  }

  @AfterAll
  public static void tearDownClass() throws ODataJPAModelException {
    DropDensityFunction();
  }

  @Test
  void testFilterOnFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population) gt 1");
    helper.assertStatus(200);
  }

  @Test
  void testFilterOnFunctionAndProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population)  mul 1000000 gt 1000 and ParentDivisionCode eq 'BE255'&orderBy=DivisionCode)");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
    assertEquals("35002", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterOnFunctionAndMultiply() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=Area,Population=Population)  mul 1000000 gt 100");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(59, orgs.size());
  }

  @Test
  void testFilterOnFunctionWithFixedValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=13079087,Population=$it/Population)  mul 1000000 gt 1000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(29, orgs.size());
  }

  @Test
  void testFilterOnFunctionComputedValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=Area div 1000000,Population=Population) gt 1000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(7, orgs.size());
  }

  @Test
  void testFilterOnFunctionMixParamOrder() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Population=Population,Area=Area) mul 1000000 gt 1000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(7, orgs.size());
  }

  private static void CreateDensityFunction() {
    final EntityManager em = emf.createEntityManager();
    final EntityTransaction t = em.getTransaction();

    final StringBuffer sqlString = new StringBuffer();

    sqlString.append(
        "CREATE FUNCTION  \"OLINGO\".\"PopulationDensity\" (UnitArea  INT, Population BIGINT ) ");
    sqlString.append("RETURNS DOUBLE ");
    sqlString.append("BEGIN ATOMIC  "); //
    sqlString.append("  DECLARE aDouble DOUBLE; "); //
    sqlString.append("  DECLARE pDouble DOUBLE; ");
    sqlString.append("  SET aDouble = UnitArea; ");
    sqlString.append("  SET pDouble = Population; ");
    sqlString.append("  IF UnitArea <= 0 THEN RETURN 0; ");
    sqlString.append("  ELSE RETURN pDouble  / aDouble; "); // * 1000000
    sqlString.append("  END IF;  "); //
    sqlString.append("END");

    t.begin();
    final Query q = em.createNativeQuery(sqlString.toString());
    q.executeUpdate();
    t.commit();
  }

  private static void DropDensityFunction() {
    final EntityManager em = emf.createEntityManager();
    final EntityTransaction t = em.getTransaction();

    final StringBuffer sqlString = new StringBuffer();

    sqlString.append("DROP FUNCTION  \"OLINGO\".\"PopulationDensity\"");

    t.begin();
    final Query q = em.createNativeQuery(sqlString.toString());
    q.executeUpdate();
    t.commit();
  }
}
