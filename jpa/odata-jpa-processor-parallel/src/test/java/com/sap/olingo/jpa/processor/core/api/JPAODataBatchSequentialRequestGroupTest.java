package com.sap.olingo.jpa.processor.core.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

public class JPAODataBatchSequentialRequestGroupTest extends JPAODataBatchAbstractRequestGroupTest {

  private JPAODataBatchSequentialRequestGroup cut;

  @Override
  @BeforeEach
  public void setup() throws ODataApplicationException, ODataLibraryException {
    super.setup();
    cut = new JPAODataBatchSequentialRequestGroup(processor, groupElements);
  }

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
  public void testExecuteRethrowsException() throws ODataApplicationException, ODataLibraryException {
    final BatchRequestPart part = mock(BatchRequestPart.class);
    final List<ODataRequest> post = new ArrayList<>(1);
    final ODataRequest request = mock(ODataRequest.class);
    final ODataResponse response = mock(ODataResponse.class);
    post.add(request);
    groupElements.add(part);
    when(request.getRawODataPath()).thenReturn("Organizations('3')");
    when(request.getRawBaseUri()).thenReturn("Organizations('3')");
    when(request.getMethod()).thenReturn(HttpMethod.POST);
    when(part.getRequests()).thenReturn(post);
    when(part.isChangeSet()).thenReturn(true);
    when(response.getHeader(HttpHeader.LOCATION)).thenReturn("Hallo");
    when(odataHandler.process(request)).thenReturn(response);
    Assertions.assertThrows(ODataJPABatchRuntimeException.class, cut::execute);
  }
}
