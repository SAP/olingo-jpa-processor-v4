package org.apache.olingo.jpa.processor.core.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestJPAFunction extends TestBase {

  @Before
  public void setup() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("javax.persistence.nonJtaDataSource", DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HANA));
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    emf.getProperties();
  }

  @Ignore // TODO check is path is in general allowed
  @Test
  public void testNavigationAfterFunctionNotAllowed() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')/Parent");
    helper.assertStatus(501);
  }

  @Ignore
  @Test
  public void testFunctionGenerateQueryString() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Siblings(DivisionCode='BE25',CodeID='NUTS2',CodePublisher='Eurostat')");
    helper.assertStatus(200);
  }
}
