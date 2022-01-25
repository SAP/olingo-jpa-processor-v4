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

class TestJPADeleteProcessor extends TestJPAModifyProcessor {

  @Test
  void testSuccessReturnCode() throws ODataApplicationException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(new RequestHandleSpy());

    processor.deleteEntity(request, response);
    assertEquals(204, response.getStatusCode());
  }

  @Test
  void testThrowUnexpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(NullPointerException.class).when(handler).deleteEntity(any(JPARequestEntity.class), any(
        EntityManager.class));

    when(requestContext.getCUDRequestHandler()).thenReturn(handler);
    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.deleteEntity(request, response));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());

  }

  @Test
  void testThrowExpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(JPARequestEntity.class), any(EntityManager.class));

    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> processor.deleteEntity(request, response));

    assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testConvertEntityType() throws ODataJPAProcessException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final RequestHandleSpy spy = new RequestHandleSpy();
    final UriParameter param = mock(UriParameter.class);

    keyPredicates.add(param);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    when(param.getName()).thenReturn("ID");
    when(param.getText()).thenReturn("'1'");

    processor.deleteEntity(request, response);

    assertEquals("com.sap.olingo.jpa.processor.core.testmodel.Organization", spy.et.getInternalName());
  }

  @Test
  void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.deleteEntity(request, response);

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  void testClaimsProvided() throws ODataJPAProcessorException, SerializerException,
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
  void testGroupsProvided() throws ODataJPAProcessorException, SerializerException,
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
  void testConvertKeySingleAttribute() throws ODataJPAProcessException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final RequestHandleSpy spy = new RequestHandleSpy();
    final UriParameter param = mock(UriParameter.class);

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
  void testConvertKeyTwoAttributes() throws ODataJPAProcessException {
    // BusinessPartnerRole
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
    final RequestHandleSpy spy = new RequestHandleSpy();
    final UriParameter param1 = mock(UriParameter.class);
    final UriParameter param2 = mock(UriParameter.class);

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
  void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.deleteEntity(request, response);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(factory.hasActiveTransaction()).thenReturn(Boolean.TRUE);
    when(requestContext.getEntityManager()).thenReturn(em);

    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, new JPAConversionHelper());

    processor.deleteEntity(request, response);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(JPARequestEntity.class), any(EntityManager.class));

    assertThrows(ODataJPAProcessorException.class, () -> processor.deleteEntity(request, response));
    verify(handler, never()).validateChanges(em);
  }

  @Test
  void testDoesRollbackIfValidateRaisesError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    // when(em.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(Boolean.FALSE);
    when(requestContext.getEntityManager()).thenReturn(em);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, new JPAConversionHelper());

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).validateChanges(em);

    assertThrows(ODataApplicationException.class, () -> processor.deleteEntity(request, response));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testBeginIsCalledOnNoTransaction() throws ODataApplicationException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = mock(ODataRequest.class);
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
