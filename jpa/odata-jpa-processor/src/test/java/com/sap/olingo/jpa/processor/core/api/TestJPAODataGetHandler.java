package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
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
    assertFalse(cut.emf.isPresent());
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
  public void testGetHandlerProvidingContext() throws ODataException {
    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();
    cut = new JPAODataGetHandler(context);
    assertNotNull(cut);
  }

  @Test
  public void testGetRequestContextProvidingSessionContext() throws ODataException {
    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds).setPUnit(PUNIT_NAME).build();
    cut = new JPAODataGetHandler(context);
    assertNotNull(cut.getJPAODataRequestContext());
  }

  @Test
  public void testPropertiesInstanceProvidingSessionContext() throws ODataException {

    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds).setPUnit(PUNIT_NAME).build();
    cut = new JPAODataGetHandler(context);
    assertNull(cut.ds);
    assertNotNull(cut.odata);
    assertNull(cut.namespace);
  }

  @Test
  public void testProcessOnlyProvidingSessionContext() throws ODataException {

    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    new JPAODataGetHandler(context).process(request, response);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testProcessWithEntityManagerProvidingSessionContext() throws ODataException {

    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    cut = new JPAODataGetHandler(context);
    cut.getJPAODataRequestContext().setEntityManager(emf.createEntityManager());
    cut.process(request, response);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testProcessOnlyProvidingSessionContextWithEm() throws ODataException {

    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    cut = new JPAODataGetHandler(context);
    cut.getJPAODataRequestContext().setEntityManager(emf.createEntityManager());
    cut.process(request, response);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testProcessWithEm() throws ODataException {
    cut = new JPAODataGetHandler(PUNIT_NAME);
    cut.getJPAODataContext().setTypePackage(enumPackages);
    cut.process(request, response, emf.createEntityManager());
    assertEquals(200, response.getStatus());
  }

}
