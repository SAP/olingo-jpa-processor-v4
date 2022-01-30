package com.sap.olingo.jpa.processor.core.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchException;

public class JPAODataBatchSequentialRequestGroupTest {
  private JPAODataBatchSequentialRequestGroup cut;
  private JPAODataBatchProcessor processor;
  private ODataHandler odataHandler;
  private JPAODataRequestContextAccess requestContext;
  private JPAODataCRUDContextAccess serviceContext;
  private OData odata;
  private ServiceMetadata serviceMetadata;
  private List<BatchRequestPart> groupElements;

  @BeforeEach
  public void setup() {
    odata = mock(OData.class);
    serviceMetadata = mock(ServiceMetadata.class);
    serviceContext = mock(JPAODataCRUDContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    odataHandler = mock(ODataHandler.class);
    processor = mock(JPAODataBatchProcessor.class);
    groupElements = new ArrayList<>();

    processor.init(odata, serviceMetadata);

    when(odata.createRawHandler(serviceMetadata)).thenReturn(odataHandler);
    cut = new JPAODataBatchSequentialRequestGroup(processor, groupElements);

  }

//final ODataHandler odataHandler = processor.odata.createRawHandler(processor.serviceMetadata);
//odataHandler.register(new JPAODataRequestProcessor(processor.serviceContext, processor.requestContext));
//handle(request.getRequests().get(0), false);
//      response = oDataHandler.process(request);
  @Test
  public void testEmptyGroupsReturnEmptyResult() {
    Assertions.assertTrue(cut.execute().isEmpty());
  }

  @Test
  public void testOneGroupOneResult() {
    final BatchRequestPart part = mock(BatchRequestPart.class);
    final List<ODataRequest> get = new ArrayList<>(1);
    final ODataRequest request = mock(ODataRequest.class);
    get.add(request);
    groupElements.add(part);
    when(part.getRequests()).thenReturn(get);
    final List<ODataResponsePart> act = cut.execute();
    Assertions.assertEquals(1, act.size());
    verify(odataHandler, times(1)).process(request);
  }

  @Test
  public void testTwoGroupTwoResults() {
    final ODataRequest request1 = buildPart();
    final ODataRequest request2 = buildPart();
    final ODataResponse part1 = mock(ODataResponse.class);
    final ODataResponse part2 = mock(ODataResponse.class);
    when(odataHandler.process(request1)).thenReturn(part1);
    when(odataHandler.process(request2)).thenReturn(part2);

    final List<ODataResponsePart> act = cut.execute();
    Assertions.assertEquals(2, act.size());
    verify(odataHandler, times(1)).process(request1);
    verify(odataHandler, times(1)).process(request2);
    Assertions.assertEquals(part1, act.get(0).getResponses().get(0));
    Assertions.assertEquals(part2, act.get(1).getResponses().get(0));
  }

  @Test
  public void testExceptionGetsRethrown() {
    Assertions.assertThrows(ODataJPABatchException.class, () -> cut.execute());
  }
  
  private ODataRequest buildPart() {
    final BatchRequestPart part = mock(BatchRequestPart.class);
    final List<ODataRequest> get = new ArrayList<>(1);
    final ODataRequest request = mock(ODataRequest.class);
    get.add(request);
    groupElements.add(part);
    when(part.getRequests()).thenReturn(get);
    return request;
  }
}
