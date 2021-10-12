package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.processor.JPACoreDebugger;

public class JPACoreDebuggerTest {

  private JPACoreDebugger cut;

  @BeforeEach
  public void setup() {
    cut = new JPACoreDebugger();
  }

  @Test
  public void testStartProvidesHandle() {
    int act = cut.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    assertEquals(0, act);
    act = cut.startRuntimeMeasurement(this, "testStartProvidesHandle-1");
    assertEquals(1, act);
  }

  @Test
  public void testRuntimeInfoDoesNotChangeWhenEndCalledTwice() throws InterruptedException {
    final int handle = cut.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    cut.stopRuntimeMeasurement(handle);
    final RuntimeMeasurement[] exp = cut.getRuntimeInformation().toArray(new RuntimeMeasurement[0]);
    assertNotNull(exp);
    final long expStart = exp[0].getTimeStarted();
    final long expStop = exp[0].getTimeStopped();
    TimeUnit.MILLISECONDS.sleep(1);
    cut.stopRuntimeMeasurement(handle);
    final RuntimeMeasurement[] act = cut.getRuntimeInformation().toArray(new RuntimeMeasurement[0]);
    assertNotNull(act);
    assertNotNull(exp);
    assertArrayEquals(exp, act);
    assertEquals(expStart, act[0].getTimeStarted());
    assertEquals(expStop, act[0].getTimeStopped());
  }

  @Test
  public void noErrorOnNotExistingHandle() {
    final int handle = cut.startRuntimeMeasurement(this, "testStartProvidesHandle-0");
    cut.stopRuntimeMeasurement(handle + 1);

  }
}
