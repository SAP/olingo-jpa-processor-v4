package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestJPACustomScalarFunctions {

  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPAEdmNameBuilder nameBuilder;
  protected static DataSource ds;

  @BeforeClass
  public static void setupClass() throws ODataJPAModelException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    nameBuilder = new JPAEdmNameBuilder(PUNIT_NAME);
    CreateDenfityFunction();
  }

  @AfterClass
  public static void tearDownClass() throws ODataJPAModelException {
    DropDenfityFunction();
  }

  @Test
  public void testFilterOnFunction() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=org.apache.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population) gt 1");
    helper.assertStatus(204);
  }

  @Test
  public void testFilterOnFunctionAndProperty() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=org.apache.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population)  mul 1000000 gt 1000 and ParentDivisionCode eq 'BE255'&orderBy=DivisionCode)");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
    assertEquals("35002", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  public void testFilterOnFunctionAndMultiply() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=org.apache.olingo.jpa.PopulationDensity(Area=Area,Population=Population)  mul 1000000 gt 100");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(59, orgs.size());
  }

  @Test
  public void testFilterOnFunctionWithFixedValue() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=org.apache.olingo.jpa.PopulationDensity(Area=13079087,Population=$it/Population)  mul 1000000 gt 100");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  private static void CreateDenfityFunction() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction t = em.getTransaction();

    // StringBuffer dropString = new StringBuffer("DROP FUNCTION PopulationDensity");

    StringBuffer sqlString = new StringBuffer();

    sqlString.append(
        "CREATE FUNCTION  \"OLINGO\".\"org.apache.olingo.jpa::PopulationDensity\" (UnitArea  INT, Population BIGINT ) ");
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
    // Query d = em.createNativeQuery(dropString.toString());
    Query q = em.createNativeQuery(sqlString.toString());
    // d.executeUpdate();
    q.executeUpdate();
    t.commit();
  }

  private static void DropDenfityFunction() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction t = em.getTransaction();

    StringBuffer sqlString = new StringBuffer();

    sqlString.append("DROP FUNCTION  \"OLINGO\".\"org.apache.olingo.jpa::PopulationDensity\"");

    t.begin();
    // Query d = em.createNativeQuery(dropString.toString());
    Query q = em.createNativeQuery(sqlString.toString());
    // d.executeUpdate();
    q.executeUpdate();
    t.commit();
  }
}
