package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.olingo.commons.api.format.PreferenceName;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.api.prefer.Preferences;
import org.apache.olingo.server.api.prefer.Preferences.Preference;
import org.apache.olingo.server.api.serializer.BatchSerializerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;
import com.sap.olingo.jpa.processor.core.processor.JPAEmptyDebugger;

class JPAODataBatchProcessorTest {
  private JPAODataBatchProcessor cut;

  @Mock
  private EntityManager em;
  @Mock
  private JPAODataTransaction transaction;
  @Mock
  private OData odata;
  @Mock
  private ServiceMetadata serviceMetadata;
  @Mock
  private BatchFacade facade;
  @Mock
  private ODataRequest request;
  @Mock
  private ODataResponse response;
  @Mock
  private RollbackException e;
  @Mock
  private JPAODataSessionContextAccess context;
  @Mock
  private JPACUDRequestHandler cudHandler;
  @Mock
  private JPAODataRequestContextAccess requestContext;
  @Mock
  private JPAODataTransactionFactory factory;
  @Mock
  private JPAODataSessionContextAccess sessionContext;

  private List<ODataRequest> requests;

  @BeforeEach
  void setup() throws ODataJPATransactionException {
    MockitoAnnotations.openMocks(this);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getCUDRequestHandler()).thenReturn(cudHandler);
    when(requestContext.getTransactionFactory()).thenReturn(factory);
    when(factory.createTransaction()).thenReturn(transaction);
//    final JPAODataCRUDContextAccess sessionContext = new JPAODataContextAccessDouble(edmProvider, ds, provider,
//        functionPackage);
    cut = new JPAODataBatchProcessor(sessionContext, requestContext);
    cut.init(odata, serviceMetadata);
    requests = new ArrayList<>();
    requests.add(request);
    when(requestContext.getDebugger()).thenReturn(new JPAEmptyDebugger());
  }

  @Test
  void whenNotOptimisticLockRollBackExceptionThenThrowODataJPAProcessorExceptionWithHttpCode500()
      throws ODataApplicationException, ODataLibraryException {
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);
    doThrow(e).when(transaction).commit();

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class,
        () -> cut.processChangeSet(facade, requests));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void whenOptimisticLockRollBackExceptionThenThrowODataJPAProcessorExceptionWithHttpCode412()
      throws ODataApplicationException, ODataLibraryException {
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);
    doThrow(e).when(transaction).commit();
    when(e.getCause()).thenReturn(new OptimisticLockException());

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class,
        () -> cut.processChangeSet(facade, requests));
    assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), act.getStatusCode());

  }

  @Test
  void whenSuccessfulThenCallValidateChanges() throws ODataApplicationException,
      ODataLibraryException {
    cut = new JPAODataBatchProcessor(sessionContext, requestContext);

    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);

    cut.processChangeSet(facade, requests);
    verify(cudHandler, times(1)).validateChanges(em);
  }

  @Test
  void whenValidateChangesThrowsThenRollbackAndThrow() throws ODataApplicationException,
      ODataLibraryException {
    cut = new JPAODataBatchProcessor(sessionContext, requestContext);
    final ODataJPAProcessException error = new ODataJPAProcessorException(
        ODataJPAProcessorException.MessageKeys.GETTER_NOT_FOUND, HttpStatusCode.BAD_REQUEST);
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);
    doThrow(error).when(cudHandler).validateChanges(em);
    assertThrows(ODataJPAProcessorException.class, () -> cut.processChangeSet(facade, requests));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }
//ODataLibraryException

  @Test
  void whenODataLibraryExceptionThrowsThenRollbackAndThrow() throws ODataApplicationException,
      ODataLibraryException {
    cut = new JPAODataBatchProcessor(sessionContext, requestContext);
    final ODataLibraryException error = new BatchSerializerException("",
        BatchSerializerException.MessageKeys.MISSING_CONTENT_ID, "");
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenThrow(error);
    assertThrows(ODataLibraryException.class, () -> cut.processChangeSet(facade, requests));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void whenNoExceptionOccurredThenCommit() throws ODataApplicationException, ODataLibraryException {

    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);

    final ODataResponsePart act = cut.processChangeSet(facade, requests);
    verify(transaction, times(1)).commit();
    assertTrue(act.isChangeSet());
  }

  @Test
  void whenProcessChangeSetReturnsUnsuccessfulCallThenRollback() throws ODataApplicationException,
      ODataLibraryException {
    cut = new JPAODataBatchProcessor(sessionContext, requestContext);

    when(response.getStatusCode()).thenReturn(HttpStatusCode.BAD_REQUEST.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);

    final ODataResponsePart act = cut.processChangeSet(facade, requests);
    verify(cudHandler, never()).validateChanges(em);
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
    assertFalse(act.isChangeSet());
  }

  @Test
  void whenTransactionCouldNotBeCreatedThenThrowWith501() throws ODataApplicationException,
      ODataLibraryException {

    when(factory.createTransaction()).thenThrow(new ODataJPATransactionException(
        ODataJPATransactionException.MessageKeys.CANNOT_CREATE_NEW_TRANSACTION));
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.processChangeSet(
        facade, requests));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
    verify(facade, never()).handleODataRequest(any());
  }

  @Test
  void whenNoContinueHeaderContinueOnErrorReturnsFalse() {
    final Preferences preferences = mock(Preferences.class);
    when(preferences.getPreference(PreferenceName.CONTINUE_ON_ERROR.getName())).thenReturn(null);
    assertFalse(cut.continueOnError(preferences));
  }

  @Test
  void whenContinueHeaderNoValueContinueOnErrorReturnsTrue() {
    final Preferences preferences = mock(Preferences.class);
    final Preference continueOnError = mock(Preference.class);
    when(preferences.getPreference(PreferenceName.CONTINUE_ON_ERROR.getName())).thenReturn(continueOnError);
    when(continueOnError.getValue()).thenReturn(null);
    assertTrue(cut.continueOnError(preferences));
  }

  @Test
  void whenContinueHeaderTrueContinueOnErrorReturnsTrue() {
    final Preferences preferences = mock(Preferences.class);
    final Preference continueOnError = mock(Preference.class);
    when(preferences.getPreference(PreferenceName.CONTINUE_ON_ERROR.getName())).thenReturn(continueOnError);
    when(continueOnError.getValue()).thenReturn("true");
    assertTrue(cut.continueOnError(preferences));
  }

  @Test
  void whenContinueHeaderFalseContinueOnErrorReturnsFalse() {
    final Preferences preferences = mock(Preferences.class);
    final Preference continueOnError = mock(Preference.class);
    when(preferences.getPreference(PreferenceName.CONTINUE_ON_ERROR.getName())).thenReturn(continueOnError);
    when(continueOnError.getValue()).thenReturn("false");
    assertFalse(cut.continueOnError(preferences));
  }

  @Test
  void whenNotContinueOnErrorSecondNotPerformed() throws ODataApplicationException, ODataLibraryException {
    final List<BatchRequestPart> requestParts = createBatchRequest();
    final ODataResponsePart resp = mock(ODataResponsePart.class);
    final List<ODataResponse> responses = createBatchPartResponse(400);

    when(facade.handleBatchRequest(requestParts.get(0))).thenReturn(resp);
    when(resp.getResponses()).thenReturn(responses);

    cut.executeBatchParts(facade, requestParts, false);

    verify(facade, times(1)).handleBatchRequest(any());
  }

  @Test
  void whenContinueOnErrorSecondPerformed() throws ODataApplicationException, ODataLibraryException {
    final List<BatchRequestPart> requestParts = createBatchRequest();
    final ODataResponsePart resp1 = mock(ODataResponsePart.class);
    final ODataResponsePart resp2 = mock(ODataResponsePart.class);
    final List<ODataResponse> responses1 = createBatchPartResponse(400);
    final List<ODataResponse> responses2 = createBatchPartResponse(200);

    when(facade.handleBatchRequest(requestParts.get(0))).thenReturn(resp1);
    when(resp1.getResponses()).thenReturn(responses1);
    when(facade.handleBatchRequest(requestParts.get(1))).thenReturn(resp2);
    when(resp2.getResponses()).thenReturn(responses2);

    final List<ODataResponsePart> act = cut.executeBatchParts(facade, requestParts, true);

    verify(facade, times(2)).handleBatchRequest(any());
    assertEquals(2, act.size());
  }

  @Test
  void whenNotContinueOnErrorSecondPerformedNoFailuer() throws ODataApplicationException, ODataLibraryException {
    final List<BatchRequestPart> requestParts = createBatchRequest();
    final ODataResponsePart resp1 = mock(ODataResponsePart.class);
    final ODataResponsePart resp2 = mock(ODataResponsePart.class);
    final List<ODataResponse> responses1 = createBatchPartResponse(200);
    final List<ODataResponse> responses2 = createBatchPartResponse(200);

    when(facade.handleBatchRequest(requestParts.get(0))).thenReturn(resp1);
    when(resp1.getResponses()).thenReturn(responses1);
    when(facade.handleBatchRequest(requestParts.get(1))).thenReturn(resp2);
    when(resp2.getResponses()).thenReturn(responses2);

    final List<ODataResponsePart> act = cut.executeBatchParts(facade, requestParts, false);

    verify(facade, times(2)).handleBatchRequest(any());
    assertEquals(2, act.size());
  }

  private List<BatchRequestPart> createBatchRequest() {
    final List<BatchRequestPart> requestParts = new ArrayList<>();
    final BatchRequestPart part1 = mock(BatchRequestPart.class);
    final BatchRequestPart part2 = mock(BatchRequestPart.class);
    requestParts.add(part1);
    requestParts.add(part2);
    return requestParts;
  }

  private List<ODataResponse> createBatchPartResponse(final int statusCode) {
    final List<ODataResponse> responses = new ArrayList<>();
    final ODataResponse response = mock(ODataResponse.class);
    responses.add(response);
    when(response.getStatusCode()).thenReturn(statusCode);
    return responses;
  }
}
