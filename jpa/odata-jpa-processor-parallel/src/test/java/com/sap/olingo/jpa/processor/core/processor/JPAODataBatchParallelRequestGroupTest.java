package com.sap.olingo.jpa.processor.core.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

class JPAODataBatchParallelRequestGroupTest extends JPAODataBatchAbstractRequestGroupTest {
  private JPAODataBatchParallelRequestGroup cut;

  @Override
  @BeforeEach
  void setup() {
    super.setup();
    cut = new JPAODataBatchParallelRequestGroup(processor, groupElements);
  }

  @Test
  void testEmptyGroupsReturnEmptyResult() {
    Assertions.assertTrue(cut.execute().isEmpty());
  }

  @Test
  void testOneGroupOneResult() {
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
  void testTwoPartsFirstSlower() {
    final ODataRequest request1 = buildPart();
    final ODataRequest request2 = buildPart();
    final ODataResponse part1 = mock(ODataResponse.class);
    final ODataResponse part2 = mock(ODataResponse.class);

    when(odataHandler.process(request1)).thenAnswer(new AnswerLate<>(100, part1));
    when(odataHandler.process(request2)).thenAnswer(new AnswerLate<>(10, part2));

    final List<ODataResponsePart> act = cut.execute();
    Assertions.assertEquals(2, act.size());
    verify(odataHandler, times(1)).process(request1);
    verify(odataHandler, times(1)).process(request2);
    Assertions.assertEquals(part1, act.get(0).getResponses().get(0));
    Assertions.assertEquals(part2, act.get(1).getResponses().get(0));
  }

  @Test
  void testTwoPartsSecondSlower() {
    final ODataRequest request1 = buildPart();
    final ODataRequest request2 = buildPart();
    final ODataResponse part1 = mock(ODataResponse.class);
    final ODataResponse part2 = mock(ODataResponse.class);

    when(odataHandler.process(request1)).thenAnswer(new AnswerLate<>(10, part1));
    when(odataHandler.process(request2)).thenAnswer(new AnswerLate<>(100, part2));

    final List<ODataResponsePart> act = cut.execute();
    Assertions.assertEquals(2, act.size());
    verify(odataHandler, times(1)).process(request1);
    verify(odataHandler, times(1)).process(request2);
    Assertions.assertEquals(part1, act.get(0).getResponses().get(0));
    Assertions.assertEquals(part2, act.get(1).getResponses().get(0));
  }

  @Test
  void testThreePartsSecondSlower() {
    final ODataRequest request1 = buildPart();
    final ODataRequest request2 = buildPart();
    final ODataRequest request3 = buildPart();
    final ODataResponse part1 = mock(ODataResponse.class);
    final ODataResponse part2 = mock(ODataResponse.class);
    final ODataResponse part3 = mock(ODataResponse.class);

    when(odataHandler.process(request1)).thenAnswer(new AnswerLate<>(10, part1));
    when(odataHandler.process(request2)).thenAnswer(new AnswerLate<>(100, part2));
    when(odataHandler.process(request3)).thenAnswer(new AnswerLate<>(50, part3));

    final List<ODataResponsePart> act = cut.execute();
    Assertions.assertEquals(3, act.size());
    Assertions.assertEquals(part1, act.get(0).getResponses().get(0));
    Assertions.assertEquals(part2, act.get(1).getResponses().get(0));
    Assertions.assertEquals(part3, act.get(2).getResponses().get(0));
  }

  @Test
  void testExecuteRethrowsException() {
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

  private static class AnswerLate<T> implements Answer<T> {
    private final int millisDelay;
    private final T response;

    public AnswerLate(final int millisDelay, final T response) {
      this.millisDelay = millisDelay;
      this.response = response;
    }

    @Override
    public T answer(final InvocationOnMock invocation) throws Throwable {
      Thread.sleep(millisDelay); // NOSONAR
      return response;
    }

  }
}
