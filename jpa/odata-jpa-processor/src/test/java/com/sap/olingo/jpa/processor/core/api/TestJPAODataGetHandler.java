package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

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
    assertNotNull(cut.odata);
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
  public void testMappingPathInSessionContextCreatesMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setRequestMappingPath("/test")
        .build();
    cut = new JPAODataGetHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(isA(HttpServletRequestWrapper.class), any());
  }

  @Test
  public void testEmptyMappingPathInSessionContextNoMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();
    cut = new JPAODataGetHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(argThat(new HttpRequestMatcher()), any());
  }

  @Test
  public void testEmptyMappingPathInSessionContextEmptyMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataCRUDContextAccess context = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setRequestMappingPath("")
        .build();
    cut = new JPAODataGetHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(argThat(new HttpRequestMatcher()), any());
  }

  public static class HttpRequestMatcher implements ArgumentMatcher<HttpServletRequest> {
    @Override
    public boolean matches(final HttpServletRequest argument) {
      return argument instanceof HttpServletRequest && !(argument instanceof HttpServletRequestWrapper);
    }
  }
}
