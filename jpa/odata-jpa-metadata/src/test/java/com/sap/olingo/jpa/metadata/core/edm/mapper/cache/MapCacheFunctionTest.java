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

  @Test
  void checkCacheReturnsGivenValue() throws ODataJPAModelException {
    var act = new MapCacheFunction<>(this::functionReturnString, "Test", "String");
    assertFalse(act.get().isEmpty());
    assertEquals(1, act.get().size());
    assertEquals("String", act.get().get("Test"));
  }

  @Test
  void checkCacheThrowsExceptionOnNull() throws ODataJPAModelException {
    var act = new MapCacheFunction<>(this::functionReturnNull, "Test", "String");
    assertThrows(NullPointerException.class, act::get);
  }

  @Test
  void checkCacheThrowsException() {
    var act = new MapCacheFunction<>(this::functionThrows, "Test", "String");
    assertThrows(ODataJPAModelException.class, act::get);
  }

  Map<String, String> functionReturnString(String first, String last) {
    return Map.of("Test", "String");
  }

  Map<String, String> functionReturnNull(String first, String last) { // NOSONAR
    return null;
  }

  Map<String, String> functionThrows(String first, String last) {
    throw new ODataJPAModelInternalException(new ODataJPAModelException(MessageKeys.DB_TYPE_NOT_DETERMINED, first,
        last));
  }
}
