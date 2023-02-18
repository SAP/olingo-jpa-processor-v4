package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurment;

class JPAEmptyDebuggerTest {

  JPAEmptyDebugger cut;

  @BeforeEach
  void setup() {
    cut = new JPAEmptyDebugger();
  }

  @Test
  void testMeassumentCreated() throws Exception {
    try (JPARuntimeMeasurment meassument = cut.newMeasurement(cut, "firstTest")) {

    }
    assertTrue(cut.getRuntimeInformation().isEmpty());
  }

}
