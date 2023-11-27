package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;

class JPAEmptyDebuggerTest {

  JPAEmptyDebugger cut;

  @BeforeEach
  void setup() {
    cut = new JPAEmptyDebugger();
  }

  @Test
  void testMeasurementCreated() throws Exception {
    try (JPARuntimeMeasurement measurement = cut.newMeasurement(cut, "firstTest")) {

    }
    assertTrue(cut.getRuntimeInformation().isEmpty());
  }

}
