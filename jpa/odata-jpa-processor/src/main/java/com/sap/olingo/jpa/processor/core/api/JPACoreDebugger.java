package com.sap.olingo.jpa.processor.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

class JPACoreDebugger implements JPAServiceDebugger {
  private final List<RuntimeMeasurement> runtimeInformation = new ArrayList<>();

  @Override
  public int startRuntimeMeasurement(final Object instance, final String methodName) {
    final int handleId = runtimeInformation.size();

    final RuntimeMeasurement measurement = new RuntimeMeasurement();
    measurement.setTimeStarted(System.nanoTime());
    measurement.setClassName(instance.getClass().getSimpleName());
    measurement.setMethodName(methodName);

    runtimeInformation.add(measurement);

    return handleId;
  }

  @Override
  public void stopRuntimeMeasurement(final int handle) {
    if (handle < runtimeInformation.size()) {
      final RuntimeMeasurement runtimeMeasurement = runtimeInformation.get(handle);
      if (runtimeMeasurement != null && runtimeMeasurement.getTimeStopped() == 0L) {
        runtimeMeasurement.setTimeStopped(System.nanoTime());
      }
    }
  }

  @Override
  public Collection<RuntimeMeasurement> getRuntimeInformation() {
    return runtimeInformation;
  }

}
