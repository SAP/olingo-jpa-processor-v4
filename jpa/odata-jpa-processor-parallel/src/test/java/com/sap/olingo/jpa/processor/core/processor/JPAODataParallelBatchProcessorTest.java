package com.sap.olingo.jpa.processor.core.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;

class JPAODataParallelBatchProcessorTest {
  private JPAODataParallelBatchProcessor cut;

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

  @BeforeEach
  void setup() throws ODataJPATransactionException {
    MockitoAnnotations.openMocks(this);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getCUDRequestHandler()).thenReturn(cudHandler);
    when(requestContext.getTransactionFactory()).thenReturn(factory);
    when(factory.createTransaction()).thenReturn(transaction);
    cut = new JPAODataParallelBatchProcessor(sessionContext, requestContext);
    cut.init(odata, serviceMetadata);
    when(requestContext.getDebugger()).thenReturn(new JPAEmptyDebugger());
  }

  @Test
  void testBuildGroupsReturnsEmptyListOnEmptyInput() throws ODataJPABatchException {
    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(Collections.emptyList());
    Assertions.assertNotNull(act);
    Assertions.assertTrue(act.isEmpty());
  }

  @Test
  void testBuildGroupsReturnsOneSequentialGroupOnOnePart() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(1, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchSequentialRequestGroup);
  }

  @Test
  void testBuildGroupsThrowsExceptionOnNonChangeSetWithMultipleEntries() {
    final List<BatchRequestPart> requests = buildParts(buildGet(false, mock(ODataRequest.class)));
    final ODataJPABatchException act = Assertions.assertThrows(ODataJPABatchException.class, () -> cut.buildGroups(
        requests));
    Assertions.assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testBuildGroupsAcceptsChangeSetWithMultipleEntries() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(true, mock(ODataRequest.class)));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(1, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchSequentialRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsOneParallelForTwoGets() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false), buildGet(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(1, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchParallelRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsSeqSeqForGetPost() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false), buildPost(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(2, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchSequentialRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchSequentialRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsParaSeqForGetGetPost() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false), buildGet(false), buildPost(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(2, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchParallelRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchSequentialRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsSeqParaForPatchGetGet() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildPatch(false), buildGet(false), buildGet(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(2, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchSequentialRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchParallelRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsParaSeqParaForGetGetPatchGetGet() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false), buildGet(false), buildPatch(false), buildGet(
        false), buildGet(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(3, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchParallelRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchSequentialRequestGroup);
    Assertions.assertTrue(act.get(2) instanceof JPAODataBatchParallelRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsParaSeqForGetGetChangeSet() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(false), buildGet(false), buildGet(true));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(2, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchParallelRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchSequentialRequestGroup);
  }

  @Test
  void testBuildGroupsReturnsParaSeqForChangeSetGetGet() throws ODataJPABatchException {
    final List<BatchRequestPart> requests = buildParts(buildGet(true), buildGet(false), buildGet(false));

    final List<JPAODataBatchRequestGroup> act = cut.buildGroups(requests);
    Assertions.assertNotNull(act);
    Assertions.assertEquals(2, act.size());
    Assertions.assertTrue(act.get(0) instanceof JPAODataBatchSequentialRequestGroup);
    Assertions.assertTrue(act.get(1) instanceof JPAODataBatchParallelRequestGroup);
  }

  private BatchRequestPart buildGet(final boolean isChangeSet, final ODataRequest... addRequest) {
    return buildPart(HttpMethod.GET, isChangeSet, addRequest);
  }

  private BatchRequestPart buildPost(final boolean isChangeSet, final ODataRequest... addRequest) {
    return buildPart(HttpMethod.POST, isChangeSet, addRequest);
  }

  private BatchRequestPart buildPatch(final boolean isChangeSet, final ODataRequest... addRequest) {
    return buildPart(HttpMethod.PATCH, isChangeSet, addRequest);
  }

  private BatchRequestPart buildPart(final HttpMethod method, final boolean isChangeSet,
      final ODataRequest... addRequest) {
    final BatchRequestPart post = mock(BatchRequestPart.class);
    final List<ODataRequest> requestParts = new ArrayList<>();
    final ODataRequest getPart = mock(ODataRequest.class);
    when(post.getRequests()).thenReturn(requestParts);
    when(post.isChangeSet()).thenReturn(isChangeSet);
    when(getPart.getMethod()).thenReturn(method);
    requestParts.add(getPart);
    requestParts.addAll(Arrays.asList(addRequest));
    return post;
  }

  private List<BatchRequestPart> buildParts(final BatchRequestPart... get) {
    final List<BatchRequestPart> requests = new ArrayList<>();
    requests.addAll(Arrays.asList(get));
    return requests;
  }
}
