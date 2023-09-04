/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 15.02.2023
 */
class ODataPathNotFoundExceptionTest {

  @Test
  void checkCreatesWithItemAndPath() {
    final ODataPathNotFoundException cut = new ODataPathNotFoundException("Test", "Dummy");
    assertTrue(cut.getMessage().contains("Test"));
    assertTrue(cut.getMessage().contains("Dummy"));
  }

  @Test
  void checkCreatesWithPath() {
    final Exception e = new NullPointerException();
    final ODataPathNotFoundException cut = new ODataPathNotFoundException("Test", e);
    assertTrue(cut.getMessage().contains("Test"));
    assertEquals(e, cut.getCause());
  }
}
