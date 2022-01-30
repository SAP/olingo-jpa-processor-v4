package com.sap.olingo.jpa.processor.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

class TestODataJPAProcessorException {
  // private static String BUNDLE_NAME = "exceptions-i18n";

  public static enum MessageKeys implements ODataJPAMessageKey {
    RESULT_NOT_FOUND;

    @Override
    public String getKey() {
      return name();
    }

  }

  @Test
  void checkSimpleRaiseException() {
    try {
      RaiseException();
    } catch (final ODataApplicationException e) {
      assertEquals("No result was fond by Serializer", e.getMessage());
      assertEquals(400, e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  void checkSimpleViaMessageKeyRaiseException() {
    try {
      RaiseExceptionParam();
    } catch (final ODataApplicationException e) {
      assertEquals("Unable to convert value 'Willi' of parameter 'Hugo'", e.getMessage());
      assertEquals(500, e.getStatusCode());
      return;
    }
    fail();
  }

  private void RaiseExceptionParam() throws ODataJPAProcessException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
        HttpStatusCode.INTERNAL_SERVER_ERROR, "Willi", "Hugo");
  }

  private void RaiseException() throws ODataJPAProcessException {
    throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.RESULT_NOT_FOUND,
        HttpStatusCode.BAD_REQUEST);
  }
}
