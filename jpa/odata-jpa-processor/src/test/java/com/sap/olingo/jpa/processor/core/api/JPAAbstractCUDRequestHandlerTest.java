package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPAAbstractCUDRequestHandlerTest {
  private JPAAbstractCUDRequestHandler cut;

  @BeforeEach
  void setup() {
    cut = new ExampleJPAAbstractCUDRequestHandler();
  }

  @Test
  void testThrowsNotImplementedOnDelete() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.deleteEntity(null,
        null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testThrowsNotImplementedOnUpdate() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.updateEntity(null,
        null, null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testThrowsNotImplementedOnCreate() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.createEntity(null,
        null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  private class ExampleJPAAbstractCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
