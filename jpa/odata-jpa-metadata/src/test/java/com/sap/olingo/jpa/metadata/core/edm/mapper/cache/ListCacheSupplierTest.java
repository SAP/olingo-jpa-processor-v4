package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

class ListCacheSupplierTest {

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    var act = new ListCacheSupplier<>(this::supplierReturnString);
    assertFalse(act.get().isEmpty());
    assertEquals(2, act.get().size());
    assertEquals("Test", act.get().get(0));
    assertEquals("String", act.get().get(1));
  }

  @Test
  void checkCacheThrowsExceptionOnNull() {
    var act = new ListCacheSupplier<>(this::supplierReturnNull);
    assertThrows(NullPointerException.class, act::get);
  }

  @Test
  void checkCacheThrowsException() {
    var act = new ListCacheSupplier<>(this::supplierThrows);
    assertThrows(ODataJPAModelException.class, act::get);
  }

  List<String> supplierReturnString() {
    return List.of("Test", "String");
  }

  List<String> supplierReturnNull() {
    return null;
  }

  List<String> supplierThrows() {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED));
  }
}
