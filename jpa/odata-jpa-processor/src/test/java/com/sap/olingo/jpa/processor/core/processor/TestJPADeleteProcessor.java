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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
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
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

public class TestJPADeleteProcessor extends TestJPAModifyProcessor {

  @Test
  public void testSuccessReturnCode() throws ODataApplicationException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(new RequestHandleSpy());

    processor.deleteEntity(request, response);
    assertEquals(204, response.getStatusCode());
  }

  @Test
  public void testThrowUnexpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(NullPointerException.class).when(handler).deleteEntity(any(JPARequestEntity.class), any(
        EntityManager.class));

    when(requestContext.getCUDRequestHandler()).thenReturn(handler);
    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.deleteEntity(request, response));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());

  }

  @Test
  public void testThrowExpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(JPARequestEntity.class), any(EntityManager.class));

    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.deleteEntity(request, response));

    assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), act.getStatusCode());
  }

  @Test
  public void testConvertEntityType() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param = mock(UriParameter.class);

    keyPredicates.add(param);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    when(param.getName()).thenReturn("ID");
    when(param.getText()).thenReturn("'1'");

    processor.deleteEntity(request, response);

    assertEquals("com.sap.olingo.jpa.processor.core.testmodel.Organization", spy.et.getInternalName());
  }

  @Test
  public void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.deleteEntity(request, response);

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  public void testClaimsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataClaimProvider provider = new JPAODataClaimsProvider();
    final Optional<JPAODataClaimProvider> claims = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getClaimsProvider()).thenReturn(claims);

    processor.deleteEntity(request, response);

    assertNotNull(spy.claims);
    assertTrue(spy.claims.isPresent());
    assertEquals(provider, spy.claims.get());
  }

  @Test
  public void testGroupsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataGroupsProvider provider = new JPAODataGroupsProvider();
    provider.addGroup("Person");
    // final List<String> groups = new ArrayList<>(Arrays.asList("Person"));
    final Optional<JPAODataGroupProvider> groups = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getGroupsProvider()).thenReturn(groups);

    processor.deleteEntity(request, response);

    assertNotNull(spy.groups);
    assertFalse(spy.groups.isEmpty());
    assertEquals("Person", spy.groups.get(0));
  }

  @Test
  public void testConvertKeySingleAttribute() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param = mock(UriParameter.class);

    keyPredicates.add(param);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    when(param.getName()).thenReturn("ID");
    when(param.getText()).thenReturn("'1'");
    processor.deleteEntity(request, response);

    assertEquals(1, spy.keyPredicates.size());
    assertTrue(spy.keyPredicates.get("iD") instanceof String);
    assertEquals("1", spy.keyPredicates.get("iD"));
  }

  @Test
  public void testConvertKeyTwoAttributes() throws ODataJPAProcessException {
    // BusinessPartnerRole
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param1 = mock(UriParameter.class);
    UriParameter param2 = mock(UriParameter.class);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(ets.getName()).thenReturn("BusinessPartnerRoles");
    when(param1.getName()).thenReturn("BusinessPartnerID");
    when(param1.getText()).thenReturn("'1'");
    when(param2.getName()).thenReturn("RoleCategory");
    when(param2.getText()).thenReturn("'A'");
    keyPredicates.add(param1);
    keyPredicates.add(param2);
    processor.deleteEntity(request, response);

    assertEquals(2, spy.keyPredicates.size());
    assertTrue(spy.keyPredicates.get("businessPartnerID") instanceof String);
    assertEquals("1", spy.keyPredicates.get("businessPartnerID"));
    assertTrue(spy.keyPredicates.get("roleCategory") instanceof String);
    assertEquals("A", spy.keyPredicates.get("roleCategory"));

  }

  @Test
  public void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.deleteEntity(request, response);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(factory.hasActiveTransaction()).thenReturn(Boolean.TRUE);
    when(requestContext.getEntityManager()).thenReturn(em);

    processor = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());

    processor.deleteEntity(request, response);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  public void testDoesNotCallsValidateChangesOnError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(JPARequestEntity.class), any(EntityManager.class));

    assertThrows(ODataJPAProcessorException.class, () -> processor.deleteEntity(request, response));
    verify(handler, never()).validateChanges(em);
  }

  @Test
  public void testDoesRollbackIfValidateRaisesError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);

    RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    // when(em.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(Boolean.FALSE);
    when(requestContext.getEntityManager()).thenReturn(em);

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    processor = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).validateChanges(em);

    assertThrows(ODataApplicationException.class, () -> processor.deleteEntity(request, response));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  public void testBeginIsCalledOnNoTransaction() throws ODataApplicationException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = mock(ODataRequest.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(new RequestHandleSpy());
    processor.deleteEntity(request, response);
    verify(factory, times(1)).createTransaction();
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public int noValidateCalls;
    public Map<String, Object> keyPredicates;
    public JPAEntityType et;
    public Map<String, List<String>> headers;
    public Optional<JPAODataClaimProvider> claims;
    public List<String> groups;

    @Override
    public void deleteEntity(final JPARequestEntity entity, final EntityManager em) {

      this.keyPredicates = entity.getKeys();
      this.et = entity.getEntityType();
      this.headers = entity.getAllHeader();
      this.claims = entity.getClaims();
      this.groups = entity.getGroups();
    }

    @Override
    public void validateChanges(final EntityManager em) throws ODataJPAProcessException {
      noValidateCalls++;
    }
  }
}
