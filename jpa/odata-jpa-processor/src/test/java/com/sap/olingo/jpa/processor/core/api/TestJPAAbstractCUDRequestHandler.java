package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public class TestJPAAbstractCUDRequestHandler {
  private JPAAbstractCUDRequestHandler cut;

  @BeforeEach
  public void setup() {
    cut = new ExampleJPAAbstractCUDRequestHandler();
  }

  @Test
  public void testTrothNotImplementedOnDelete() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.deleteEntity(null,
        null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  public void testTrothNotImplementedOnUpdate() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.updateEntity(null,
        null, null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  public void testTrothNotImplementedOnCreate() {
    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.createEntity(null,
        null));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  private class ExampleJPAAbstractCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
