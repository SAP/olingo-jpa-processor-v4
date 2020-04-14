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

public class TestJPAClearProcessor extends TestJPAModifyProcessor {
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
  public void testSuccessReturnCode() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    ODataResponse response = new ODataResponse();

    prepareDeleteName2();
    processor.clearFields(request, response);
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testHockIsCalled() throws ODataApplicationException {
    // .../Organizations('35')/Name2

    RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertTrue(spy.called);
  }

  @Test
  public void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.clearFields(request, new ODataResponse());

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  public void testClaimsProvided() throws ODataJPAProcessorException, SerializerException,
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
  public void testGroupsProvided() throws ODataJPAProcessorException, SerializerException,
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
  public void testSimplePropertyEntityTypeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertEquals("Organization", spy.et.getExternalName());
  }

  @Test
  public void testSimplePropertyKeyProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    RequestHandleSpy spy = prepareDeleteName2();

    List<UriParameter> keys = new ArrayList<>();
    UriParameter uriParam = mock(UriParameter.class);
    when(uriParam.getText()).thenReturn("'35'");
    when(uriParam.getName()).thenReturn("ID");
    keys.add(uriParam);

    when(uriEts.getKeyPredicates()).thenReturn(keys);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.keyPredicates.size());
    assertEquals("35", spy.keyPredicates.get("iD"));
  }

  @Test
  public void testSimplePropertyAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    RequestHandleSpy spy = prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("name2", keys[0].toString());
  }

  @Test
  public void testSimpleCollectionPropertyAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Comment
    RequestHandleSpy spy = prepareDeleteComment();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("comment", keys[0].toString());
  }

  @Test
  public void testComplexPropertyHoleProvided() throws ODataApplicationException {
    // .../Organizations('35')/Address
    RequestHandleSpy spy = prepareDeleteAddress();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("address", keys[0].toString());
  }

  @Test
  public void testSimplePropertyValueAttributeProvided() throws ODataApplicationException {
    // .../Organizations('35')/Name2/$value
    RequestHandleSpy spy = prepareDeleteName2();

    UriResourceValue uriProperty;
    uriProperty = mock(UriResourceValue.class);
    pathParts.add(uriProperty);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());
    Object[] keys = spy.jpaAttributes.keySet().toArray();
    assertEquals("name2", keys[0].toString());
  }

  @Test
  public void testComplexPropertyOnePropertyProvided() throws ODataApplicationException {
    // .../Organizations('35')/Address/Country
    RequestHandleSpy spy = prepareDeleteAddressCountry();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    @SuppressWarnings("unchecked")
    Map<String, Object> address = (Map<String, Object>) spy.jpaAttributes.get("address");
    assertEquals(1, address.size());
    Object[] keys = address.keySet().toArray();
    assertEquals("country", keys[0].toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTwoComplexPropertiesOnePropertyProvided() throws ODataApplicationException {
    // .../Organizations('4')/AdministrativeInformation/Updated/By
    RequestHandleSpy spy = prepareDeleteAdminInfo();

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    Map<String, Object> adminInfo = (Map<String, Object>) spy.jpaAttributes.get("administrativeInformation");
    assertEquals(1, adminInfo.size());
    Map<String, Object> update = (Map<String, Object>) adminInfo.get("updated");
    assertEquals(1, update.size());
    Object[] keys = update.keySet().toArray();
    assertEquals("by", keys[0].toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTwoComplexPropertiesOnePropertyValueProvided() throws ODataApplicationException {
    // .../Organizations('4')/AdministrativeInformation/Updated/By/$value
    RequestHandleSpy spy = prepareDeleteAdminInfo();

    UriResourceValue uriProperty;
    uriProperty = mock(UriResourceValue.class);
    pathParts.add(uriProperty);

    processor.clearFields(request, new ODataResponse());
    assertEquals(1, spy.jpaAttributes.size());

    Map<String, Object> adminInfo = (Map<String, Object>) spy.jpaAttributes.get("administrativeInformation");
    assertEquals(1, adminInfo.size());
    Map<String, Object> update = (Map<String, Object>) adminInfo.get("updated");
    assertEquals(1, update.size());
    Object[] keys = update.keySet().toArray();
    assertEquals("by", keys[0].toString());
  }

  @Test
  public void testBeginIsCalledOnNoTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());

    verify(factory, times(1)).createTransaction();
  }

  @Test
  public void testBeginIsNotCalledOnTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();
    when(factory.hasActiveTransaction()).thenReturn(true);

    processor.clearFields(request, new ODataResponse());

    verify(factory, times(0)).createTransaction();
  }

  @Test
  public void testCommitIsCalledOnNoTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();

    processor.clearFields(request, new ODataResponse());

    verify(transaction, times(1)).commit();
  }

  @Test
  public void testCommitIsNotCalledOnTransaction() throws ODataApplicationException {
    // .../Organizations('35')/Name2
    prepareDeleteName2();
    when(factory.hasActiveTransaction()).thenReturn(true);

    processor.clearFields(request, new ODataResponse());

    verify(transaction, times(0)).commit();
  }

  @Test
  public void testErrorReturnCodeWithRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    ODataResponse response = new ODataResponse();

    RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(1);
    ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
    verify(transaction, times(1)).rollback();
  }

  @Test
  public void testErrorReturnCodeWithOutRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    ODataResponse response = new ODataResponse();

    RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(1);
    when(factory.hasActiveTransaction()).thenReturn(true);

    ODataApplicationException act = assertThrows(ODataApplicationException.class, () -> processor.clearFields(request,
        response));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
    verify(transaction, times(0)).rollback();
  }

  @Test
  public void testReraiseWithRollback() throws ODataJPATransactionException {
    // .../Organizations('35')/Name2
    ODataResponse response = new ODataResponse();

    RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(2);

    ODataJPAProcessException act = assertThrows(ODataJPAProcessException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
    verify(transaction, times(1)).rollback();
  }

  @Test
  public void testReraiseReturnCodeWithOutRollback() throws ODataJPAProcessException {
    // .../Organizations('35')/Name2
    ODataResponse response = new ODataResponse();

    RequestHandleSpy spy = prepareDeleteName2();
    spy.raiseException(2);
    when(factory.hasActiveTransaction()).thenReturn(true);
    ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class,
        () -> processor.clearFields(request, response));

    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
    verify(transaction, times(0)).rollback();
  }

  @Test
  public void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.clearFields(request, response);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(factory.hasActiveTransaction()).thenReturn(Boolean.TRUE);

    processor.clearFields(request, response);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();
    when(request.getMethod()).thenReturn(HttpMethod.POST);

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).updateEntity(any(JPARequestEntity.class), any(EntityManager.class),
            any(HttpMethod.class));

    assertThrows(ODataApplicationException.class, () -> processor.clearFields(request, response));
    verify(handler, never()).validateChanges(em);

  }

  @Test
  public void testDoesRollbackIfValidateRaisesError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
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

    RequestHandleSpy spy = new RequestHandleSpy();
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

    RequestHandleSpy spy = new RequestHandleSpy();
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

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    return spy;
  }

  private RequestHandleSpy prepareDeleteAddressCountry() {
    RequestHandleSpy spy = prepareDeleteAddress();

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

    RequestHandleSpy spy = new RequestHandleSpy();
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

    public void raiseException(int type) {
      this.raiseEx = type;

    }

    @Override
    public void validateChanges(final EntityManager em) throws ODataJPAProcessException {
      noValidateCalls++;
    }
  }
}
