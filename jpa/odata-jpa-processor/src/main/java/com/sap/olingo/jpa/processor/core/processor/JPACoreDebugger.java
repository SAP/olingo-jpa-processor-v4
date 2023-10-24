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
  private final MemoryReader memoryReader;

  JPACoreDebugger(final boolean isDebugMode) {
    this.isDebugMode = isDebugMode;
    this.memoryReader = new MemoryReader();
  }

  @Override
  public void debug(final Object instance, final String log) {
    final Long threadID = Thread.currentThread().getId();
    LogFactory.getLog(instance.getClass().getCanonicalName())
        .debug(String.format("thread: %d, logger: %s, info %s", threadID, this, log));
  }

  @Override
  public void debug(final Object instance, final String pattern, final Object... arguments) {
    final Log logger = LogFactory.getLog(instance.getClass().getCanonicalName());
    if (logger.isDebugEnabled()) {
      logger.debug(composeLog(pattern, arguments));
    }
  }

  @Override
  public List<RuntimeMeasurement> getRuntimeInformation() {
    if (isDebugMode)
      return runtimeInformation;
    return Collections.emptyList();
  }

  @Override
  public JPARuntimeMeasurement newMeasurement(final Object instance, final String methodName) {
    final Measurement m = new Measurement(instance, methodName, memoryReader);
    if (isDebugMode)
      runtimeInformation.add(m);
    return m;
  }

  @Override
  public void trace(final Object instance, final String pattern, final Object... arguments) {
    final Log logger = LogFactory.getLog(instance.getClass().getCanonicalName());
    if (logger.isTraceEnabled()) {
      logger.trace(composeLog(pattern, arguments));
    }
  }

  public boolean hasMemoryInformation() {
    return memoryReader.isSAPJvm;
  }

  private Object[] composeArguments(final Long threadID, final Object... arguments) {
    final Object[] allArgs = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, allArgs, 1, arguments.length);
    allArgs[0] = threadID;
    return allArgs;
  }

  private String composeLog(final String pattern, final Object... arguments) {
    final Long threadID = Thread.currentThread().getId();
    final StringBuilder log = new StringBuilder().append("thread: %d, ").append(pattern);
    return String.format(log.toString(), composeArguments(threadID, arguments));
  }

  private static class Measurement extends RuntimeMeasurement implements JPARuntimeMeasurement {

    private final MemoryReader memoryReader;

    public Measurement(final Object instance, final String methodName, final MemoryReader memoryReader) {
      this.setTimeStarted(System.nanoTime());
      this.setClassName(instance.getClass().getCanonicalName());
      this.setMethodName(methodName);
      this.memoryReader = memoryReader;
    }

    @Override
    public void close() {
      this.setTimeStopped(System.nanoTime());
      final long threadID = Thread.currentThread().getId();
      final long runtime = (this.getTimeStopped() - this.getTimeStarted()) / 1000;
      final Long memory = memoryReader.getCurrentThreadMemoryConsumption() / 1000;
      LogFactory.getLog(this.getClassName())
          .debug(String.format("thread: %d, method: %s,  runtime [µs]: %d; memory [kb]: %d",
              threadID,
              this.getMethodName(),
              runtime,
              memory));
    }
  }

  private static class MemoryReader {
    private Object[] memoryInfoReader;
    private boolean isSAPJvm = true;

    public MemoryReader() {
      super();
      try {
        memoryInfoReader = getMemoryInformation();
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
        memoryInfoReader = null;
        isSAPJvm = false;
      }
    }

    long getCurrentThreadMemoryConsumption() {
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

    private long getMemoryConsumption() {

      try {

        final Object memoryInfo = ((Method) memoryInfoReader[1]).invoke(memoryInfoReader[0], Thread.currentThread());
        final Method getMemoryConsumption = memoryInfo.getClass().getMethod("getMemoryConsumption");
        return (long) getMemoryConsumption.invoke(memoryInfo);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
          | SecurityException e) {
        return 0;
      }
    }

    private Object[] getMemoryInformation() throws ClassNotFoundException, NoSuchMethodException,
        IllegalAccessException, InvocationTargetException, InstantiationException {

      final Class<?> info = Class.forName("com.sap.jvm.monitor.vm.VmInfo");
      final Object vmInfo = info.getConstructor().newInstance();
      final Method getMemoryInfo = info.getMethod("getThreadMemoryInfo", Thread.class);

      return new Object[] { vmInfo, getMemoryInfo };
    }
  }
}
