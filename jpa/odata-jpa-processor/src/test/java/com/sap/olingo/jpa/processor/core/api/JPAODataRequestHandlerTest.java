package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import com.sap.olingo.jpa.processor.core.api.mapper.JakartaRequestMapper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class JPAODataRequestHandlerTest extends TestBase {
  private JPAODataRequestHandler cut;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  @BeforeEach
  void setup() throws IOException {
    request = IntegrationTestHelper.getRequestMock("http://localhost:8080/Test/Olingo.svc/Organizations",
        new StringBuilder(), headers);
    response = IntegrationTestHelper.getResponseMock();
  }

  @Test
  void testGetHandlerProvidingSessionContext() throws ODataException {
    final JPAODataSessionContextAccess sessionContext = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .build();
    cut = new JPAODataRequestHandler(sessionContext);
    assertNotNull(cut);
  }

  @Test
  void testPropertiesInstanceProvidingSessionContext() throws ODataException {

    final JPAODataSessionContextAccess context = JPAODataServiceContext.with()
        .setDataSource(dataSource).setPUnit(PUNIT_NAME).build();
    cut = new JPAODataRequestHandler(context);
    assertNotNull(cut.odata);
  }

  @Test
  void testProcessOnlyProvidingSessionContext() throws ODataException {

    final JPAODataSessionContextAccess context = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    new JPAODataRequestHandler(context).process(request, response);
    assertEquals(200, getStatus());
  }

  @Test
  void testProcessWithEntityManagerProvidingSessionContext() throws ODataException {

    final JPAODataSessionContextAccess sessionContext = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    cut = new JPAODataRequestHandler(sessionContext);
    // cut.getJPAODataRequestContext().setEntityManager(emf.createEntityManager());
    cut.process(request, response);
    assertEquals(200, getStatus());
  }

  @Test
  void testProcessOnlyProvidingSessionContextWithEm() throws ODataException {

    final JPAODataSessionContextAccess sessionContext = JPAODataServiceContext.with()
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    final JPAODataRequestContext requestContext = JPAODataRequestContext.with()
        .setEntityManager(emf.createEntityManager())
        .build();
    cut = new JPAODataRequestHandler(sessionContext, requestContext);
    cut.process(request, response);
    assertEquals(200, getStatus());
  }

  @Test
  void testMappingPathInSessionContextCreatesMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataSessionContextAccess context = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .setRequestMappingPath("/test")
        .build();
    cut = new JPAODataRequestHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(argThat(new WrappedHttpRequestMatcher()), any());
  }

  @Test
  void testEmptyMappingPathInSessionContextNoMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataSessionContextAccess context = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .build();
    cut = new JPAODataRequestHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(argThat(new HttpRequestMatcher()), any());
  }

  @Test
  void testEmptyMappingPathInSessionContextEmptyMapper() throws ODataException {
    final OData odata = mock(OData.class);
    final ODataHttpHandler handler = mock(ODataHttpHandler.class);
    when(odata.createHandler(any())).thenReturn(handler);
    final JPAODataSessionContextAccess context = JPAODataServiceContext.with()
        .setDataSource(dataSource)
        .setPUnit(PUNIT_NAME)
        .setRequestMappingPath("")
        .build();
    cut = new JPAODataRequestHandler(context, odata);
    cut.process(request, response);
    verify(handler, times(1)).process(argThat(new HttpRequestMatcher()), any());
  }

  public static class HttpRequestMatcher implements ArgumentMatcher<javax.servlet.http.HttpServletRequest> {
    @Override
    public boolean matches(final javax.servlet.http.HttpServletRequest argument) {
      if (argument instanceof JakartaRequestMapper) {
        final HttpServletRequest wrapped = ((JakartaRequestMapper) argument).getWrapped();
        return wrapped instanceof HttpServletRequest && !(wrapped instanceof HttpServletRequestWrapper);
      } else
        return false;
    }
  }

  public static class WrappedHttpRequestMatcher implements ArgumentMatcher<javax.servlet.http.HttpServletRequest> {
    @Override
    public boolean matches(final javax.servlet.http.HttpServletRequest argument) {
      if (argument instanceof JakartaRequestMapper) {
        final HttpServletRequest wrapped = ((JakartaRequestMapper) argument).getWrapped();
        return wrapped instanceof HttpServletRequestWrapper;
      } else
        return false;
    }
  }

  public int getStatus() {
    final ArgumentCaptor<Integer> acStatus = ArgumentCaptor.forClass(Integer.class);
    verify(response).setStatus(acStatus.capture());
    return acStatus.getValue();
  }
}
