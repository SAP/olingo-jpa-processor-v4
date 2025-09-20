package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.processor.core.api.JPAODataApiVersionAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.util.Assertions;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryTopSkip extends TestBase {

  @Test
  void testTop() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$top=10");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(10, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31003", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(243, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31022", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testTopSkip() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5&$top=5");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(5, act.size());
    assertEquals("Eurostat", act.get(0).get("CodePublisher").asText());
    assertEquals("LAU2", act.get(0).get("CodeID").asText());
    assertEquals("31022", act.get(0).get("DivisionCode").asText());
  }

  @Test
  void testTopReturnsAllIfToLarge() throws IOException, ODataException {
    final OData odata = OData.newInstance();

    final var sessionContext = JPAODataServiceContext.with()
        .setPUnit(IntegrationTestHelper.PUNIT_NAME)
        .setEntityManagerFactory(emf)
        .setRequestMappingPath("bp/v1")
        .setTypePackage(TestBase.enumPackages)
        .build();

    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(externalContext.getClaimsProvider()).thenReturn(Optional.empty());
    when(externalContext.getGroupsProvider()).thenReturn(Optional.empty());
    when(externalContext.getDebuggerSupport()).thenReturn(new DefaultDebugSupport());
    when(externalContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    final var requestContext = new JPAODataInternalRequestContext(externalContext, sessionContext, odata);

    final var handler = odata.createHandler(odata.createServiceMetadata(sessionContext
        .getApiVersion(JPAODataApiVersionAccess.DEFAULT_VERSION).getEdmProvider(),
        new ArrayList<>()));

    final var request = IntegrationTestHelper.getRequestMock(IntegrationTestHelper.URI_PREFIX + "Persons?$top=5000");
    final var response = IntegrationTestHelper.getResponseMock();
    handler.register(new JPAODataRequestProcessor(sessionContext, requestContext));
    handler.process(request, response);

    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode value = (ObjectNode) mapper.readTree(getRawResult(response));
    assertNull(value.get("@odata.nextLink"));

  }

  @Tag(Assertions.CB_ONLY_TEST)
  @Test
  void testExpandTopSkipWithoutError() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$skip=5&$top=5&$expand=Children");
    helper.assertStatus(200);
    final ObjectNode collection = helper.getValue();
    final ArrayNode act = ((ArrayNode) collection.get("value"));
    assertEquals(5, act.size());
  }

  public String getRawResult(final HttpServletResponse response) throws IOException {
    final InputStream in = asInputStream(response);
    final StringBuilder builder = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = reader.readLine()) != null) {
      builder.append(read);
    }
    reader.close();
    return builder.toString();
  }

  public InputStream asInputStream(final HttpServletResponse response) throws IOException {
    return new IntegrationTestHelper.ResultStream((IntegrationTestHelper.OutPutStream) response.getOutputStream());
  }

}
