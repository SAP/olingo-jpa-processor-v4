package com.sap.olingo.jpa.processor.core.api;

import java.util.Collection;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

public interface JPAServiceDebugger {

  public int startRuntimeMeasurement(final Object instance, final String methodName);

  public void stopRuntimeMeasurement(final int handle);

  public Collection<RuntimeMeasurement> getRuntimeInformation();

  public default void debug(final Object instance, final String pattern, final Object... arguments) {}

  public default void trace(final Object instance, final String pattern, final Object... arguments) {}

  public default void debug(final Object instance, final String log) {}
}
