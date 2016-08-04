package org.apache.olingo.jpa.processor.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

class JPACoreDeugger implements JPAServiceDebugger {
  private final List<RuntimeMeasurement> runtimeInformation = new ArrayList<RuntimeMeasurement>();

  @Override
  public int startRuntimeMeasurement(String className, String methodName) {
    int handleId = runtimeInformation.size();

    final RuntimeMeasurement measurement = new RuntimeMeasurement();
    measurement.setTimeStarted(System.nanoTime());
    measurement.setClassName(className);
    measurement.setMethodName(methodName);

    runtimeInformation.add(measurement);

    return handleId;
  }

  @Override
  public void stopRuntimeMeasurement(int handle) {
    if (handle < runtimeInformation.size()) {
      RuntimeMeasurement runtimeMeasurement = runtimeInformation.get(handle);
      if (runtimeMeasurement != null) {
        runtimeMeasurement.setTimeStopped(System.nanoTime());
      }
    }
  }

  @Override
  public Collection<? extends RuntimeMeasurement> getRuntimeInformation() {
    return runtimeInformation;
  }

}
