package org.apache.olingo.jpa.processor.core.query;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestJPAFunction {
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static DataSource ds;

  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPAEdmNameBuilder nameBuilder;

  @Before
  public void setup() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("javax.persistence.nonJtaDataSource", ds);
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    emf.getProperties();
  }

  @Ignore // TODO check is path is in general allowed
  @Test
  public void testNavigationAfterFunctionNotAllowed() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')/Parent");
    helper.assertStatus(501);
  }

  @Test
  public void testFunctionGenerateQueryString() throws IOException, ODataException, SQLException {

    createSiblingsFunction();
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')");
    helper.assertStatus(200);
  }

  private void createSiblingsFunction() {
    StringBuffer sqlString = new StringBuffer();

    EntityManager em = emf.createEntityManager();
    EntityTransaction t = em.getTransaction();

    sqlString.append("create function \"OLINGO\".\"org.apache.olingo.jpa::Siblings\""); // \"OLINGO\".
    sqlString.append("( CodePublisher nvarchar(10), CodeID nvarchar(10), DivisionCode nvarchar(10))");
    sqlString.append(
        "RETURNS TABLE (\"CodePublisher\" nvarchar(10), \"CodeID\" nvarchar(10), \"DivisionCode\" nvarchar(10),");
    sqlString.append(
        "\"CountryISOCode\"  NVARCHAR(4), \"ParentCodeID\"  NVARCHAR(10), \"ParentDivisionCode\"  NVARCHAR(10),");
    sqlString.append("\"AlternativeCode\"  NVARCHAR(10),  \"Area\"  DECIMAL(34,0), \"Population\"  BIGINT )");
    sqlString.append("READS SQL  DATA RETURN TABLE (SELECT ");
    sqlString.append("a.\"CodePublisher\", a.\"CodeID\", a.\"DivisionCode\", a.\"CountryISOCode\",a.\"ParentCodeID\"");
    sqlString.append(",a.\"ParentDivisionCode\", a.\"AlternativeCode\",a.\"Area\", a.\"Population\"");
    sqlString.append("FROM \"OLINGO\".\"org.apache.olingo.jpa::AdministrativeDivision\" as a);");

    t.begin();
    javax.persistence.Query q = em.createNativeQuery(sqlString.toString());
    q.executeUpdate();
    t.commit();
  }
}
