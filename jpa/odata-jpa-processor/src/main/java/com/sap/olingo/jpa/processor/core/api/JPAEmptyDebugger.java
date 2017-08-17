package com.sap.olingo.jpa.processor.core.api;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

final class JPAEmptyDebugger implements JPAServiceDebugger {

  @Override
  public int startRuntimeMeasurement(final String className, final String methodName) {
    return 0;
  }

  @Override
  public void stopRuntimeMeasurement(final int handle) {}

  @Override
  public Collection<? extends RuntimeMeasurement> getRuntimeInformation() {
    return new ArrayList<RuntimeMeasurement>();
  }

}
