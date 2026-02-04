package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

class MapCacheFunctionTest {
  private MapCacheFunction<String, String, String, String> cycle;

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    final var act = new MapCacheFunction<>(this::functionReturnString, "Test", "String");
    assertFalse(act.get().isEmpty());
    assertEquals(1, act.get().size());
    assertEquals("String", act.get().get("Test"));
  }

  @Test
  void checkCacheThrowsExceptionOnNull() {
    final var act = new MapCacheFunction<>(this::functionReturnNull, "Test", "String");
    assertThrows(NullPointerException.class, act::get);
  }

  @Test
  void checkCacheThrowsException() {
    final var act = new MapCacheFunction<>(this::functionThrows, "Test", "String");
    assertThrows(ODataJPAModelException.class, act::get);
  }

  @Test
  void checkThrowsExceptionOnCallDuringConstruction() throws ODataJPAModelException {
    cycle = new MapCacheFunction<>(this::functionCallsFunction, "Test", "String");
    cycle.get();
  }

  Map<String, String> functionReturnString(final String first, final String last) {
    return Map.of("Test", "String");
  }

  Map<String, String> functionReturnNull(final String first, final String last) { // NOSONAR
    return null;
  }

  Map<String, String> functionThrows(final String first, final String last) {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED, first,
        last));
  }

  Map<String, String> functionCallsFunction(final String first, final String last) {
    try {
      cycle.get();
    } catch (ODataJPAModelException e) {
      assertEquals(MessageKeys.CYCLE_DETECTED.getKey(), e.getId());
    }
    return Map.of();
  }
}
