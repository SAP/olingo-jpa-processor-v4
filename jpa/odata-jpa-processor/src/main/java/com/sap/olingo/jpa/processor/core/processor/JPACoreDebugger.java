package com.sap.olingo.jpa.processor.core.processor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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
    return memoryReader.memoryConsumptionAvailable;
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
    private boolean closed;
    private long usedMemory;

    public Measurement(final Object instance, final String methodName, final MemoryReader memoryReader) {
      this.setTimeStarted(System.nanoTime());
      this.setClassName(instance.getClass().getCanonicalName());
      this.setMethodName(methodName);
      this.memoryReader = memoryReader;
      this.closed = false;
      this.usedMemory = memoryReader.getCurrentThreadMemoryConsumption() / 1000;
    }

    @Override
    public void close() {
      this.setTimeStopped(System.nanoTime());
      this.closed = true;
      final long threadID = Thread.currentThread().getId();
      final long runtime = (this.getTimeStopped() - this.getTimeStarted()) / 1000;
      final Long memory = memoryReader.getCurrentThreadMemoryConsumption() / 1000;
      usedMemory = memory - usedMemory;
      LogFactory.getLog(this.getClassName())
          .debug(String.format(
              "thread: %d, method: %s,  runtime [µs]: %d; over all memory [kb]: %d; additional memory [kb]: %d",
              threadID,
              this.getMethodName(),
              runtime,
              memory,
              usedMemory));
    }

    @Override
    public long getMemoryConsumption() {
      assert closed;
      return usedMemory;
    }
  }

  private static class MemoryReader {
    private boolean memoryConsumptionAvailable = true;

    public MemoryReader() {
      super();
    }

    long getCurrentThreadMemoryConsumption() {
      if (!memoryConsumptionAvailable) {
        return 0L;
      }
      try {
        return getMemoryConsumption();
      } catch (final Exception e) {
        memoryConsumptionAvailable = false;
      }
      return 0L;
    }

    private long getMemoryConsumption() {
      final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
      if (threadMXBean instanceof final com.sun.management.ThreadMXBean sunMXBean) {
        return sunMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
      }
      return 0L;
    }
  }
}
