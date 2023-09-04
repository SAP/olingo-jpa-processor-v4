package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;

public interface JPAServiceDebugger {

  public List<RuntimeMeasurement> getRuntimeInformation();

  public default void debug(final Object instance, final String pattern, final Object... arguments) {}

  public default void trace(final Object instance, final String pattern, final Object... arguments) {}

  public default void debug(final Object instance, final String log) {}

  public JPARuntimeMeasurement newMeasurement(final Object instance, final String methodName);

  public static interface JPARuntimeMeasurement extends AutoCloseable {
    @Override
    void close();
  }
}
