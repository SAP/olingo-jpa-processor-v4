package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.HttpServletRequestDouble;
import com.sap.olingo.jpa.processor.core.util.HttpServletResponseDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;

public class TestJPAODataGetHandler extends TestBase {
  private JPAODataGetHandler cut;
  private HttpServletRequestDouble request;
  private HttpServletResponseDouble response;
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  @BeforeEach
  public void setup() throws IOException {
    request = new HttpServletRequestDouble("http://localhost:8080/Test/Olingo.svc/Organizations", new StringBuffer(),
        headers);
    response = new HttpServletResponseDouble();
  }

  @Test
  public void testCanCreateInstanceWithPunit() throws ODataException {
    assertNotNull(new JPAODataGetHandler(PUNIT_NAME));
  }

  @Test
  public void testPropertiesInstanceWithPunit() throws ODataException {
    cut = new JPAODataGetHandler(PUNIT_NAME);
    assertNotNull(cut.getJPAODataContext());
    assertNull(cut.ds);
    assertNull(cut.emf);
    assertNotNull(cut.odata);
    assertNotNull(cut.namespace);
  }

  @Test
  public void testCanCreateInstanceWithPunitAndDs() throws ODataException {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    assertNotNull(new JPAODataGetHandler(PUNIT_NAME, ds));
  }

  @Test
  public void testPropertiesInstanceWithPunitAndDs() throws ODataException {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    cut = new JPAODataGetHandler(PUNIT_NAME, ds);
    assertNotNull(cut.getJPAODataContext());
    assertNotNull(cut.ds);
    assertNotNull(cut.emf);
    assertNotNull(cut.odata);
    assertNotNull(cut.namespace);
    assertNotNull(cut.jpaMetamodel);
  }

  @Test
  public void testProcessWithoutEntityManager() throws ODataException {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    cut = new JPAODataGetHandler(PUNIT_NAME, ds);
    cut.getJPAODataContext().setTypePackage(enumPackages);
    cut.process(request, response);
    assertNotNull(cut.jpaMetamodel);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testProcessWithEntityManager() throws ODataException {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    final EntityManager em = emf.createEntityManager();
    cut = new JPAODataGetHandler(PUNIT_NAME, ds);
    cut.getJPAODataContext().setTypePackage(enumPackages);
    cut.process(request, response, em);
    assertNotNull(cut.jpaMetamodel);
    assertEquals(200, response.getStatus());

  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProcessWithClaims() throws ODataException {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    final EntityManager em = emf.createEntityManager();
    final JPAODataClaimProvider claims = new JPAODataClaimsProvider();
    cut = new JPAODataGetHandler(PUNIT_NAME, ds);
    cut.getJPAODataContext().setTypePackage(enumPackages);
    cut.process(request, response, claims, em);
    assertNotNull(cut.jpaMetamodel);
    assertEquals(200, response.getStatus());

  }

  @Test
  public void testGetSessionContext() throws ODataException {
    cut = new JPAODataGetHandler(PUNIT_NAME);
    assertNotNull(cut.getJPAODataContext());
  }

  @Test
  public void testGetRequestContext() throws ODataException {
    cut = new JPAODataGetHandler(PUNIT_NAME);
    assertNotNull(cut.getJPAODataRequestContext());
  }

  @Test
  public void testInitDebugger() throws IOException, ODataException {
    request.setDebugFormat(DebugSupport.ODATA_DEBUG_JSON);
    cut = new JPAODataGetHandler(PUNIT_NAME, ds);
    cut.getJPAODataContext().setTypePackage(enumPackages);
    cut.process(request, response);
    final JPAServiceDebugger act = ((JPAODataSessionContextAccess) cut.getJPAODataContext()).getDebugger();
    assertNotNull(act);
    assertFalse(act instanceof JPAEmptyDebugger);
  }
}
