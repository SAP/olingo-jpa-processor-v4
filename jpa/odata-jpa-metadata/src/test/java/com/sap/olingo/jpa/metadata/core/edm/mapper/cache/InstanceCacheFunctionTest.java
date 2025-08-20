package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

class InstanceCacheFunctionTest {

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    var act = new InstanceCacheFunction<>(this::functionReturnString, "Test", "String");
    assertTrue(act.get().isPresent());
    assertEquals("TestString", act.get().get());
  }

  @Test
  void checkCacheReturnsEmpty() throws ODataJPAModelException {
    var act = new InstanceCacheFunction<>(this::functionReturnNull, "Test", "String");
    assertTrue(act.get().isEmpty());
  }

  @Test
  void checkCacheThrowsException() {
    var act = new InstanceCacheFunction<>(this::functionThrows, "Test", "String");
    assertThrows(ODataJPAModelException.class, act::get);
  }

  String functionReturnString(String first, String last) {
    return first + last;
  }

  String functionReturnNull(String first, String last) { // NOSONAR
    return null;
  }

  String functionThrows(String first, String last) {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED, first,
        last));
  }
}
