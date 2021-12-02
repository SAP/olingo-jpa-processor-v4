package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestJPACoreDebugger {

  private JPACoreDebugger cutDebugOn;
  private JPACoreDebugger cutDebugOff;

  private static LogHandler logHandler;

  private PrintStream systemOut;
  private OutputStream output;
  private PrintStream printOut;

  @BeforeAll
  public static void classSetup() {
    // Redirect log to System out
    System.getProperties().put("org.slf4j.simpleLogger.logFile", "System.err");
    System.getProperties().put(
        "org.slf4j.simpleLogger.log.com.sap.olingo.jpa.processor.core.processor.TestJPACoreDebugger", "debug");
    System.getProperties().put("org.slf4j.simpleLogger.log.com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger",
        "trace");

    logHandler = new LogHandler();
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.TestJPACoreDebugger").setLevel(Level.FINE);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.TestJPACoreDebugger").addHandler(logHandler);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger").setLevel(Level.FINEST);
    Logger.getLogger("com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger").addHandler(logHandler);
  }

  @BeforeEach
  void setup() {
    cutDebugOn = new JPACoreDebugger(true);
    cutDebugOff = new JPACoreDebugger(false);

    systemOut = System.err;
    output = new ByteArrayOutputStream();
    printOut = new PrintStream(output);
  }

  @AfterEach
  void teardown() {
    logHandler.close();
    System.setErr(systemOut);
  }

  @Test
  void testStartProvidesHandle() {
    int act = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    assertEquals(0, act);
    act = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-1");
    assertEquals(1, act);
  }

  @Test
  void testRuntimeInfoDoesNotChangeWhenEndCalledTwice() throws InterruptedException {
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
  void noErrorOnNotExistingHandle() {
    final int handle = cutDebugOn.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    try {
      cutDebugOn.stopRuntimeMeasurement(handle + 1);
    } catch (final Exception e) {
      fail();
    }
  }

  @Test
  void testRuntimeMeasurementEmptyAfterStopWhenOff() {
    System.setErr(printOut);
    final int handle = cutDebugOff.startRuntimeMeasurement(cutDebugOff, "testStartProvidesHandle-0");
    cutDebugOff.stopRuntimeMeasurement(handle);

    assertEquals(0, handle);
    final String act = output.toString();
    assertTrue(cutDebugOff.getRuntimeInformation().isEmpty());
    assertTrue(StringUtils.isNotEmpty(act));
  }

  @Test
  void testDebugLogWithTread() {
    System.setErr(printOut);
    cutDebugOff.debug(this, "Test");
    final String act = output.toString();

    assertTrue(StringUtils.isNotEmpty(act));
    assertTrue(act.contains("thread"));
  }

  @Test
  void testDebugLogText() {
    System.setErr(printOut);
    cutDebugOff.debug(this, "Test %s", "Hallo");
    final String act = output.toString();

    assertTrue(StringUtils.isNotEmpty(act));
    assertTrue(act.contains("thread"));
    assertTrue(act.contains("Hallo"));
  }

  @Test
  void testDebugHasNoTrace() {
    System.setErr(printOut);
    cutDebugOff.trace(this, "Test");
    final String act = output.toString();

    assertTrue(StringUtils.isEmpty(act));
  }

  @Test
  void testTraceLogText() {

    System.setErr(printOut);
    cutDebugOff.trace(cutDebugOff, "Test %s", "Hallo");
    final String act = output.toString();

    assertTrue(StringUtils.isNotEmpty(act));
    assertTrue(act.contains("thread"));
    assertTrue(act.contains("Hallo"));
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
  }
}
