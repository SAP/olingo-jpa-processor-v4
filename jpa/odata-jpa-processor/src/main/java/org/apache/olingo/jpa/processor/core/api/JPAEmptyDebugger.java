package org.apache.olingo.jpa.processor.core.api;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

class JPAEmptyDebugger implements JPAServiceDebugger {

  @Override
  public int startRuntimeMeasurement(String className, String methodName) {
    return 0;
  }

  @Override
  public void stopRuntimeMeasurement(int handle) {}

  @Override
  public Collection<? extends RuntimeMeasurement> getRuntimeInformation() {
    return new ArrayList<RuntimeMeasurement>();
  }

}
