package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;

class JPACoreDebuggerTest {

  private JPACoreDebugger cutDebugOn;
  private JPACoreDebugger cutDebugOff;

  private static LogHandler logHandler;

  private PrintStream systemOut;
  private OutputStream output;
  private PrintStream printOut;

  @BeforeAll
  static void classSetup() {
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
  void testMeasurementCreated() {
    try (JPARuntimeMeasurement measurement = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {}
    assertFalse(cutDebugOn.getRuntimeInformation().isEmpty());
  }

  @Test
  void testNoMeasurementDebugFalls() {
    cutDebugOn = new JPACoreDebugger(false);
    try (JPARuntimeMeasurement measurement = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {}
    assertTrue(cutDebugOn.getRuntimeInformation().isEmpty());
  }

  @Test
  void testMeasurementCreateMeasurement() throws Exception {
    try (JPARuntimeMeasurement measurement = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {
      TimeUnit.MILLISECONDS.sleep(100);
    }
    assertFalse(cutDebugOn.getRuntimeInformation().isEmpty());
    final RuntimeMeasurement act = cutDebugOn.getRuntimeInformation().get(0);
    final long delta = act.getTimeStopped() - act.getTimeStarted();
    assertTrue(delta >= 100);
    assertEquals("firstTest", act.getMethodName());
    assertEquals(cutDebugOn.getClass().getName(), act.getClassName());
  }

  @Test
  void testRuntimeMeasurementEmptyAfterStopWhenOff() throws InterruptedException {
    System.setErr(printOut);
    try (JPARuntimeMeasurement measurement = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {
      TimeUnit.MILLISECONDS.sleep(10);
    }
    final String act = output.toString();
    assertTrue(cutDebugOff.getRuntimeInformation().isEmpty());
    assertTrue(StringUtils.isNotEmpty(act));
  }

  @SuppressWarnings("resource")
  @Test
  void testMemoryMeasurement() {
    final JPARuntimeMeasurement measurement;
    try (final JPARuntimeMeasurement m = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {
      @SuppressWarnings("unused")
      final String[] dummy = new String[100];
      measurement = m;
    }
    assertTrue(measurement.getMemoryConsumption() > 0);
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

  @SuppressWarnings("resource")
  @Test
  void testMemoryConsumption() {
    final JPARuntimeMeasurement act;
    try (JPARuntimeMeasurement measurement = cutDebugOn.newMeasurement(cutDebugOn, "firstTest")) {
      act = measurement;
    } finally {

    }
    assertTrue(act.getMemoryConsumption() < 10);
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
