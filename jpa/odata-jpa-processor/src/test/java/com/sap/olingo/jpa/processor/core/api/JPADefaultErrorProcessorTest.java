package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPADefaultErrorProcessorTest {
  private JPADefaultErrorProcessor cut;

  @BeforeEach
  void setup() {
    cut = new JPADefaultErrorProcessor();
  }

  @Test
  void testInitDoesNotThrowException() {
    assertDoesNotThrow(() -> cut.init(null, null));
  }

  @Test
  void testProcessErrorDoesNotThrowException() {
    final ODataServerError error = mock(ODataServerError.class);
    final ODataResponse response = mock(ODataResponse.class);
    cut.init(OData.newInstance(), null);
    assertDoesNotThrow(() -> cut.processError(null, response, error, ContentType.JSON));
  }
}
