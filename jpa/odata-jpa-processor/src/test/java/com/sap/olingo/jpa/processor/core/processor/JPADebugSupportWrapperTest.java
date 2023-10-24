package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;

class JPADebugSupportWrapperTest {
  private static final String DEBUG_FORMAT = "JSON";
  private JPADebugSupportWrapper cut;
  private DebugSupport warpped;
  private OData odata;

  @BeforeEach
  void setup() throws Exception {
    warpped = mock(DebugSupport.class);
    odata = OData.newInstance();
    cut = new JPADebugSupportWrapper(warpped);
  }

  @Test
  void testInitForwarded() {
    cut.init(odata);
    verify(warpped, times(1)).init(odata);
  }

  @Test
  void testIsUserAuthorizedForwarded() {
    cut.isUserAuthorized();
    verify(warpped, times(1)).isUserAuthorized();
  }

  @Test
  void testCreateDebugResponse() {
    final DebugInformation debugInfo = mock(DebugInformation.class);
    final JPAServiceDebugger debugger = mock(JPAServiceDebugger.class);
    cut.addDebugger(debugger);

    final RuntimeMeasurement first = newRuntimeMeasurement();
    final RuntimeMeasurement second = newRuntimeMeasurement();
    final RuntimeMeasurement third = newRuntimeMeasurement();
    final List<RuntimeMeasurement> debugInfoList = new ArrayList<>(
        Arrays.asList(first, third));
    final List<RuntimeMeasurement> debuggerList = Arrays.asList(second);

    when(debugInfo.getRuntimeInformation()).thenReturn(debugInfoList);
    when(debugger.getRuntimeInformation()).thenReturn(debuggerList);

    cut.createDebugResponse(DEBUG_FORMAT, debugInfo);

    assertEquals(3, debugInfoList.size());

  }

  private RuntimeMeasurement newRuntimeMeasurement() {
    final RuntimeMeasurement r = new RuntimeMeasurement();
    r.setTimeStarted(System.nanoTime());
    return r;
  }
}
