package org.apache.olingo.jpa.metadata.core.edm.mapper.exception;

import org.junit.Before;
import org.junit.Test;

public class TestODataJPAModelException {
  private static String BUNDLE_NAME = "test-i18n";

  private ODataJPAMessageTextBuffer messages;

  @Before
  public void setup() {
    messages = new ODataJPAMessageTextBuffer(BUNDLE_NAME);
    ODataJPAModelException.setMessageBuffer(messages);
  }

  @Test
  public void checkTextInDefaultLocale() {
    try {
      RaiseExeption();
    } catch (ODataJPAModelException e) {

    }

  }

  private void RaiseExeption() throws ODataJPAModelException {
    throw new ODataJPAModelException(ODataJPAModelException.GENERAL);

  }
}
