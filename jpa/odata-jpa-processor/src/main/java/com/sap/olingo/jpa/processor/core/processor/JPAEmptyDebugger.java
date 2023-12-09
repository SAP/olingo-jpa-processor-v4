package com.sap.olingo.jpa.processor.core.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;

public final class JPAEmptyDebugger implements JPAServiceDebugger {

  @Override
  public List<RuntimeMeasurement> getRuntimeInformation() {
    return new ArrayList<>();
  }

  @Override
  public JPARuntimeMeasurement newMeasurement(final Object instance, final String methodName) {
    return new JPAEmptyMeasurement();
  }

  public static class JPAEmptyMeasurement implements JPARuntimeMeasurement {

    @Override
    public void close() {
      // DO Nothing
    }
  }
}
