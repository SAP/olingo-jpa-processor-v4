package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.junit.Test;
import org.mockito.Matchers;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public class TestJPACreateProcessor extends TestJPAModifyProcessor {

  @Test
  public void testHookIsCalled() throws ODataJPAModelException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertTrue(spy.called);
  }

  @Test
  public void testEntityTypeProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("Organization", spy.et.getExternalName());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAttributesProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();
    Map<String, Object> attributes = new HashMap<>(1);

    attributes.put("ID", "35");

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    when(convHelper.convertProperties(Matchers.any(OData.class), Matchers.any(JPAStructuredType.class), Matchers.any(
        List.class))).thenReturn(attributes);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.jpaAttributes);
    assertEquals(1, spy.jpaAttributes.size());
    assertEquals("35", spy.jpaAttributes.get("ID"));
  }

  @Test
  public void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  public void testThrowExpectedExceptionInCaseOfError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPARequestEntity.class), any(EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testThrowUnexpectedExceptionInCaseOfError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(NullPointerException.class).when(handler).createEntity(any(JPARequestEntity.class), any(
        EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testMinimalResponseLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  public void testMinimalResponseODataEntityIdHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.ODATA_ENTITY_ID));
  }

  @Test
  public void testMinimalResponseStatusCode() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testMinimalResponsePreferApplied() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("return=minimal", response.getHeader(HttpHeader.PREFERENCE_APPLIED));
  }

  @Test
  public void testRepresentationResponseStatusCode() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testRepresentationResponseStatusCodeMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testRepresentationResponseContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    byte[] act = new byte[100];
    response.getContent().read(act);
    String s = new String(act).trim();
    assertEquals("{\"ID\":\"35\"}", s);
  }

  @Test
  public void testRepresentationResponseContentMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    byte[] act = new byte[100];
    response.getContent().read(act);
    String s = new String(act).trim();
    assertEquals("{\"ID\":\"35\"}", s);
  }

  @Test
  public void testRepresentationLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  public void testRepresentationLocationHeaderMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  public void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);
    when(em.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(Boolean.TRUE);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPARequestEntity.class), any(EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      verify(handler, never()).validateChanges(em);
      return;
    }
    fail();
  }

  @Test
  public void testDoesRollbackIfValidateRaisesError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    when(em.getTransaction()).thenReturn(transaction);

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).validateChanges(em);

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      verify(transaction, never()).commit();
      verify(transaction, times(1)).rollback();
      return;
    }
    fail();
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public int noValidateCalls;
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;
    public Map<String, List<String>> headers;

    @Override
    public Object createEntity(final JPARequestEntity requestEntity, EntityManager em)
        throws ODataJPAProcessException {
      Organization result = new Organization();
      result.setID("35");
      this.et = requestEntity.getEntityType();
      this.jpaAttributes = requestEntity.getData();
      this.em = em;
      this.headers = requestEntity.getAllHeader();
      this.called = true;
      return result;
    }

    @Override
    public void validateChanges(final EntityManager em) throws ODataJPAProcessException {
      this.noValidateCalls++;
    }
  }

  class RequestHandleMapResultSpy extends JPAAbstractCUDRequestHandler {
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;

    @Override
    public Object createEntity(final JPARequestEntity requestEntity, EntityManager em)
        throws ODataJPAProcessException {
      Map<String, Object> result = new HashMap<>();
      result.put("iD", "35");
      this.et = requestEntity.getEntityType();
      this.jpaAttributes = requestEntity.getData();
      this.em = em;
      this.called = true;
      return result;
    }
  }
}
