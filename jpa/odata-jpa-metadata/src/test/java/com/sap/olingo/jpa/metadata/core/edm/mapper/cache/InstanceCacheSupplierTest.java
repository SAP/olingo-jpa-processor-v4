package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

class InstanceCacheSupplierTest {

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    var act = new InstanceCacheSupplier<>(this::supplierReturnString);
    assertTrue(act.get().isPresent());
    assertEquals("TestString", act.get().get());
  }

  @Test
  void checkCacheReturnsEmpty() throws ODataJPAModelException {
    var act = new InstanceCacheSupplier<>(this::supplierReturnNull);
    assertTrue(act.get().isEmpty());
  }

  @Test
  void checkCacheThrowsException() {
    var act = new InstanceCacheSupplier<>(this::supplierThrows);
    assertThrows(ODataJPAModelException.class, act::get);
  }

  String supplierReturnString() {
    return "TestString";
  }

  String supplierReturnNull() {
    return null;
  }

  String supplierThrows() {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED));
  }
}
