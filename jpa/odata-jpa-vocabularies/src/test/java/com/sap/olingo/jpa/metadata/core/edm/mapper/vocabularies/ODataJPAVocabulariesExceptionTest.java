package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies.ODataJPAVocabulariesException.MessageKeys;

class ODataJPAVocabulariesExceptionTest {

  private ODataJPAVocabulariesException cut;

  @Test
  void checkReturnsStandardMessageWithoutKey() {
    cut = new ODataJPAVocabulariesException(null, "Test");
    assertNotNull(cut.getMessage());
  }

  @Test
  void checkReturnsStandardMessageWithCauseWithoutKey() {
    final Exception e = new NullPointerException("Null");
    cut = new ODataJPAVocabulariesException(null, e, "Test");
    assertNotNull(cut.getMessage());
  }

  @Test
  void checkReturnsMessageWithKey() {
    cut = new ODataJPAVocabulariesException(MessageKeys.FILE_NOT_FOUND, "Test");
    assertNotNull(cut.getMessage());
    assertTrue(cut.getMessage().contains("Test"));
  }
}
