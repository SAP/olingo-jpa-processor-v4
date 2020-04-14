package com.sap.olingo.jpa.processor.core.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;

/**
 * 
 * 
 * @author Oliver Grande
 * Created: 27.02.2020
 *
 */
public class JPALogger implements JPAServiceDebugger {
  private final List<RuntimeMeasurement> runtimeInformation = new ArrayList<>();

  @Override
  public int startRuntimeMeasurement(final Object instance, final String methodName) {
    final int handleId = runtimeInformation.size();
    final RuntimeMeasurement measurement = new RuntimeMeasurement();
    measurement.setTimeStarted(System.nanoTime());
    measurement.setClassName(instance.getClass().getCanonicalName());
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
        final Long threadID = Thread.currentThread().getId();
        final Long runtime = (runtimeMeasurement.getTimeStopped() - runtimeMeasurement.getTimeStarted()) / 1000;
        LogFactory.getLog(runtimeMeasurement.getClassName())
            .trace(String.format("thread: %d, method: %s,  runtime [µs]: %d",
                threadID,
                runtimeMeasurement.getMethodName(),
                runtime));
      }
    }
  }

  @Override
  public Collection<RuntimeMeasurement> getRuntimeInformation() {
    return runtimeInformation;
  }

  @Override
  public void debug(final Object instance, final String pattern, final Object... arguments) {
    final Log logger = LogFactory.getLog(instance.getClass().getCanonicalName());
    if (logger.isDebugEnabled()) {
      logger.debug(composeLog(pattern, arguments));
    }
  }

  @Override
  public void debug(final Object instance, final String log) {
    final Long threadID = Thread.currentThread().getId();
    LogFactory.getLog(instance.getClass().getCanonicalName())
        .debug(String.format("thread: %d, logger: %s, info %s", threadID, this, log));
  }

  @Override
  public void info(final Object instance, final String pattern, final Object... arguments) {

    final Log logger = LogFactory.getLog(instance.getClass().getCanonicalName());
    if (logger.isInfoEnabled()) {
      logger.info(composeLog(pattern, arguments));
    }
  }

  private String composeLog(final String pattern, final Object... arguments) {
    final Long threadID = Thread.currentThread().getId();
    final StringBuilder log = new StringBuilder().append("thread: %d,").append(pattern);
    return String.format(log.toString(), composeArguments(threadID, arguments));
  }

  private Object[] composeArguments(final Long threadID, final Object... arguments) {
    Object[] allArgs = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, allArgs, 1, arguments.length);
    allArgs[0] = threadID;
    return allArgs;
  }

}
