package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

class MapCacheSupplierTest {
  private MapCacheSupplier<String, String> cycle;

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    var act = new MapCacheSupplier<>(this::supplierReturnString);
    assertFalse(act.get().isEmpty());
    assertEquals(1, act.get().size());
    assertEquals("String", act.get().get("Test"));
  }

  @Test
  void checkCacheThrowsExceptionOnNull() {
    var act = new MapCacheSupplier<>(this::supplierReturnNull);
    assertThrows(NullPointerException.class, act::get);
  }

  @Test
  void checkCacheThrowsException() {
    var act = new MapCacheSupplier<>(this::supplierThrows);
    assertThrows(ODataJPAModelException.class, act::get);
  }

  @Test
  void checkThrowsExceptionOnCallDuringConstruction() throws ODataJPAModelException {
    cycle = new MapCacheSupplier<>(this::supplierCallsSupplier);
    cycle.get();
  }

  Map<String, String> supplierReturnString() {
    return Map.of("Test", "String");
  }

  Map<String, String> supplierReturnNull() {
    return null;
  }

  Map<String, String> supplierThrows() {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED));
  }

  Map<String, String> supplierCallsSupplier() {
    try {
      cycle.get();
    } catch (ODataJPAModelException e) {
      assertEquals(MessageKeys.CYCLE_DETECTED.getKey(), e.getId());
    }
    return Map.of();
  }

}
