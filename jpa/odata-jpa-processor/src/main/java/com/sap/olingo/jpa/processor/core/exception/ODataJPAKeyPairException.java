package com.sap.olingo.jpa.processor.core.exception;

import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

public class ODataJPAKeyPairException extends ODataJPAProcessException {

  private static final long serialVersionUID = 6006099025067551818L;
  private static final String BUNDLE_NAME = "processor-exceptions-i18n";
  private static final String MESSAGE_KEY = "KEY_PAIR_CONVERTION_ERROR";

  public ODataJPAKeyPairException(final Throwable cause, final String... params) {
    super(MESSAGE_KEY, INTERNAL_SERVER_ERROR, cause, params);
  }

  @Override
  protected String getBundleName() {
    return BUNDLE_NAME;
  }

}

