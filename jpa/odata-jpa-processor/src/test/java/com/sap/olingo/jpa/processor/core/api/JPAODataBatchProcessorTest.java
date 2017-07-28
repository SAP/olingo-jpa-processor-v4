package com.sap.olingo.jpa.processor.core.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

@RunWith(MockitoJUnitRunner.class)
public class JPAODataBatchProcessorTest {
  private JPAODataBatchProcessor cut;

  @Mock
  private EntityManager em;

  @Mock
  private EntityTransaction et;

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

  private List<ODataRequest> requests;

  @Before
  public void setup() {
    cut = new JPAODataBatchProcessor(context, em);
    cut.init(odata, serviceMetadata);
    requests = new ArrayList<ODataRequest>();
    requests.add(request);
    when(context.getDebugger()).thenReturn(new JPAEmptyDebugger());
  }

  @Test
  public void whenNotOptimisticLockRollBackExceptionThenThrowODataJPAProcessorExceptionWithHttpCode500()
      throws ODataApplicationException, ODataLibraryException {
    when(em.getTransaction()).thenReturn(et);
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);
    doThrow(e).when(et).commit();

    try {
      cut.processChangeSet(facade, requests);
      Assert.fail("Should have thrown ODataJPAProcessorException!");
    } catch (ODataJPAProcessorException e) {
      Assert.assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
    }

  }

  @Test
  public void whenOptimisticLockRollBackExceptionThenThrowODataJPAProcessorExceptionWithHttpCode412()
      throws ODataApplicationException, ODataLibraryException {
    when(em.getTransaction()).thenReturn(et);
    when(response.getStatusCode()).thenReturn(HttpStatusCode.OK.getStatusCode());
    when(facade.handleODataRequest(request)).thenReturn(response);
    doThrow(e).when(et).commit();
    when(e.getCause()).thenReturn(new OptimisticLockException());

    try {
      cut.processChangeSet(facade, requests);
      Assert.fail("Should have thrown ODataJPAProcessorException!");
    } catch (ODataJPAProcessorException e) {
      Assert.assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), e.getStatusCode());
    }

  }

}
