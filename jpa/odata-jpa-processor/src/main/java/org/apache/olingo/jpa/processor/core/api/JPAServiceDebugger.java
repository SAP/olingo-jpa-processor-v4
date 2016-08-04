package org.apache.olingo.jpa.processor.core.api;

import java.util.Collection;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

public interface JPAServiceDebugger {
  public int startRuntimeMeasurement(final String className, final String methodName);

  public void stopRuntimeMeasurement(final int handle);

  public Collection<? extends RuntimeMeasurement> getRuntimeInformation();
}
