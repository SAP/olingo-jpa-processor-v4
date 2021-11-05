package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;

class JPAClearProcessorTest extends TestJPAModifyProcessor {
  private ODataRequest request;

  @Override
  @BeforeEach
  public void setup() throws Exception {
    super.setup();
    request = mock(ODataRequest.class);
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());
  }

  @Test
  void testSuccessReturnCode() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    final ODataResponse response = new ODataResponse();

    prepareDeleteName2();
    processor.clearFields(request, response);
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  void testHockIsCalled() throws ODataApplicationException {
    // .../Organizations('35')/Name2

    final RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertTrue(spy.called);
  }

  @Test
  void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.clearFields(request, new ODataResponse());

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  void testClaimsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataClaimProvider provider = new JPAODataClaimsProvider();
    final Optional<JPAODataClaimProvider> claims = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getClaimsProvider()).thenReturn(claims);

    processor.clearFields(request, new ODataResponse());

    assertNotNull(spy.claims);
    assertTrue(spy.claims.isPresent());
    assertEquals(provider, spy.claims.get());
  }

  @Test
  void testGroupsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataGroupsProvider provider = new JPAODataGroupsProvider();
    provider.addGroup("Person");
    // final List<String> groups = new ArrayList<>(Arrays.asList("Person"));
    final Optional<JPAODataGroupProvider> groups = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getGroupsProvider()).thenReturn(groups);

    processor.clearFields(request, new ODataResponse());

    assertNotNull(spy.groups);
    assertFalse(spy.groups.isEmpty());
    assertEquals("Person", spy.groups.get(0));
  }

  @Test
  void testSimplePropertyEntityTypeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    final RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertEquals("Organization", spy.et.getExternalName());
  }

  @Test
  void testSimplePropertyKeyProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    final RequestHandleSpy spy = prepareDeleteName2();

    final List<UriParameter> keys = new ArrayList<>();
    final UriParameter uriParam = mock(UriParameter.class);
    when(uriParam.getText()).thenReturn("'35'");
    when(uriParam.getName()).thenReturn("ID");
    keys.add(uriParam);

    when(uriEts.getKeyPredicates()).thenReturn(keys);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.keyPredicates.size());
    assertEquals("35", spy.keyPredicates.get("iD"));
  }

  @Test
  void testSimplePropertyAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    final RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    final Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("name2", keys[0].toString());
  }

  @Test
  void testSimpleCollectionPropertyAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Comment
    final RequestHandleSpy spy = prepareDeleteComment();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    final Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("comment", keys[0].toString());
  }

  @Test
  void testComplexPropertyHoleProvided() throws ODataApplicationException {
    // .../Organizations('35')/Address
    final RequestHandleSpy spy = prepareDeleteAddress();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    final Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("address", keys[0].toString());
  }

  @Test
  void testSimplePropertyValueAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2/$value
    final RequestHandleSpy spy = prepareDeleteName2();

    UriResourceValue uriProperty;
    uriProperty = mock(UriResourceValue.class);
    pathParts.add(uriProperty);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    final Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("name2", keys[0].toString());
  }

  @Test
  void testComplexPropertyOnePropertyProvided() throws ODataApplicationException {
    // .../Organizations('35')/Address/Country
    final RequestHandleSpy spy = prepareDeleteAddressCountry();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    @SuppressWarnings("unchecked")
    final Map<String, Object> address = (Map<String, Object>) spy.jpaAttributes.get("address");
    assertEquals(1, address.size());
    final Object[] keys = address.keySet().toArray();
    assertEquals("country", keys[0].toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testTwoComplexPropertiesOnePropertyProvided() throws ODataApplicationException {
    // .../Organizations('4')/AdministrativeInformation/Updated/By
    final RequestHandleSpy spy = prepareDeleteAdminInfo();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    final Map<String, Object> adminInfo = (Map<String, Object>) spy.jpaAttributes.get("administrativeInformation");
    assertEquals(1, adminInfo.size());
    final Map<String, Object> update = (Map<String, Object>) adminInfo.get("updated");
    assertEquals(1, update.size());
    final Object[] keys = update.keySet().toArray();
    assertEquals("by", keys[0].toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testTwoComplexPropertiesOnePropertyValueProvided() throws ODataApplicationException {
    // .../Organizations('4')/AdministrativeInformation/Updated/By/$value
    final RequestHandleSpy spy = prepareDeleteAdminInfo();

    UriResourceValue uriProperty;
    uriProperty = mock(UriResourceValue.class);
    pathParts.add(uriProperty);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    final Map<String, Object> adminInfo = (Map<String, Object>) spy.jpaAttributes.get("administrativeInformation");
    assertEquals(1, adminInfo.size());
    final Map<String, Object> update = (Map<String, Object>) adminInfo.get("updated");
    assertEquals(1, update.size());
    final Object[] keys = update.keySet().toArray();
    assertEquals("by", keys[0].toString());
  }

  @Test
  void testBeginIsCalledOnNoTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());

    verify(factory, times(1)).createTransaction();
  }

  @Test
  void testBeginIsNotCalledOnTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();
    when(factory.hasActiveTransaction()).thenReturn(true);

    processor.clearFields(request, new ODataResponse());

    verify(factory, times(0)).createTransaction();
  }

  @Test
  void testCommitIsCalledOnNoTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());

    verify(transaction, times(1)).commit();
  }

  @Test
  void testCommitIsNotCalledOnTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();
    when(factory.hasActiveTransaction()).thenReturn(true);

    processor.clearFields(request, new ODataResponse());

    verify(transaction, times(0)).commit();
  }

  @Test
  void testErrorReturnCodeWithRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    final ODataResponse response = new ODataResponse();

    final RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(1);
    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testErrorReturnCodeWithOutRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    final ODataResponse response = new ODataResponse();

    final RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(1);
    when(factory.hasActiveTransaction()).thenReturn(true);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class, () -> processor.clearFields(
        request,
        response));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
    verify(transaction, times(0)).rollback();
  }

  @Test
  void testReraiseWithRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    final ODataResponse response = new ODataResponse();

    final RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(2);

    final ODataJPAProcessException act = assertThrows(ODataJPAProcessException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testReraiseReturnCodeWithOutRollback() throws ODataJPAProcessException {
    // .../Organizations('35')/Name2
    final ODataResponse response = new ODataResponse();

    final RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(2);
    when(factory.hasActiveTransaction()).thenReturn(true);
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
    verify(transaction, times(0)).rollback();
  }

  @Test
  void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.clearFields(request, response);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(factory.hasActiveTransaction()).thenReturn(Boolean.TRUE);

    processor.clearFields(request, response);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();
    when(request.getMethod()).thenReturn(HttpMethod.POST);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).updateEntity(any(JPARequestEntity.class), any(EntityManager.class),
            any(HttpMethod.class));

    assertThrows(ODataApplicationException.class, () -> processor.clearFields(request, response));
    verify(handler, never()).validateChanges(em);

  }

  @Test
  void testDoesRollbackIfValidateRaisesError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).validateChanges(em);

    assertThrows(ODataApplicationException.class, () -> processor.clearFields(request, response));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  private RequestHandleSpy prepareDeleteName2() {

    UriResourcePrimitiveProperty uriProperty;
    EdmProperty property;
    uriProperty = mock(UriResourcePrimitiveProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Name2");

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    return spy;
  }

  private RequestHandleSpy prepareDeleteComment() {

    UriResourcePrimitiveProperty uriProperty;
    EdmProperty property;
    uriProperty = mock(UriResourcePrimitiveProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Comment");

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    return spy;
  }

  private RequestHandleSpy prepareDeleteAddress() {

    UriResourceComplexProperty uriProperty;
    EdmProperty property;
    uriProperty = mock(UriResourceComplexProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Address");

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    return spy;
  }

  private RequestHandleSpy prepareDeleteAddressCountry() {
    final RequestHandleSpy spy = prepareDeleteAddress();

    UriResourcePrimitiveProperty uriProperty;
    EdmProperty property;
    uriProperty = mock(UriResourcePrimitiveProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Country");

    return spy;
  }

  private RequestHandleSpy prepareDeleteAdminInfo() {

    UriResourceComplexProperty uriProperty;
    EdmProperty property;
    uriProperty = mock(UriResourceComplexProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("AdministrativeInformation");

    uriProperty = mock(UriResourceComplexProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriProperty);
    when(uriProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Updated");

    UriResourcePrimitiveProperty uriPrimProperty;
    uriPrimProperty = mock(UriResourcePrimitiveProperty.class);
    property = mock(EdmProperty.class);

    pathParts.add(uriPrimProperty);
    when(uriPrimProperty.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("By");

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    return spy;
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public int noValidateCalls;
    public Map<String, Object> keyPredicates;
    public Map<String, Object> jpaAttributes;
    public JPAEntityType et;
    public boolean called;
    public Map<String, List<String>> headers;
    private int raiseEx;
    public Optional<JPAODataClaimProvider> claims;
    public List<String> groups;

    @Override
    public JPAUpdateResult updateEntity(final JPARequestEntity requestEntity, final EntityManager em,
        final HttpMethod verb) throws ODataJPAProcessException {

      this.et = requestEntity.getEntityType();
      this.keyPredicates = requestEntity.getKeys();
      this.jpaAttributes = requestEntity.getData();
      this.headers = requestEntity.getAllHeader();
      this.claims = requestEntity.getClaims();
      this.groups = requestEntity.getGroups();
      called = true;

      if (raiseEx == 1)
        throw new NullPointerException();
      if (raiseEx == 2)
        throw new ODataJPAProcessorException(MessageKeys.NOT_SUPPORTED_DELETE, HttpStatusCode.NOT_IMPLEMENTED);
      return null;
    }

    void raiseException(final int type) {
      this.raiseEx = type;

    }

    @Override
    public void validateChanges(final EntityManager em) throws ODataJPAProcessException {
      noValidateCalls++;
    }
  }
}
