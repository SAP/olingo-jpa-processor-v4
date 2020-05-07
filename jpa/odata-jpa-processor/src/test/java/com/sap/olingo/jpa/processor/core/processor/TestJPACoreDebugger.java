package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJPACoreDebugger {

  private JPACoreDebugger cutDebugOn;
  private JPACoreDebugger cutDebugOff;

  private static LogHandler logHandler;
  private static PrintStream originOut;

  @BeforeAll
  public static void classSetup() {
    // Redirect log to System out
    System.getProperties().put("org.slf4j.simpleLogger.logFile", "System.err");
    System.getProperties().put(
        "org.slf4j.simpleLogger.log.com.sap.olingo.jpa.processor.core.processor.JPACoreDebuggerTest", "debug");
    System.getProperties().put("org.slf4j.simpleLogger.log.com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger",
        "trace");

    logHandler = new LogHandler();
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.TestJPACoreDebugger").setLevel(Level.FINE);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.TestJPACoreDebugger").addHandler(logHandler);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger").setLevel(Level.FINEST);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger").addHandler(logHandler);
  }

  @AfterAll
  public static void classTeardown() {
    System.setErr(originOut);
  }

  @BeforeEach
  public void setup() {
    cutDebugOn = new JPACoreDebugger(true);
    cutDebugOff = new JPACoreDebugger(false);
  }

  @AfterEach
  public void teardown() {
    logHandler.close();
  }

  @Test
  public void testStartProvidesHandle() {
    int act = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    assertEquals(0, act);
    act = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-1");
    assertEquals(1, act);
  }

  @Test
  public void testRuntimeInfoDoesNotChangeWhenEndCalledTwice() throws InterruptedException {
    final int handle = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    cutDebugOn.stopRuntimeMeasurement(handle);
    final RuntimeMeasurement[] exp = cutDebugOn.getRuntimeInformation().toArray(new RuntimeMeasurement[0]);
    assertNotNull(exp);
    final long expStart = exp[0].getTimeStarted();
    final long expStop = exp[0].getTimeStopped();
    TimeUnit.MILLISECONDS.sleep(1);
    cutDebugOn.stopRuntimeMeasurement(handle);
    final RuntimeMeasurement[] act = cutDebugOn.getRuntimeInformation().toArray(new RuntimeMeasurement[0]);
    assertNotNull(act);
    assertNotNull(exp);
    assertArrayEquals(exp, act);
    assertEquals(expStart, act[0].getTimeStarted());
    assertEquals(expStop, act[0].getTimeStopped());
  }

  @Test
  public void noErrorOnNotExistingHandle() {
    final int handle = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    cutDebugOn.stopRuntimeMeasurement(handle + 1);

  }

  @Test
  public void testRuntimeMeasurementEmptyAfterStopWhenOff() {
    final int handle = cutDebugOff.startRuntimeMeasurement(cutDebugOff, "testStartProvidesHandle-0");
    assertEquals(0, handle);
    cutDebugOff.stopRuntimeMeasurement(handle);
    assertTrue(cutDebugOff.getRuntimeInformation().isEmpty());

    assertTrue(logHandler.getCache().size() > 0);
    assertEquals(Level.FINEST, logHandler.getCache().get(0).getLevel());
  }

  @Test
  public void testDebugLogWithTread() {
    cutDebugOff.debug(this, "Test");
    assertTrue(logHandler.getCache().size() > 0);
    assertEquals(Level.FINE, logHandler.getCache().get(0).getLevel());
    assertTrue(logHandler.getCache().get(0).getMessage().contains("thread"));
  }

  @Test
  public void testDebugLogText() {
    cutDebugOff.debug(this, "Test %s", "Hallo");
    assertTrue(logHandler.getCache().size() > 0);
    assertEquals(Level.FINE, logHandler.getCache().get(0).getLevel());
    assertTrue(logHandler.getCache().get(0).getMessage().contains("thread"));
    assertTrue(logHandler.getCache().get(0).getMessage().contains("Hallo"));
  }

  @Test
  public void testDebugHasNoTrace() {
    cutDebugOff.trace(this, "Test");
    assertTrue(logHandler.getCache().size() == 0);
  }

  @Test
  public void testTraceLogText() {
    cutDebugOff.trace(cutDebugOff, "Test %s", "Hallo");
    assertTrue(logHandler.getCache().size() > 0);
    assertEquals(Level.FINEST, logHandler.getCache().get(0).getLevel());
    assertTrue(logHandler.getCache().get(0).getMessage().contains("thread"));
    assertTrue(logHandler.getCache().get(0).getMessage().contains("Hallo"));
  }

  private static class LogHandler extends Handler {

    private List<LogRecord> cache = new ArrayList<>();

    @Override
    public void publish(final LogRecord record) {
      cache.add(record);
    }

    @Override
    public void flush() {
      // Do nothing
    }

    @Override
    public void close() throws SecurityException {
      cache = new ArrayList<>();
    }

    List<LogRecord> getCache() {
      return Collections.unmodifiableList(cache);
    }
  }
}
