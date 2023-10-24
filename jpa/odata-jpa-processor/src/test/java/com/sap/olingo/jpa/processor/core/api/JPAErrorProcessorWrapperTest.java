package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.ServiceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAErrorProcessorWrapperTest {
  private JPAErrorProcessor errorProcessor;
  private JPAErrorProcessorWrapper cut;

  @BeforeEach
  void setup() {
    errorProcessor = mock(JPAErrorProcessor.class);
    cut = new JPAErrorProcessorWrapper(errorProcessor);
  }

  @Test
  void testInit() {
    final OData odata = mock(OData.class);
    final ServiceMetadata metadata = mock(ServiceMetadata.class);

    assertDoesNotThrow(() -> cut.init(odata, metadata));
  }

  @Test
  void testProcessErrorCallsErrorProcessor() {
    final ODataRequest request = mock(ODataRequest.class);
    final ODataResponse response = mock(ODataResponse.class);
    final ODataServerError serverError = mock(ODataServerError.class);
    final ContentType responseFormat = ContentType.APPLICATION_JSON;

    cut.processError(request, response, serverError, responseFormat);
    verify(errorProcessor, times(1)).processError(request, serverError);
  }
}
