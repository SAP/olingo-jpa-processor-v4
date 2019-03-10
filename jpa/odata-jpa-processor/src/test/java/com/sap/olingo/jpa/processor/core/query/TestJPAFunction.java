package com.sap.olingo.jpa.processor.core.query;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJPAFunction {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static DataSource ds;

  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPAEdmNameBuilder nameBuilder;

  @Before
  public void setup() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    Map<String, Object> properties = new HashMap<>();
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

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')");
    helper.assertStatus(200);
  }
}
