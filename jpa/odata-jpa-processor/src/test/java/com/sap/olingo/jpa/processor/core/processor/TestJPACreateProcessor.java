package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public class TestJPACreateProcessor extends TestJPAModifyProcessor {

  @Test
  public void testHockIsCalled() throws ODataJPAModelException, ODataException {
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
    Map<String, Object> attributes = new HashMap<String, Object>(1);

    attributes.put("ID", "35");

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    when(helper.convertProperties(Matchers.any(OData.class), Matchers.any(JPAStructuredType.class), Matchers.any(
        List.class))).thenReturn(attributes);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.jpaAttributes);
    assertEquals(1, spy.jpaAttributes.size());
    assertEquals("35", spy.jpaAttributes.get("ID"));
  }

  @Test
  public void testThrowExpectedExceptionInCaseOfError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPAEntityType.class), anyMapOf(String.class,
            Object.class), any(EntityManager.class));

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

    doThrow(NullPointerException.class).when(handler).createEntity(any(JPAEntityType.class), anyMapOf(String.class,
        Object.class), any(EntityManager.class));

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
  public void testRepresentationLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;

    @Override
    public Object createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em)
        throws ODataJPAProcessException {
      Organization result = new Organization();
      result.setID("35");
      this.et = et;
      this.jpaAttributes = jpaAttributes;
      this.em = em;
      this.called = true;
      return result;
    }
  }
}
