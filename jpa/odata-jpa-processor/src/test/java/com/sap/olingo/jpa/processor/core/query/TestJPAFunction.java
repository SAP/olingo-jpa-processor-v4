package com.sap.olingo.jpa.processor.core.query;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class TestJPAFunction {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static DataSource ds;
  protected static boolean functionCreated;

  protected TestHelper helper;
  protected Map<String, List<String>> headers;

  @BeforeEach
  void setup() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    final Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.nonJtaDataSource", ds);
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    emf.getProperties();
    createFunction();
  }

  @Disabled("The segment of an action or of a non-composable function must be the last resource-path segment.")
  @Test
  void testNavigationAfterFunctionNotAllowed() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')/Parent");
    helper.assertStatus(501);
  }

  @Test
  void testFunctionGenerateQueryString() throws IOException, ODataException, SQLException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')");
    helper.assertStatus(200);
  }

  private void createFunction() {
    if (!functionCreated) {
      final EntityManager em = emf.createEntityManager();
      final EntityTransaction t = em.getTransaction();
      final StringBuilder createSiblingsString = new StringBuilder();
      createSiblingsString.append(
          "CREATE FUNCTION  \"OLINGO\".\"Siblings\" (\"Publisher\" VARCHAR(10), \"ID\" VARCHAR(10), \"Division\" VARCHAR(10)) ");
      createSiblingsString.append(
          "RETURNS TABLE(\"CodePublisher\" VARCHAR(10),\"CodeID\" VARCHAR(10),\"DivisionCode\" VARCHAR(10),");
      createSiblingsString.append(
          "\"CountryISOCode\" VARCHAR(4), \"ParentCodeID\" VARCHAR(10),\"ParentDivisionCode\" VARCHAR(10),");
      createSiblingsString.append("\"AlternativeCode\" VARCHAR(10),\"Area\" int, \"Population\" BIGINT) ");
      createSiblingsString.append("READS SQL DATA ");
      createSiblingsString.append("RETURN TABLE( SELECT * FROM \"AdministrativeDivision\" as a  WHERE ");
      createSiblingsString.append("EXISTS (SELECT \"CodePublisher\" ");
      createSiblingsString.append("FROM \"OLINGO\".\"AdministrativeDivision\" as b ");
      createSiblingsString.append("WHERE b.\"CodeID\" = \"ID\" ");
      createSiblingsString.append("AND   b.\"DivisionCode\" = \"Division\" ");
      createSiblingsString.append("AND   b.\"CodePublisher\" = a.\"CodePublisher\" ");
      createSiblingsString.append("AND   b.\"ParentCodeID\" = a.\"ParentCodeID\" ");
      createSiblingsString.append("AND   b.\"ParentDivisionCode\" = a.\"ParentDivisionCode\") ");
      createSiblingsString.append("AND NOT( a.\"CodePublisher\" = \"Publisher\" ");
      createSiblingsString.append("AND  a.\"CodeID\" = \"ID\" ");
      createSiblingsString.append("AND  a.\"DivisionCode\" = \"Division\" )); ");
      t.begin();
      final Query qP = em.createNativeQuery(createSiblingsString.toString());
      qP.executeUpdate();
      t.commit();
      functionCreated = true;
    }
  }
}
