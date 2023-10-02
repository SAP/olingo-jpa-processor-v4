/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since 1.1.1
 * 15.02.2023
 */
class ODataVocabularyReadExceptionTest {
  @Test
  void checkCreatesWithItemAndPath() {
    final Exception e = new NullPointerException();
    final ODataVocabularyReadException cut = new ODataVocabularyReadException("Test", "Dummy", e);
    assertTrue(cut.getMessage().contains("Test"));
    assertTrue(cut.getMessage().contains("Dummy"));
    assertEquals(e, cut.getCause());
  }

  @Test
  void checkCreatesWithPath() throws URISyntaxException {
    final URI uri = new URI("http://example.org:8080");
    final Exception e = new NullPointerException();
    final ODataVocabularyReadException cut = new ODataVocabularyReadException(uri, "Test", e);
    assertTrue(cut.getMessage().contains("Test"));
    assertTrue(cut.getMessage().contains(uri.toString()));
    assertEquals(e, cut.getCause());
  }
}
