package com.sap.olingo.jpa.processor.core.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;

public final class JPAEmptyDebugger implements JPAServiceDebugger {

  @Override
  public int startRuntimeMeasurement(final Object instance, final String methodName) {
    return 0;
  }

  @Override
  public void stopRuntimeMeasurement(final int handle) {
    // Not needed
  }

  @Override
  public List<RuntimeMeasurement> getRuntimeInformation() {
    return new ArrayList<>();
  }
}
