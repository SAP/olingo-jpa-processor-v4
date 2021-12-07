package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;

class JPACoreDebugger implements JPAServiceDebugger {
  private final List<RuntimeMeasurement> runtimeInformation = new ArrayList<>();
  private final boolean isDebugMode;
  private Object[] memoryInfoReader;
  private boolean isSAPJvm = true;

  public JPACoreDebugger(final boolean isDebugMode) {
    this.isDebugMode = isDebugMode;

    try {
      memoryInfoReader = getMemoryInformation();
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
      memoryInfoReader = null;
      isSAPJvm = false;
    }
  }

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
        final Long memory = getCurrentThreadMemoryConsumption() / 1000;
        LogFactory.getLog(runtimeMeasurement.getClassName())
            .trace(String.format("thread: %d, method: %s,  runtime [µs]: %d; memory [kb]: %d",
                threadID,
                runtimeMeasurement.getMethodName(),
                runtime,
                memory));
        if (!isDebugMode)
          runtimeInformation.remove(handle);
      }
    }
  }

  @Override
  public List<RuntimeMeasurement> getRuntimeInformation() {
    if (isDebugMode)
      return runtimeInformation;
    return Collections.emptyList();
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
  public void trace(final Object instance, final String pattern, final Object... arguments) {
    final Log logger = LogFactory.getLog(instance.getClass().getCanonicalName());
    if (logger.isTraceEnabled()) {
      logger.trace(composeLog(pattern, arguments));
    }
  }

  private String composeLog(final String pattern, final Object... arguments) {
    final Long threadID = Thread.currentThread().getId();
    final StringBuilder log = new StringBuilder().append("thread: %d, ").append(pattern);
    return String.format(log.toString(), composeArguments(threadID, arguments));
  }

  private Object[] composeArguments(final Long threadID, final Object... arguments) {
    final Object[] allArgs = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, allArgs, 1, arguments.length);
    allArgs[0] = threadID;
    return allArgs;
  }

  private long getCurrentThreadMemoryConsumption() {
    long result = 0;
    if (!isSAPJvm) {
      return result;
    }
    try {
      result = getMemoryConsumption();
    } catch (NoClassDefFoundError | Exception e) {
      isSAPJvm = false;
    }
    return result;
  }

  protected long getMemoryConsumption() {

    try {

      final Object memInfo = ((Method) memoryInfoReader[1]).invoke(memoryInfoReader[0], Thread.currentThread());
      final Method getMemConsumption = memInfo.getClass().getMethod("getMemoryConsumption");
      return (long) getMemConsumption.invoke(memInfo);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      return 0;
    }
  }

  private Object[] getMemoryInformation() throws ClassNotFoundException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {

    final Class<?> info = Class.forName("com.sap.jvm.monitor.vm.VmInfo");
    final Object vmInfo = info.getConstructor().newInstance();
    final Method getMemInfo = info.getMethod("getThreadMemoryInfo", Thread.class);

    return new Object[] { vmInfo, getMemInfo };
  }
}
